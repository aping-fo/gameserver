package com.game.module.activity;

import com.game.data.ActivityCfg;
import com.game.data.ActivityTaskCfg;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.activity.ActivityInfo;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.game.util.TimerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityService implements InitHandler {
    private static Logger logger = Logger.getLogger(ActivityService.class);
    //开启的活动
    public static final Map<Integer, ActivityCfg> OpenActivitys = Maps.newConcurrentMap();
    //活动对任务
    public static final Map<Integer, List<ActivityTaskCfg>> ActivityTasks = Maps.newConcurrentMap();

    private static final int CMD_ACTIVITY_OPEN = 8004; //活动开启
    private static final int CMD_ACTIVITY_TASK_UPDATE = 8006; //任务更新
    private static final int CMD_ACTIVITY_CLOSE = 8005; //关闭

    @Autowired
    private TimerService timerService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;

    @Override
    public void handleInit() {
        try {
            doCheckActivityOpen();
        } catch (Exception e) {
            ServerLogger.err(e, "活动异常");
        }

        LocalDateTime dateTime = LocalDateTime.now();
        int second = dateTime.getSecond();
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次，活动开关
            @Override
            public void run() {
                try {
                    doCheckActivityOpen();
                } catch (Exception e) {
                    ServerLogger.err(e, "活动定时器异常");
                }
            }
        }, 60 - second, 60, TimeUnit.SECONDS);

        //每分钟检测活动状态
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次,任务是否完成
            @Override
            public void run() {
                try {
                    doCheckActivityComplete(playerService.getPlayerDatas().values(), true);
                } catch (Exception e) {
                    ServerLogger.err(e, "玩家活动定时器异常");
                }
            }
        }, 120, 60, TimeUnit.SECONDS);

        for (Object obj1 : GameData.getConfigs(ActivityTaskCfg.class)) {
            ActivityTaskCfg conf = (ActivityTaskCfg) obj1;
            List<ActivityTaskCfg> list = ActivityTasks.get(conf.ActivityId);
            if (list == null) {
                list = Lists.newArrayList();
                ActivityTasks.put(conf.ActivityId, list);
            }
            list.add(conf);
        }
    }

    /**
     * 检测任务是否完成
     *
     * @param players
     */
    private void doCheckActivityComplete(Collection<PlayerData> players, boolean toCli) {
        List<ActivityTask> updateActivityList = Lists.newArrayList();
        for (PlayerData data : players) {
            updateActivityList.clear();
            for (ActivityTask at : data.getActivityTasks().values()) {
                if (checkActivityTaskUpdate(data, at)) {
                    updateActivityList.add(at);
                }
            }
            if (toCli) {
                pushActivityUpdate(data.getPlayerId(), updateActivityList);
            }
        }
    }

    private boolean checkActivityTaskUpdate(PlayerData data, ActivityTask at) {
        boolean bUpdate = false;
        if (at.isRewardFlag()) { //已经奖励完，则不检测
            return bUpdate;
        }

        if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TIME) { //时间区间类型单独处理
            ActivityTaskCfg cfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
            int beginHour = cfg.Conds[0][1];
            int endHour = cfg.Conds[0][2];
            int hour = LocalDateTime.now().getHour();
            if (beginHour <= hour && endHour > hour) {
                if (at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                    at.setState(ActivityConsts.ActivityState.T_FINISH);
                    bUpdate = true;
                }
            } else if (endHour <= hour) { //时间已过，进行补领判断
                if (at.getState() == ActivityConsts.ActivityState.T_UN_FINISH || //未完成的
                        at.getState() == ActivityConsts.ActivityState.T_FINISH) { //已完成的
                    at.setState(ActivityConsts.ActivityState.T_AGAIN_AWARD);
                    bUpdate = true;
                }
            }
        } else { //其他类型
            if (at.getState() == ActivityConsts.ActivityState.T_FINISH) { //已经完成的，则不检测
                return bUpdate;
            }
            if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_LEVEL_UP
                    || at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_GROW_FUND) { //等级类型
                Player player = playerService.getPlayer(data.getPlayerId());
                at.getCond().setValue(player.getLev());
                bUpdate = true;

            } else if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_LOGIN) { //7天登录
                at.getCond().setValue(data.getLoginDays());
                bUpdate = true;
            }
            if (at.getCond().checkComplete()) {
                at.setState(ActivityConsts.ActivityState.T_FINISH);
                bUpdate = true;
            }
        }
        return bUpdate;
    }

    private void doCheckActivityOpen() throws Exception {
        List<ActivityCfg> openActivitys = Lists.newArrayList();
        List<Integer> closeActivitys = Lists.newArrayList();
        LocalDate nowDate = LocalDate.now();
        for (Object obj : GameData.getConfigs(ActivityCfg.class)) {
            ActivityCfg cfg = (ActivityCfg) obj;

            if (cfg.BeginTime != null && !"".equals(cfg.BeginTime)) {
                LocalDate beginDate = LocalDate.parse(cfg.BeginTime, TimeUtil.formatter);
                if (nowDate.isBefore(beginDate)) { //还未开启
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.EndTime != null && !"".equals(cfg.EndTime)) {
                LocalDate beginDate = LocalDate.parse(cfg.EndTime, TimeUtil.formatter);
                if (nowDate.isAfter(beginDate)) { //活动结束
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.WeekTime != null) {
                int week = nowDate.getDayOfWeek().getValue();
                boolean flag = false;
                for (int w : cfg.WeekTime) {
                    flag |= (w == week);
                }
                if (!flag) { //星期不满足
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.HourTime != null) {
                int hour = LocalDateTime.now().getHour();
                boolean flag = false;
                for (int i = 0; i < cfg.HourTime.length; i += 2) {
                    int startHour = cfg.HourTime[i];
                    int endHour = cfg.HourTime[i + 1];
                    if (hour >= startHour && hour < endHour) {
                        flag |= true;
                    }
                }
                if (!flag) { //小时区间不满足
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (!OpenActivitys.containsKey(cfg.id)) {
                OpenActivitys.put(cfg.id, cfg);
                openActivitys.add(cfg);
            }
        }

        if (!openActivitys.isEmpty()) { //新活动开启
            addNewActivity(openActivitys);
        }
        if (!closeActivitys.isEmpty()) { //关闭的活动
            pushActivityClose(closeActivitys, 0);
        }
    }

    /**
     * 创建一个新任务
     *
     * @param taskCfg
     * @return
     */
    private ActivityTask createActivityTask(ActivityTaskCfg taskCfg) {
        int taskType = taskCfg.Conds[0][0];
        int targetCount = taskCfg.Conds[0][1];
        return new ActivityTask(taskCfg.id, taskCfg.ResetType, taskCfg.ActivityId, targetCount, taskType);
    }

    private void closeActivity(int id, List<Integer> closeActivitys) {
        ActivityCfg cfg = OpenActivitys.remove(id);
        if (cfg != null) {
            closeActivitys.add(id);
        }
    }

    /**
     * 完成活动
     *
     * @param playerId
     * @param taskCondType
     * @param value
     * @param updateType
     */
    public void completeActivityTask(int playerId, int taskCondType, int value, int updateType, boolean toCli) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask at : data.getActivityTasks().values()) {
            if (at.getState() != ActivityConsts.ActivityState.T_UN_FINISH) {
                continue;
            }
            int condType = at.getCond().getCondType();
            if (condType == taskCondType && at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                if (updateType == ActivityConsts.UpdateType.T_ADD) {
                    at.getCond().setValue(at.getCond().getValue() + value);
                } else if (updateType == ActivityConsts.UpdateType.T_VALUE) {
                    at.getCond().setValue(value);
                }
                if (at.getCond().checkComplete()) {
                    at.setState(ActivityConsts.ActivityState.T_FINISH);
                }
                tasks.add(at);
            }
        }
        if (toCli) {
            pushActivityUpdate(playerId, tasks);
        }
    }


    /**
     * 登录检测是否有新的活动开启
     *
     * @param playerId
     */
    public void onLogin(int playerId) {
        checkAddNewActivity(playerId, OpenActivitys.values(), null, null);
    }

    private void addNewActivity(List<ActivityCfg> openActivitys) {
        List<Integer> openActivity = Lists.newArrayList();
        List<ActivityTask> openActivityTasks = Lists.newArrayList();
        for (int playerId : playerService.getPlayerDatas().keySet()) {
            openActivity.clear();
            openActivityTasks.clear();
            checkAddNewActivity(playerId, openActivitys, openActivity, openActivityTasks);
        }
    }

    private void checkAddNewActivity(int playerId, Collection<ActivityCfg> openActivitys,
                                     List<Integer> openActivity, List<ActivityTask> openActivityTasks) {
        PlayerData data = playerService.getPlayerData(playerId);
        //Player player = playerService.getPlayer(playerId);
        for (ActivityCfg cfg : openActivitys) {
            if (openActivity != null) {
                openActivity.add(cfg.id);
            }

            if (cfg.OpenType == ActivityConsts.ActivityOpenType.T_HANDLE)
                continue;
/*            if (cfg.ActivityType == ActivityConsts.ActivityType.T_LOGIN) {
                if (!checkLoginActivity(player.getRegTime(), cfg.Param0)) { //过滤本人7天登录活动
                    continue;
                }
            }*/

            List<ActivityTaskCfg> list = ActivityTasks.get(cfg.id);
            for (ActivityTaskCfg taskCfg : list) {
                if (!data.getActivityTasks().containsKey(taskCfg.id)) {
                    ActivityTask at = createActivityTask(taskCfg);
                    if (at.getCond().checkComplete()) {//默认是否完成
                        at.setState(ActivityConsts.ActivityState.T_FINISH);
                    }
                    data.getActivityTasks().put(at.getId(), at);
                    checkActivityTaskUpdate(data, at);
                    if (openActivityTasks != null) {
                        openActivityTasks.add(at);
                    }
                }
            }
        }

        if (openActivity != null && openActivityTasks != null) {
            pushActivityOpen(playerId, openActivity, openActivityTasks);
        }
    }

    /**
     * 检测7天登录活动
     *
     * @param date
     * @return
     */
    private boolean checkLoginActivity(PlayerData data, Date date, int param) {
        LocalDate localDate = LocalDate.now();
        int today = localDate.getDayOfYear();

        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        int day = localDateTime.toLocalDate().getDayOfYear();

        boolean ret = today - day < param; //时间范围内
        boolean unFinish = false;
        if (ret) {
            for (ActivityTask at : data.getActivityTasks().values()) {
                if (at.getState() != ActivityConsts.ActivityState.T_AWARD) {
                    unFinish = true;
                    break;
                }
            }
        }
        return ret && unFinish;
    }

    /**
     * 日常重置
     *
     * @param playerId
     */
    public void dailyRest(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> list = Lists.newArrayList();
        for (ActivityTask at : data.getActivityTasks().values()) {
            if (at.getResetType() == ActivityConsts.ActivityTaskResetType.T_DAILY) {
                at.cleanup();
                list.add(at);
            }
        }
        pushActivityUpdate(playerId, list);

        //检测登录活动
        completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_LOGIN, data.getLoginDays(), ActivityConsts.UpdateType.T_VALUE, true);
    }

    /**
     * 获取活动任务列表
     */
    public ActivityInfo getPlayerActivitys(int playerId) {
        ActivityInfo result = new ActivityInfo();
        result.id = Lists.newArrayList();
        result.tasks = Lists.newArrayList();
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);
        for (ActivityCfg cfg : OpenActivitys.values()) {
            if (cfg.ActivityType == ActivityConsts.ActivityType.T_LOGIN) { //7天登录
                if (!checkLoginActivity(data, player.getRegTime(), cfg.Param0)) {
                    continue;
                }
            } else if (cfg.ActivityType == ActivityConsts.ActivityType.T_NEW_ROLE) { //新手礼包
                List<ActivityTaskCfg> list = ActivityTasks.get(cfg.id);
                boolean bClose = true;
                for (ActivityTaskCfg taskCfg : list) {
                    ActivityTask task = data.getActivityTasks().get(taskCfg.id);
                    if (task != null) {
                        bClose = bClose && task.getState() == ActivityConsts.ActivityState.T_AWARD;
                    }
                }
                if (bClose) {
                    continue;
                }
            }
            result.id.add(cfg.id);
        }

        for (ActivityTask at : data.getActivityTasks().values()) {
            if (result.id.contains(at.getActivityId())) {
                result.tasks.add(at.toProto());
            }
        }
        return result;
    }

    /**
     * 领取奖励
     *
     * @param playerId
     * @param taskId
     * @return
     */
    public IntParam getActivityAwards(int playerId, int taskId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityTask task = data.getActivityTasks().get(taskId);
        IntParam result = new IntParam();
        if (task.getState() != ActivityConsts.ActivityState.T_FINISH) {
            result.param = Response.ERR_PARAM;
            return result;
        }

        task.setState(ActivityConsts.ActivityState.T_AWARD);
        task.setRewardFlag(true);
        ActivityTaskCfg config = ConfigData.getConfig(ActivityTaskCfg.class, taskId);
        goodsService.addRewards(playerId, config.Rewards, LogConsume.ACTIVITY_REWARD);

        ActivityCfg cfg = ConfigData.getConfig(ActivityCfg.class, task.getActivityId());
        if (cfg.ActivityType == ActivityConsts.ActivityType.T_NEW_ROLE) {
            pushActivityClose(Lists.newArrayList(task.getActivityId()), playerId);
        }
        pushActivityUpdate(playerId, Lists.newArrayList(task));
        result.param = Response.SUCCESS;
        return result;
    }


    /**
     * 补领奖励
     *
     * @param playerId
     * @param taskId
     * @return
     */
    public IntParam fixedActivityAwards(int playerId, int taskId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityTask task = data.getActivityTasks().get(taskId);
        IntParam result = new IntParam();
        if (task.getState() != ActivityConsts.ActivityState.T_AGAIN_AWARD) {
            result.param = Response.ERR_PARAM;
            return result;
        }
        ActivityTaskCfg config = ConfigData.getConfig(ActivityTaskCfg.class, taskId);
        if (!playerService.decDiamond(playerId, config.Param0, LogConsume.ACTIVITY_RE_REWARD)) {
            result.param = Response.NO_DIAMOND;
            return result;
        }
        task.setState(ActivityConsts.ActivityState.T_AWARD);
        task.setRewardFlag(true);
        goodsService.addRewards(playerId, config.Rewards, LogConsume.ACTIVITY_REWARD);

        pushActivityUpdate(playerId, Lists.newArrayList(task));
        result.param = Response.SUCCESS;
        return result;
    }

    /**
     * 手动领取活动
     *
     * @param playerId
     * @param activityId
     * @return
     */
    public IntParam openActivity(int playerId, int activityId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, activityId);
        IntParam result = new IntParam();
        Player player = playerService.getPlayer(playerId);
        boolean flag = true;
        List<GoodsEntry> items = Lists.newArrayList();
        for (int[] cond : config.Conds) {
            int type = cond[0];
            if (type == ActivityConsts.ActivityCondType.T_VIP) {
                flag = flag && (player.getVip() >= cond[1]);
            } else if (type == ActivityConsts.ActivityCondType.T_ITEM) {
                GoodsEntry item = new GoodsEntry(cond[1], cond[2]);
                items.add(item);
            }
        }

        if (!flag) {
            result.param = Response.ERR_PARAM;
            return result;
        }
        if (!items.isEmpty()) {
            int ret = goodsService.decConsume(playerId, items, LogConsume.ACTIVITY_OPEN);
            if (Response.SUCCESS != ret) {
                result.param = ret;
                return result;
            }
        }

        List<Integer> openActivity = Lists.newArrayList(activityId);
        List<ActivityTask> openActivityTasks = Lists.newArrayList();

        List<ActivityTaskCfg> taskCfgs = ActivityTasks.get(activityId);
        if (taskCfgs == null) {
            logger.error("活动配置有错误~~~~~~ 活动ID = " + activityId);
        }

        for (ActivityTaskCfg taskCfg : taskCfgs) {
            if (!data.getActivityTasks().containsKey(taskCfg.id)) {
                ActivityTask at = createActivityTask(taskCfg);
                checkActivityTaskUpdate(data, at);
                data.getActivityTasks().put(at.getId(), at);
                //checkActivityTaskState(openActivityTasks, data, at);
                openActivityTasks.add(at);
            }
        }

        pushActivityOpen(playerId, openActivity, openActivityTasks);
        result.param = Response.SUCCESS;
        return result;
    }


    /**
     * 推送状态更新
     *
     * @param playerId
     * @param tasks
     */
    private void pushActivityUpdate(int playerId, List<ActivityTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        ListParam listParam = new ListParam();
        listParam.params = Lists.newArrayList();
        for (ActivityTask at : tasks) {
            listParam.params.add(at.toProto());
        }
        SessionManager.getInstance().sendMsg(CMD_ACTIVITY_TASK_UPDATE, listParam, playerId);
    }


    /**
     * 活动关闭
     *
     * @param closeActivity
     */
    private void pushActivityClose(List<Integer> closeActivity, int playerId) {
        if (closeActivity.isEmpty()) {
            return;
        }
        ListParam listParam = new ListParam();
        listParam.params = Lists.newArrayList();
        for (int id : closeActivity) {
            IntParam param = new IntParam();
            param.param = id;
            listParam.params.add(param);
        }
        if (playerId == 0) {
            SessionManager.getInstance().sendMsgToAll(CMD_ACTIVITY_CLOSE, listParam);
        } else {

            SessionManager.getInstance().sendMsg(CMD_ACTIVITY_CLOSE, listParam, playerId);
        }
    }


    /**
     * 活动开启
     *
     * @param playerId
     * @param openActivity
     * @param tasks
     */
    private void pushActivityOpen(int playerId, List<Integer> openActivity, List<ActivityTask> tasks) {
        if (openActivity.isEmpty() && tasks.isEmpty()) {
            return;
        }
        ActivityInfo vo = new ActivityInfo();
        vo.id = Lists.newArrayList();
        vo.tasks = Lists.newArrayList();
        for (int id : openActivity) {
            vo.id.add(id);
        }
        for (ActivityTask at : tasks) {
            vo.tasks.add(at.toProto());
        }

        if (playerId == 0) {
            SessionManager.getInstance().sendMsgToAll(CMD_ACTIVITY_OPEN, vo);
        } else {
            SessionManager.getInstance().sendMsg(CMD_ACTIVITY_OPEN, vo, playerId);
        }
    }
}
