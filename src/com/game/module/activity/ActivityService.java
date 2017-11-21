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
import com.game.util.TimerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class ActivityService implements InitHandler {
    private static Logger logger = Logger.getLogger(ActivityService.class);

    public static final ThreadLocal<SimpleDateFormat> DateFormat = ThreadLocal.withInitial(new Supplier<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    });

    //开启的活动
    public static final Map<Integer, ActivityCfg> OpenActivitys = Maps.newConcurrentMap();
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
            doCheckActivity();
        } catch (Exception e) {
            ServerLogger.err(e, "活动异常");
        }
        Calendar c = Calendar.getInstance();
        int second = c.get(Calendar.SECOND);
        //每分钟检测活动状态
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次
            @Override
            public void run() {
                try {
                    doCheckActivity();
                } catch (Exception e) {
                    ServerLogger.err(e, "活动定时器异常");
                }
            }
        }, 60 - second, 60, TimeUnit.SECONDS);

        //每分钟检测活动状态
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次
            @Override
            public void run() {
                try {
                    doCheckActivityComplete(playerService.getPlayerDatas().values());
                } catch (Exception e) {
                    ServerLogger.err(e, "活动定时器异常");
                }
            }
        }, 60, 60, TimeUnit.SECONDS);

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
    private void doCheckActivityComplete(Collection<PlayerData> players) {
        for (PlayerData data : players) {
            for (ActivityTask at : data.getActivityTasks().values()) {
                if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TIME) {
                    ActivityTaskCfg cfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                    int beginHour = cfg.Conds[0][1];
                    int endHour = cfg.Conds[0][2];
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (beginHour <= hour && endHour > hour) {
                        if (at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                            at.setState(ActivityConsts.ActivityState.T_FINISH);
                        }
                    } else if (endHour < hour) { //时间已过，进行补领判断
                        if (at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                            at.setState(ActivityConsts.ActivityState.T_AGAIN_AWARD);
                        }
                    }
                }
            }
        }
    }

    private void doCheckActivityComplete(PlayerData data) {
        doCheckActivityComplete(Lists.newArrayList(data));
    }

    private void doCheckActivity() throws Exception {
        Calendar c = Calendar.getInstance();
        List<ActivityCfg> openActivitys = Lists.newArrayList();
        List<Integer> closeActivitys = Lists.newArrayList();

        for (Object obj : GameData.getConfigs(ActivityCfg.class)) {
            ActivityCfg cfg = (ActivityCfg) obj;

            if (cfg.BeginTime != null && !"".equals(cfg.BeginTime)) {
                Date beginDate = DateFormat.get().parse(cfg.BeginTime);
                if (c.getTime().before(beginDate)) { //还未开启
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.EndTime != null && !"".equals(cfg.EndTime)) {
                Date beginDate = DateFormat.get().parse(cfg.EndTime);
                if (c.getTime().after(beginDate)) { //活动结束
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.WeekTime != null) {
                int week = c.get(Calendar.DAY_OF_WEEK) - 1;
                if (week == 0) {
                    week = 7;
                }

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
                int hour = c.get(Calendar.HOUR_OF_DAY);
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

        if (!openActivitys.isEmpty()) {
            List<ActivityTask> openActivityTasks = Lists.newArrayList();
            List<Integer> openActivityIds = Lists.newArrayList();
            for (ActivityCfg cfg : openActivitys) {
                openActivityIds.add(cfg.id);
                List<ActivityTaskCfg> list = ActivityTasks.get(cfg.id);
                if (list != null) {
                    for (ActivityTaskCfg taskCfg : list) {
                        ActivityTask at = createActivityTask(taskCfg);
                        openActivityTasks.add(at);
                    }
                }
            }
        }

        if (!closeActivitys.isEmpty()) {
            pushActivityClose(closeActivitys);
        }
    }

    /**
     * 创建一个新任务
     *
     * @param taskCfg
     * @return
     */
    private ActivityTask createActivityTask(ActivityTaskCfg taskCfg) {
        int targetCount = 0;
        int taskType = taskCfg.Conds[0][0];
        if (taskType != ActivityConsts.ActivityTaskCondType.T_TIME) {
            targetCount = taskCfg.Conds[0][1];
        }
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
    public void completeActivityTask(int playerId, int taskCondType, int value, int updateType) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask at : data.getActivityTasks().values()) {
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
        pushActivityUpdate(playerId, tasks);
    }


    /**
     * 检测是否有新的活动开启
     *
     * @param playerId
     */
    public void checkNewActivity(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (ActivityCfg cfg : OpenActivitys.values()) {
            if (cfg.OpenType == ActivityConsts.ActivityOpenType.T_HANDLE) continue;
            List<ActivityTaskCfg> list = ActivityTasks.get(cfg.id);
            for (ActivityTaskCfg taskCfg : list) {
                if (!data.getActivityTasks().containsKey(taskCfg.id)) {
                    ActivityTask at = createActivityTask(taskCfg);
                    if (at.getCond().checkComplete()) {//默认是否完成
                        at.setState(ActivityConsts.ActivityState.T_FINISH);
                    }
                    data.getActivityTasks().put(at.getId(), at);
                }
            }
        }

        doCheckActivityComplete(data);
    }

    /**
     * 重置
     *
     * @param playerId
     */
    public void dailyRest(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (ActivityTask at : data.getActivityTasks().values()) {
            if (at.getResetType() == 1) {
                at.cleanup();
            }
        }
    }

    /**
     * 获取活动任务列表
     */
    public ActivityInfo getOpenActivitys(int playerId) {
        ActivityInfo result = new ActivityInfo();
        result.id = Lists.newArrayList();
        result.tasks = Lists.newArrayList();
        PlayerData data = playerService.getPlayerData(playerId);
        for (ActivityCfg cfg : OpenActivitys.values()) {
            result.id.add(cfg.id);
        }
        for (ActivityTask at : data.getActivityTasks().values()) {
            result.tasks.add(at.toProto());
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
        ActivityTaskCfg config = ConfigData.getConfig(ActivityTaskCfg.class, taskId);
        goodsService.addRewards(playerId, config.Rewards, LogConsume.ACTIVITY_REWARD);

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
    public IntParam getAwardAgain(int playerId, int taskId) {
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
        goodsService.addRewards(playerId, config.Rewards, LogConsume.ACTIVITY_REWARD);

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
            ActivityTask at = createActivityTask(taskCfg);
            data.getActivityTasks().put(at.getId(), at);
            openActivityTasks.add(at);
        }

        pushActivityOpen(playerId, openActivity, openActivityTasks);
        result.param = Response.SUCCESS;
        return result;
    }


    /**
     * 推送
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
    private void pushActivityClose(List<Integer> closeActivity) {
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
        SessionManager.getInstance().sendMsgToAll(CMD_ACTIVITY_CLOSE, listParam);
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
