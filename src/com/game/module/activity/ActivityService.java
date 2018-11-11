package com.game.module.activity;

import com.game.data.*;
import com.game.event.InitHandler;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.fashion.FashionService;
import com.game.module.goods.EquipService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.pet.PetService;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.skill.SkillService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.activity.ActivityInfo;
import com.game.params.pet.PetGetRewardVO;
import com.game.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityService implements InitHandler {
    private static Logger logger = Logger.getLogger(ActivityService.class);
    //开启的活动
    public static final Map<Integer, ActivityCfg> OpenActivitys = Maps.newConcurrentMap();
    //活动对任务

    private static final int CMD_ACTIVITY_OPEN = 8004; //活动开启
    private static final int CMD_ACTIVITY_TASK_UPDATE = 8006; //任务更新
    private static final int CMD_ACTIVITY_CLOSE = 8005; //关闭
    private static final int ACTIVITY_TIME_BAG_ID = Integer.MAX_VALUE; //关闭

    @Autowired
    private TimerService timerService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private TitleService titleService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private MailService mailService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private PetService petService;
    @Autowired
    private RandomRewardService randomRewardService;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private PlayerCalculator playerCalculator;

    @Override
    public void handleInit() {
        try {
            doScheduleCheckActivity();
        } catch (Exception e) {
            ServerLogger.err(e, "活动异常");
        }

        LocalDateTime dateTime = LocalDateTime.now();
        int second = dateTime.getSecond();
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次，活动开关
            @Override
            public void run() {
                try {
                    doScheduleCheckActivity();
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
                    doScheduleCheckActivityTask(playerService.getPlayerDatas().values(), true);
                } catch (Exception e) {
                    ServerLogger.err(e, "玩家活动定时器异常");
                }
            }
        }, 120, 60, TimeUnit.SECONDS);
    }

    /**
     * 定时检测活动任务状态
     *
     * @param players
     */
    private void doScheduleCheckActivityTask(Collection<PlayerData> players, boolean toCli) {
        List<ActivityTask> updateActivityList = Lists.newArrayList();
        for (PlayerData data : players) {
            updateActivityList.clear();
            for (ActivityTask at : data.getAllActivityTasks()) {
                if (checkTimeActivityUpdate(at)) {
                    updateActivityList.add(at);
                }
            }
            if (toCli) {
                pushActivityUpdate(data.getPlayerId(), updateActivityList);
            }
        }
    }

    /**
     * 定时检测活动开启关闭
     *
     * @throws Exception
     */
    private void doScheduleCheckActivity() throws Exception {
        List<ActivityCfg> openActivitys = Lists.newArrayList();
        List<Integer> closeActivitys = Lists.newArrayList();
        LocalDateTime nowDate = LocalDateTime.now();
        for (Object obj : GameData.getConfigs(ActivityCfg.class)) {
            ActivityCfg cfg = (ActivityCfg) obj;

            if (cfg.BeginTime != null && !"".equals(cfg.BeginTime)) {
                LocalDateTime beginDate = LocalDateTime.parse(cfg.BeginTime, TimeUtil.formatter);
                if (nowDate.isBefore(beginDate)) { //还未开启
                    closeActivity(cfg.id, closeActivitys);
                    continue;
                }
            }

            if (cfg.EndTime != null && !"".equals(cfg.EndTime)) {
                LocalDateTime beginDate = LocalDateTime.parse(cfg.EndTime, TimeUtil.formatter);
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
            playerAddNewActivity(openActivitys);
        }
        if (!closeActivitys.isEmpty()) { //关闭的活动
            pushActivityClose(closeActivitys, 0);
        }
    }


    /**
     * 检查区间时间活动状态
     *
     * @param at
     * @return
     */
    private boolean checkTimeActivityUpdate(ActivityTask at) {
        boolean bUpdate = false;
        if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_ENERGY) { //体力活动时间区间类型单独处理
            ActivityTaskCfg cfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
            float beginHour = cfg.Conds[0][1];
            float endHour = cfg.Conds[0][2];

            //int hour = LocalDateTime.now().getHour();
            Calendar c = Calendar.getInstance();
            // 北京时区
            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            // 时区转换
            c.setTimeZone(tz);
            int hour = c.get(Calendar.HOUR_OF_DAY);

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
        }
        return bUpdate;
    }

    /**
     * 检查任务当前状态
     *
     * @param data
     * @param at
     * @return
     */
    private boolean checkActivityTaskUpdate(PlayerData data, ActivityTask at) {
        boolean bUpdate = false;
        if (at.isRewardFlag()) { //已经奖励完，则不检测
            return bUpdate;
        }
        ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
        if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_ENERGY) { //体力时间区间类型单独处理
            bUpdate = checkTimeActivityUpdate(at);
        } else { //其他类型
            if (at.getState() == ActivityConsts.ActivityState.T_FINISH) { //已经完成的，则不检测
                return bUpdate;
            }
            at.getCond().setTargetValue(taskCfg.Conds[0][1]);
            if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_LEVEL_UP
                    || at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_GROW_FUND) { //等级类型
                Player player = playerService.getPlayer(data.getPlayerId());
                at.getCond().setValue(player.getLev());
                bUpdate = true;
            } else if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_SEVEN_DAYS) { //7天登录
                at.getCond().setValue(data.getSevenDays());
                bUpdate = false;
            } else if (at.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TWO_DAYS) { //2天登录
                at.getCond().setValue(data.getTwoDays());
                bUpdate = false;
            }
            if (at.getCond().checkComplete()) {
                at.setState(ActivityConsts.ActivityState.T_FINISH);
                bUpdate = true;
            }
        }
        return bUpdate;
    }

    /**
     * 创建一个新任务
     *
     * @param taskCfg
     * @return
     */
    private ActivityTask createActivityTask(ActivityTaskCfg taskCfg) {
        float taskType = taskCfg.Conds[0][0];
        float targetCount = taskCfg.Conds[0][1];
        return new ActivityTask(taskCfg.id, taskCfg.ActivityId, targetCount, (int) taskType);
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
    public List<ActivityTask> completeActivityTask(int playerId, int taskCondType, float value, int updateType, boolean toCli) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask at : data.getAllActivityTasks()) {
            if (at.getState() != ActivityConsts.ActivityState.T_UN_FINISH) {
                //全服登录人数特殊处理
                ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                if (taskCfg != null && taskCfg.Conds[0][0] == ActivityConsts.ActivityTaskCondType.T_FULL_SERVICE_ATTENDANCE) {
                    at.getCond().setValue(value);
                }
                continue;
            }
            int condType = at.getCond().getCondType();
            if (condType == taskCondType && at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                if (updateType == ActivityConsts.UpdateType.T_ADD) {
                    at.getCond().setValue(at.getCond().getValue() + value);
                } else if (updateType == ActivityConsts.UpdateType.T_VALUE) {
                    at.getCond().setValue(value);
                }

                //条件读表
                ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                if (taskCfg != null) {
                    at.getCond().setTargetValue(taskCfg.Conds[0][1]);
                    if (at.getCond().checkComplete()) {
                        if (completeAndSendReward(playerId, tasks, at, taskCfg, true)) {
                            //当今日充值达到300钻时连锁追加累计充值一次
                            GlobalConfig globalParam = ConfigData.globalParam();
                            if (globalParam != null && taskCondType == ActivityConsts.ActivityTaskCondType.T_DAILY_RECHARGE_DIAMONDS && at.getCond().getTargetValue() == globalParam.Dailyrecharge) {
                                //增加累计充值次数
                                data.setAddUpRechargeDiamondsTimes(data.getAddUpRechargeDiamondsTimes() + 1);
                                Map<Integer, Integer> typeNumberMap = new HashMap<>();
                                for (ActivityTask activityTask : data.getAllActivityTasks()) {
                                    if (activityTask.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TIMED_MONEY_DIAMONDS) {
                                        ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, activityTask.getId());
                                        if (activityTaskCfg != null) {
                                            activityTask.setFinishNum(activityTask.getFinishNum() + 1);
                                            typeNumberMap.put((int) activityTaskCfg.Conds[0][2], data.getAddUpRechargeDiamondsTimes() * globalParam.Dailyrecharge);
                                        }
                                    }
                                }
                                completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_TIMED_MONEY_DIAMONDS, true, typeNumberMap, false);
                            }
                            integral(playerId, at);//巡礼活动
                        } else {
                            continue;
                        }
                    }
                    tasks.add(at);
                }
            }
        }
        if (toCli) {
            pushActivityUpdate(playerId, tasks);
        }

        return tasks;
    }

    /**
     * 登录检测是否有新的活动开启
     *
     * @param playerId
     */
    public void onLogin(int playerId) {
        checkOrAddNewActivity(playerId, OpenActivitys.values(), null, null);
    }

    /**
     * 玩家新增活动
     *
     * @param openActivitys
     */
    private void playerAddNewActivity(List<ActivityCfg> openActivitys) {
        List<Integer> openActivity = Lists.newArrayList();
        List<ActivityTask> openActivityTasks = Lists.newArrayList();
        for (int playerId : playerService.getPlayerDatas().keySet()) {
            openActivity.clear();
            openActivityTasks.clear();
            checkOrAddNewActivity(playerId, openActivitys, openActivity, openActivityTasks);
        }
    }

    /**
     * 活动任务状态检查 或者 新增活动及任务
     *
     * @param playerId
     * @param openActivitys
     * @param openActivity
     * @param openActivityTasks
     */
    private void checkOrAddNewActivity(int playerId, Collection<ActivityCfg> openActivitys,
                                       List<Integer> openActivity, List<ActivityTask> openActivityTasks) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (ActivityCfg cfg : openActivitys) {
            if (openActivity != null) {
                openActivity.add(cfg.id);
            }

            if (cfg.OpenType == ActivityConsts.ActivityOpenType.T_HANDLE)
                continue;
            List<ActivityTaskCfg> list = ConfigData.ActivityTasks.get(cfg.id);
            for (ActivityTaskCfg taskCfg : list) {
                if (cfg.ActivityType == ActivityConsts.ActivityType.T_ENERGY) { //体力活动，登录检测
                    ActivityTask at = data.getActivityTask(taskCfg.id);
                    if (at != null) {
                        checkActivityTaskUpdate(data, at);
                    }
                }
                if (!data.hasActivityTask(taskCfg.id)) {
                    ActivityTask at = createActivityTask(taskCfg);
                    if (at.getCond().checkComplete()) {//默认是否完成
                        at.setState(ActivityConsts.ActivityState.T_FINISH);
                    }
                    data.addActivityTask(at.getId(), at);
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
     * 日常重置
     *
     * @param playerId
     */
    public void dailyRest(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        data.setOnlineTime(0);
        data.setDailyRecharge(1);//重置每日充值
        List<ActivityTask> list = Lists.newArrayList();
        boolean foundCardActivity = false;
        for (ActivityTask at : data.getAllActivityTasks()) {
            ActivityTaskCfg cfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
            ActivityCfg cfg2 = ConfigData.getConfig(ActivityCfg.class, at.getActivityId());
            if (cfg != null) {
                if (cfg.ResetType == ActivityConsts.ActivityTaskResetType.T_DAILY) {
                    at.cleanup();
                    list.add(at);
                    boolean isCarActivity = cfg2.ActivityType == ActivityConsts.ActivityType.T_CARD;
                    if (cfg2.ActivityType == ActivityConsts.ActivityType.T_GIFT_BOX
                            || isCarActivity) {
                        at.setFinishNum(0);
                        if (isCarActivity) {
                            foundCardActivity = isCarActivity;
                        }
                    }
                }
            }
        }

        if (foundCardActivity) {
            refreshDestinyCard(playerId);
        }

        //检测登录活动
        List<ActivityTask> sevenTasks = completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_SEVEN_DAYS, data.getSevenDays(), ActivityConsts.UpdateType.T_VALUE, false);
        if (!sevenTasks.isEmpty()) {
            list.addAll(sevenTasks);
        }

        List<ActivityTask> twoTasks = completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_TWO_DAYS, data.getTwoDays(), ActivityConsts.UpdateType.T_VALUE, false);
        if (!twoTasks.isEmpty()) {
            list.addAll(twoTasks);
        }

        pushActivityUpdate(playerId, list);
    }

    /**
     * 获取活动任务列表
     */
    public ActivityInfo getPlayerActivitys(int playerId, List<Integer> idList) {
        ActivityInfo result = new ActivityInfo();
        result.id = Lists.newArrayList();
        result.tasks = Lists.newArrayList();
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);

        Collection<ActivityCfg> activityCfgs = Lists.newArrayList();
        if (idList != null && idList.size() > 0) {
            for (int id : idList) {
                if (OpenActivitys.containsKey(id)) {
                    activityCfgs.add(OpenActivitys.get(id));
                }
            }
        } else {
            activityCfgs = OpenActivitys.values();
        }

        for (ActivityCfg cfg : activityCfgs) {

            //暂时关闭多余的2个宠物福利活动
            if (cfg.id == 33 || cfg.id == 34) {
                continue;
            }

            if (cfg.ActivityType == ActivityConsts.ActivityType.T_SEVEN_DAYS) { //7天登录
                if (data.getSevenDays() > cfg.Param0) {
                    continue;
                }
            } else if (cfg.ActivityType == ActivityConsts.ActivityType.T_TWO_DAYS) { //2天登录){
                if (data.getTwoDays() > cfg.Param0) {
                    continue;
                }
            } else if (cfg.ActivityType == ActivityConsts.ActivityType.T_NEW_ROLE   //新手礼包
                    || cfg.ActivityType == ActivityConsts.ActivityType.T_LEVEL_UP   //冲级活动
                    || cfg.ActivityType == ActivityConsts.ActivityType.T_GROW_FUND  //成长基金
                    || cfg.ActivityType == ActivityConsts.ActivityType.T_FIRST_RECHARGE //首充
                    || cfg.ActivityType == ActivityConsts.ActivityType.T_TIMED_BAG //限时礼包
                    || cfg.ActivityType == ActivityConsts.ActivityType.T_SPECIAL_BAG) {//特价礼包
                List<ActivityTaskCfg> list = ConfigData.ActivityTasks.get(cfg.id);
                if (list == null) {
                    continue;
                }
                boolean bClose = true;
                for (ActivityTaskCfg taskCfg : list) {
                    ActivityTask task = data.getActivityTask(taskCfg.id);
                    if (cfg.ActivityType == ActivityConsts.ActivityType.T_TIMED_BAG) {//限时礼包
                        if (task != null && task.getState() == ActivityConsts.ActivityState.T_UN_FINISH && (System.currentTimeMillis() - task.getTimedBag()) / 1000 / 60 <= cfg.Param0) {
                            bClose = false;
                        }
                    } else if (cfg.ActivityType == ActivityConsts.ActivityType.T_SPECIAL_BAG) {//特价礼包
                        if (task != null && task.getState() == ActivityConsts.ActivityState.T_UN_FINISH && player.getLev() >= cfg.Conds[0][1] && player.getLev() < cfg.Conds[0][2]) {
                            bClose = false;
                        }
                    } else {
                        //检测是否有未领取奖励的或者未领取任务的活动(task == null)
                        bClose = task != null && bClose && task.getState() == ActivityConsts.ActivityState.T_AWARD;
                    }
                    if (!bClose) break;
                }
                if (bClose) {
                    continue;
                }
            }
            result.id.add(cfg.id);
        }

        for (ActivityTask at : data.getAllActivityTasks()) {
            if (result.id.contains(at.getActivityId())) {
                ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                if (taskCfg != null) {
                    at.getCond().setCondType((int) taskCfg.Conds[0][0]);
                    at.getCond().setTargetValue(taskCfg.Conds[0][1]);

                    //更新全服登录人数
                    if (taskCfg.Conds[0][0] == ActivityConsts.ActivityTaskCondType.T_FULL_SERVICE_ATTENDANCE) {
                        SerialData serialData = serialDataService.getData();
                        if (serialData == null) {
                            ServerLogger.warn("序列化数据不存在");
                            continue;
                        }
                        completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_FULL_SERVICE_ATTENDANCE, serialData.getFullServiceAttendance().get(), ActivityConsts.UpdateType.T_VALUE, true);
                    }

                    //更新奇遇宝箱购买次数
                    updateAdventureBoxNumber(at);

                    result.tasks.add(at.toProto());
                }
            }
        }

        // ---
//        result.id.add(900); // 加入礼品兑换界面活动
        return result;
    }

    /**
     * 领取奖励
     *
     * @param playerId
     * @param taskId
     * @return
     */
    public Int2Param getActivityAwards(int playerId, int taskId) {
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);
        ActivityTask task = data.getActivityTask(taskId);
        Int2Param result = new Int2Param();
        if (task == null) {
            result.param1 = Response.ERR_PARAM;
            result.param2 = taskId;
            return result;
        }
        ActivityTaskCfg config = ConfigData.getConfig(ActivityTaskCfg.class, taskId);
        ActivityCfg activityCfg = ConfigData.getConfig(ActivityCfg.class, config.ActivityId);
        task.getCond().setTargetValue(config.Conds[0][1]); //条件读取配置
        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_ONLINE_TIME) { //在线活动奖励领取，状态设置
            long loginTime = player.onlineTime;
            int passTime = (int) ((System.currentTimeMillis() - loginTime) / 1000);
            task.getCond().setValue(passTime + data.getOnlineTime());
            if (task.getCond().checkComplete()) {
                task.setState(ActivityConsts.ActivityState.T_FINISH);
            }
        }

        if (task.getState() != ActivityConsts.ActivityState.T_FINISH) {
            result.param1 = Response.ERR_PARAM;
            result.param2 = taskId;
            return result;
        }

        task.setState(ActivityConsts.ActivityState.T_AWARD);
        task.setRewardFlag(true);

        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_ONLINE_TIME) {
            player.onlineTime = System.currentTimeMillis();
            data.setOnlineTime(0);
        }

        List<GoodsEntry> itemList = Lists.newArrayList();
        boolean activeFashion = false;
        for (int[] arr : config.Rewards) {
            if (arr.length == 2) {
                GoodsEntry goodsEntry = new GoodsEntry(arr[0], arr[1]);
                itemList.add(goodsEntry);
            } else if (arr.length == 3) {
                if (player.getVocation() == arr[2]) {
                    GoodsEntry goodsEntry = new GoodsEntry(arr[0], arr[1]);
                    itemList.add(goodsEntry);
                }
            }

            //时装激活
            GoodsConfig goodsConfig = ConfigData.getConfig(GoodsConfig.class, arr[0]);
            if (goodsConfig == null) {
                ServerLogger.warn("物品不存在，物品ID=" + arr[0]);
                continue;
            }
            if (goodsConfig.type == Goods.FASHION) {
                activeFashion = true;
            }
        }
        goodsService.addRewards(playerId, itemList, LogConsume.ACTIVITY_REWARD);
        if (activeFashion) {
            playerCalculator.calculate(player);
        }

        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_SEVEN_DAYS) {
            data.setSevenDays(data.getSevenDays() + 1);
        }

        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_TWO_DAYS) {
            data.setTwoDays(data.getTwoDays() + 1);
        }

        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_ENERGY) {
            data.setEnergyCount(data.getEnergyCount() + 1);
            taskService.doTask(playerId, Task.TYPE_ENERGY, data.getEnergyCount());
        }

        ActivityCfg cfg = ConfigData.getConfig(ActivityCfg.class, task.getActivityId());

        // 0元礼包
        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_ZERO_GIFTBAG) {
            if (cfg != null) {
                Map<Integer, Map<Integer, Long>> laterDayRewardMap = serialDataService.getData().getLaterDayRewardMap();
                if (laterDayRewardMap == null) {
                    laterDayRewardMap = new ConcurrentHashMap<>();
                }
                Map<Integer, Long> integerLongMap = laterDayRewardMap.get(task.getId());
                if (integerLongMap == null) {
                    integerLongMap = new ConcurrentHashMap<>();
                }
                integerLongMap.put(playerId, TimeUtil.getTodayBeginTime());
                laterDayRewardMap.put(task.getId(), integerLongMap);

                //清除之前存的数据
                if (laterDayRewardMap.get(cfg.id) != null) {
                    laterDayRewardMap.remove(cfg.id);
                }
            }
        }

        if (cfg.ActivityType == ActivityConsts.ActivityType.T_NEW_ROLE) {
            pushActivityClose(Lists.newArrayList(task.getActivityId()), playerId);
        }
        pushActivityUpdate(playerId, Lists.newArrayList(task));
        result.param1 = Response.SUCCESS;
        result.param2 = taskId;
        return result;
    }


    /**
     * 补领奖励
     *
     * @param playerId
     * @param taskId
     * @return 1
     */
    public IntParam fixedActivityAwards(int playerId, int taskId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityTask task = data.getActivityTask(taskId);
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

        ActivityCfg activityCfg = ConfigData.getConfig(ActivityCfg.class, config.ActivityId);
        if (activityCfg.ActivityType == ActivityConsts.ActivityType.T_ENERGY) {
            data.setEnergyCount(data.getEnergyCount() + 1);
            taskService.doTask(playerId, Task.TYPE_ENERGY, data.getEnergyCount());
        }
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
    public Int2Param openActivity(int playerId, int activityId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, activityId);
        Int2Param result = new Int2Param();
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
            } else if (type == ActivityConsts.ActivityCondType.T_BUY_CARD) {

            }
        }

        if (!flag) {
            result.param1 = Response.ERR_PARAM;
            return result;
        }
        if (!items.isEmpty()) {
            int ret = goodsService.decConsume(playerId, items, LogConsume.ACTIVITY_OPEN);
            if (Response.SUCCESS != ret) {
                result.param1 = ret;
                return result;
            }
        }

        List<Integer> openActivity = Lists.newArrayList(activityId);
        List<ActivityTask> openActivityTasks = Lists.newArrayList();

        List<ActivityTaskCfg> taskCfgs = ConfigData.ActivityTasks.get(activityId);
        if (taskCfgs == null) {
            logger.error("活动配置有错误~~~~~~ 活动ID = " + activityId);
            result.param1 = Response.ERR_PARAM;
            return result;
        }

        for (ActivityTaskCfg taskCfg : taskCfgs) {
            if (!data.hasActivityTask(taskCfg.id)) {
                ActivityTask at = createActivityTask(taskCfg);
                checkActivityTaskUpdate(data, at);
                data.addActivityTask(at.getId(), at);
                openActivityTasks.add(at);
            }
        }

        pushActivityOpen(playerId, openActivity, openActivityTasks);
        result.param1 = Response.SUCCESS;
        result.param2 = activityId;

        //装备投资
        equipService.equipmentInvestment(playerId, data);

        //卡片投资
        skillService.cardInvestment(playerId);

        //宠物投资
        petService.petInvestment(playerId);

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

    /**
     * 根据等级开启礼包
     *
     * @param playerId 玩家id
     */
    public void startBagByLevel(int playerId) {
        //获取玩家信息
        Player player = playerService.getPlayer(playerId);
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (player == null || playerData == null) {
            return;
        }

        //开启活动
        List<Integer> openActivity = Lists.newArrayList();
        List<ActivityTask> tasks = Lists.newArrayList();

        Collection<Object> configs = ConfigData.getConfigs(ActivityCfg.class);

        //查找活动表
        for (Object config : configs) {
            ActivityCfg activityCfg = (ActivityCfg) config;

            if (activityCfg.Conds != null && activityCfg.Conds[0][0] == ActivityConsts.ActivityCondType.T_LEVEL) {
                List<ActivityTaskCfg> taskCfgs = ConfigData.ActivityTasks.get(activityCfg.id);
                //查找活动任务表
                for (ActivityTaskCfg cfg : taskCfgs) {
                    LocalDateTime nowDate = LocalDateTime.now();
                    if (activityCfg.BeginTime != null && !"".equals(activityCfg.BeginTime)) {
                        LocalDateTime beginDate = LocalDateTime.parse(activityCfg.BeginTime, TimeUtil.formatter);
                        if (nowDate.isBefore(beginDate)) { //还未开启
                            continue;
                        }
                    }

                    if (activityCfg.EndTime != null && !"".equals(activityCfg.EndTime)) {
                        LocalDateTime beginDate = LocalDateTime.parse(activityCfg.EndTime, TimeUtil.formatter);
                        if (nowDate.isAfter(beginDate)) { //活动结束
                            continue;
                        }
                    }
                    //检查等级条件，开启等级相关礼包
                    if (player.getLev() >= activityCfg.Conds[0][1]) {
                        ActivityTask activityTask = playerData.getActivityTask(cfg.id);
                        if (activityTask != null) {
                            continue;
                        }
                        activityTask = createActivityTask(cfg);
                        tasks.add(activityTask);
                        playerData.addActivityTask(cfg.id, activityTask);
                        openActivity.add(activityCfg.id);
                    }
                }
            }
        }

        pushActivityOpen(playerId, openActivity, tasks);//开启活动
    }

    /**
     * 根据一次充值金额获取奖励
     *
     * @param playerId 玩家id
     * @param rmb      充值金额
     */
    public void onceRecharge(int playerId, float rmb) {
        //获取玩家信息
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return;
        }
        //活动任务表
        ActivityTask temp = null;
        for (ActivityTask task : playerData.getAllActivityTasks()) {
            if (task.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TIMED_ONCE && task.getState() == ActivityConsts.ActivityState.T_UN_FINISH && task.getCond().getTargetValue() <= rmb) {
                if (temp == null || temp.getCond().getTargetValue() < task.getCond().getTargetValue()) {
                    temp = task;
                }
            }
        }
        if (temp != null) {
            temp.getCond().setValue(rmb);
            temp.setState(ActivityConsts.ActivityState.T_FINISH);
            pushActivityUpdate(playerId, Lists.newArrayList(temp));
        }
    }

    /**
     * 每日充值
     *
     * @param playerId 玩家id
     * @param rmb      充值金额
     */
    public void dailyRecharge(int playerId, float rmb) {
        //获取玩家信息
        PlayerData data = playerService.getPlayerData(playerId);
        if (data == null) {
            return;
        }
        if (data.getDailyRecharge() == ActivityConsts.ActivityState.T_UN_FINISH) {
            for (ActivityTask task : data.getAllActivityTasks()) {
                if (task.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_DAILY_RECHARGE && task.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                    task.setState(ActivityConsts.ActivityState.T_FINISH);
                    pushActivityUpdate(playerId, Lists.newArrayList(task));
                    break;
                }
            }
            data.setDailyRecharge(ActivityConsts.ActivityState.T_FINISH);
        }
    }

    /**
     * 购买活动任务(用钻石购买特惠礼包)
     *
     * @param playerId 玩家id
     * @param taskId   任务id
     */
    public Int2Param buyActivityTask(int playerId, int taskId) {
        Int2Param result = new Int2Param();
        result.param1 = Response.ACTIVITY_DONT_FINISH;
        PlayerData data = playerService.getPlayerData(playerId);
        ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, taskId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask task : data.getAllActivityTasks()) {
            if (task.getId() == taskId && task.getFinishNum() < taskCfg.Param1) {
                //奇遇宝箱
                ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, task.getActivityId());
                if (config != null && config.ActivityType == ActivityConsts.ActivityType.T_ADVENTURE_BOX) {
                    if (!buyAdventureBoxNumber(task)) {
                        continue;
                    }
                }

                if (task.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_BUY_DIAMOND) {//消耗物品
                    List<GoodsEntry> items = Lists.newArrayList();
                    for (int i = 2; i < taskCfg.Conds[0].length; i = i + 2) {
                        GoodsEntry item = new GoodsEntry((int) taskCfg.Conds[0][i - 1], (int) taskCfg.Conds[0][i]);
                        items.add(item);
                    }
                    if (!items.isEmpty()) {
                        int ret = goodsService.decConsume(playerId, items, LogConsume.ACTIVITY_CONSUME);
                        if (Response.SUCCESS != ret) {
                            result.param1 = ret;
                            return result;
                        }
                    }
                }

                goodsService.addRewards(playerId, taskCfg.Rewards, LogConsume.ACTIVITY_REWARD);
                task.setFinishNum(task.getFinishNum() + 1);//完成次数+1`
                tasks.add(task);
                pushActivityUpdate(playerId, tasks);//推送更新
                result.param1 = Response.SUCCESS;
                break;
            }
        }
        result.param2 = taskId;
        return result;
    }

    /**
     * 完成多个条件的活动
     *
     * @param playerId      玩家Id
     * @param taskCondType  活动任务完成条件类型
     * @param toCli         是否推送到客户端
     * @param typeNumberMap 类型数量的集合
     * @param addTime       是否增加完成次数
     */
    public List<ActivityTask> completeActivityTask(int playerId, int taskCondType, boolean toCli, Map<Integer, Integer> typeNumberMap, boolean addTime) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask at : data.getAllActivityTasks()) {
            if (at.getState() != ActivityConsts.ActivityState.T_UN_FINISH) {
                continue;
            }
            int condType = at.getCond().getCondType();
            if (condType == taskCondType && at.getState() == ActivityConsts.ActivityState.T_UN_FINISH) {
                ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                if (taskCfg != null) {
                    if (taskCfg.Conds[0].length >= 3) {
                        //某种品质达到某个数量
                        Integer value = typeNumberMap.get((int) taskCfg.Conds[0][2]);
                        if (value != null) {
                            at.getCond().setValue(value);
                            if (typeNumberMap.get((int) taskCfg.Conds[0][2]) >= taskCfg.Conds[0][1]) {
                                if (completeAndSendReward(playerId, tasks, at, taskCfg, addTime)) {
                                    integral(playerId, at);//巡礼活动
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                }
                tasks.add(at);
            }
        }
        if (toCli) {
            pushActivityUpdate(playerId, tasks);
        }
        return tasks;
    }

    //完成活动并获取奖励
    private boolean completeAndSendReward(int playerId, List<ActivityTask> tasks, ActivityTask at, ActivityTaskCfg taskCfg, boolean addTime) {
        //完成次数+1
        if (addTime) {
            at.setFinishNum(at.getFinishNum() + 1);
        }
        tasks.add(at);
        if (taskCfg.Param1 > 0 && at.getFinishNum() < taskCfg.Param1) {
            goodsService.addRewards(playerId, taskCfg.Rewards, LogConsume.ACTIVITY_REWARD);
            return false;
        }
        at.setState(ActivityConsts.ActivityState.T_FINISH);

        //活动称号
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, at.getActivityId());

        //完成首充才获取首充称号
        if (config.ActivityType == ActivityConsts.ActivityType.T_FIRST_RECHARGE) {
            titleService.complete(playerId, TitleConsts.ACTIVITY, at.getActivityId(), ActivityConsts.UpdateType.T_VALUE);
        }

        //自动领奖
        if (taskCfg.AutoReward == 1) {
            at.setState(ActivityConsts.ActivityState.T_AWARD);
            goodsService.addRewards(playerId, taskCfg.Rewards, LogConsume.ACTIVITY_REWARD);
            at.setRewardFlag(true);
        }
        return true;
    }

    /**
     * 检查活动是否开启
     *
     * @param playerData           玩家数据
     * @param activityTaskCondType 活动类型
     * @return 是否开启
     */
    public boolean checkIsOpen(PlayerData playerData, int activityTaskCondType) {
//        Map<Integer, ActivityTask> activityTasks = playerData.getActivityTasks();
        boolean isOpen = false;
        Collection<ActivityTask> activityTasks = playerData.getAllActivityTasks();
        if (activityTasks != null && !activityTasks.isEmpty()) {
            for (ActivityTask activityTask : activityTasks) {
                if (activityTask.getState() != ActivityConsts.ActivityState.T_UN_FINISH) {
                    continue;
                }
                ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, activityTask.getId());
                if (activityTaskCfg != null && activityTaskCondType == activityTaskCfg.Conds[0][0]) {
                    isOpen = true;
                    break;
                }
            }
        }
        return isOpen;
    }

    /**
     * 定时发送间隔自然日的奖励
     */
    public void doScheduleCheckActivityTask() {
        Map<Integer, Map<Integer, Long>> laterDayRewardMap = serialDataService.getData().getLaterDayRewardMap();
        if (laterDayRewardMap == null || laterDayRewardMap.isEmpty()) {
            return;
        }
        List<Integer> deleteList = new ArrayList<>();//已经发奖需要删除的玩家id集合
        //延时活动激活
        for (Map.Entry<Integer, Map<Integer, Long>> activityMap : laterDayRewardMap.entrySet()) {
            ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, activityMap.getKey());
            if (activityTaskCfg == null) {
                continue;
            }
            Map<Integer, Long> playerMap = activityMap.getValue();
            if (playerMap == null || playerMap.isEmpty()) {
                continue;
            }
            //购买了0元礼包的活动玩家
            for (Map.Entry<Integer, Long> map : playerMap.entrySet()) {
                double passTime = Math.ceil((double) ((System.currentTimeMillis() - map.getValue()) / (1000 * 60 * 60 * 24)));//已经过去的自然日
                //以登录天数作为延后天数进行比较
                if (activityTaskCfg.Conds.length >= 2 && activityTaskCfg.Conds[1][0] == ActivityConsts.ActivityTaskCondType.T_SEVEN_DAYS && (passTime >= activityTaskCfg.Conds[1][1] || passTime < 0)) {
                    List<RewardMailCfg> cfgs = ConfigData.rewardMails.get(MailService.ZERO_GIFTBAG);
                    if (cfgs == null || cfgs.isEmpty()) {
                        return;
                    }
                    //邮件奖励表获取模板
                    for (RewardMailCfg rewardMailCfg : cfgs) {
                        if (activityTaskCfg.Conds[0][0] == rewardMailCfg.rank[0] && activityTaskCfg.Conds[0][1] == rewardMailCfg.rank[1]) {
                            //构造奖励
                            List<GoodsEntry> rewards = new ArrayList<>();
                            GoodsEntry goodsEntry = new GoodsEntry(102, activityTaskCfg.Param0);
                            rewards.add(goodsEntry);
                            //发送奖励
                            mailService.sendSysMail(rewardMailCfg.title, rewardMailCfg.content, rewards, map.getKey(), LogConsume.ZERO_GIFTBAG);
                            deleteList.add(map.getKey());
                            break;
                        }
                    }
                }
            }
            //删除已经发过奖励的玩家id
            if (!deleteList.isEmpty()) {
                for (Integer playerId : deleteList) {
                    playerMap.remove(playerId);
                }
            }
        }
    }

    /**
     * 日常数据重置
     */
    public void resetDailyData() {
        //重置每日登录人数
        SerialData serialData = serialDataService.getData();
        if (serialData == null) {
            ServerLogger.warn("序列化数据不存在");
            return;
        }
        if (serialData.getFullServiceAttendance() == null) {
            ServerLogger.warn("全服登录人数不存在");
            return;
        }
        serialData.getFullServiceAttendance().set(0);
    }

    /**
     * 累计活动的检测与完成
     *
     * @param playerId 玩家id
     * @param type     活动任务类型
     */
    public void completionCumulative(int playerId, int type, int value) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData != null) {
            if (checkIsOpen(playerData, type)) {
                completeActivityTask(playerId, type, value, ActivityConsts.UpdateType.T_ADD, true);
            }
        } else {
            ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
        }
    }

    /**
     * 绝对值活动的检测与完成
     *
     * @param playerId 玩家id
     * @param type     活动任务类型
     * @param value    最终值
     */
    public void absoluteValue(int playerId, int type, int value) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData != null) {
            if (checkIsOpen(playerData, type)) {
                completeActivityTask(playerId, type, value, ActivityConsts.UpdateType.T_VALUE, true);
            }
        } else {
            ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
        }
    }

    /**
     * 巡礼活动
     *
     * @param playerId 玩家id
     * @param type     活动类型
     * @param value    绝对值
     */
    public void tour(int playerId, int type, int... value) {
        if (value.length == 0) {
            completionCumulative(playerId, type, 1);
        } else if (value.length == 1) {
            absoluteValue(playerId, type, value[0]);
        }
    }

    /**
     * 巡礼活动
     *
     * @param playerId 玩家id
     * @param at       任务
     */
    public void integral(int playerId, ActivityTask at) {
        //巡礼活动计算积分
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, at.getActivityId());
        if (config != null && config.ActivityType == ActivityConsts.ActivityType.T_TOUR) {
            completionCumulative(playerId, ActivityConsts.ActivityTaskCondType.T_INTEGRAL, 1);
        }
    }

    /**
     * 更新奇遇宝箱购买次数
     *
     * @param at 活动任务表
     */
    public void updateAdventureBoxNumber(ActivityTask at) {
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, at.getActivityId());
        int id = at.getId();
        if (config == null) {
            ServerLogger.warn("活动不存在，活动Id=" + id);
            return;
        }
        if (config.ActivityType != ActivityConsts.ActivityType.T_ADVENTURE_BOX) {
            return;
        }
        SerialData serialData = serialDataService.getData();
        if (serialData == null) {
            ServerLogger.warn("序列化数据不存在");
            return;
        }
        Map<Integer, Integer> adventureBoxNumber = serialData.getAdventureBoxNumber();
        if (adventureBoxNumber == null) {
            ServerLogger.warn("奇遇宝箱数据错误");
        }
        adventureBoxNumber.put(id, adventureBoxNumber.getOrDefault(id, 0));
        int count = adventureBoxNumber.get(id);
        at.setParam0(count);
    }

    /**
     * 购买奇遇宝箱
     *
     * @param at 活动任务表
     */
    public boolean buyAdventureBoxNumber(ActivityTask at) {
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, at.getActivityId());
        int id = at.getId();
        if (config == null) {
            ServerLogger.warn("活动不存在，活动Id=" + id);
            return false;
        }
        if (config.ActivityType != ActivityConsts.ActivityType.T_ADVENTURE_BOX) {
            return false;
        }
        SerialData serialData = serialDataService.getData();
        if (serialData == null) {
            ServerLogger.warn("序列化数据不存在");
            return false;
        }
        Map<Integer, Integer> adventureBoxNumber = serialData.getAdventureBoxNumber();
        if (adventureBoxNumber == null || adventureBoxNumber.isEmpty()) {
            ServerLogger.warn("奇遇宝箱数据出错");
            return false;
        }
        if (adventureBoxNumber.containsKey(id)) {
            ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
            if (activityTaskCfg == null) {
                ServerLogger.warn("活动任务不存在，活动任务ID=" + at.getId());
                return false;
            }
            if (adventureBoxNumber.get(id) < activityTaskCfg.Param0) {
                adventureBoxNumber.put(id, adventureBoxNumber.get(id) + 1);
                if (activityTaskCfg.AutoReward == ActivityConsts.AutoReward.T_AUTO) {
                    at.setState(ActivityConsts.ActivityState.T_AWARD);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 购买命运卡牌
     *
     * @param playerId 玩家id
     * @param id       活动任务id
     * @return 物品
     */
    public Object buyDestinyCard(int playerId, int id) {
        PetGetRewardVO petGetRewardVO = new PetGetRewardVO();

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在，玩家ID=" + playerId);
            petGetRewardVO.errCode = Response.ERR_PARAM;
            return petGetRewardVO;
        }

        ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, id);
        if (activityTaskCfg == null) {
            ServerLogger.warn("活动任务不存在，任务ID=" + id);
            petGetRewardVO.errCode = Response.ERR_PARAM;
            return petGetRewardVO;
        }
        //设置状态
        for (ActivityTask at : playerData.getActivityTasks().values()) {
            if (id == at.getId() && at.getState() == ActivityConsts.ActivityState.T_AWARD) {
                ServerLogger.warn("已經領獎，任务ID=" + id);
                petGetRewardVO.errCode = Response.HAS_TAKE_REWARD;
                return petGetRewardVO;
            }
        }
        if (!playerData.getCardRewardIdxSet().isEmpty() && playerData.getCardRewards().size() == 0) {
            ServerLogger.warn("无次数");
            petGetRewardVO.errCode = Response.ERR_PARAM;
            return petGetRewardVO;
        }
        //检查物品
        List<GoodsEntry> goodsList = new ArrayList<>();
        int[] cost = ConfigData.globalParam().DestinyTurnCard[playerData.getCardRewardIdxSet().size()];
        goodsList.add(new GoodsEntry(cost[0], cost[1]));
        if (goodsService.decConsume(playerId, goodsList, LogConsume.DestinyCard) != Response.SUCCESS) {
            ServerLogger.warn("物品不足");
            petGetRewardVO.errCode = Response.ERR_GOODS_COUNT;
            return petGetRewardVO;
        }

        //扣除物品
        //goodsService.decConsume(playerId, goodsList, LogConsume.DestinyCard);

        //构造奖励
        if (playerData.getCardRewardIdxSet().isEmpty()) {
            for (int[] arr : activityTaskCfg.Rewards) {
                playerData.getCardRewards().add(arr);
            }
        }

        int idx = RandomUtil.randInt(playerData.getCardRewards().size());
        playerData.getCardRewardIdxSet().add(idx);
        int[] rewardArr = playerData.getCardRewards().get(idx);
        playerData.getCardRewards().remove(idx);

        List<GoodsEntry> goodsEntries = Lists.newArrayList();
        List<Reward> rewardArrayList = Lists.newArrayList();
        for (int i = 0; i < rewardArr.length; i += 2) {
            Reward reward = new Reward();
            reward.id = rewardArr[i];
            reward.count = rewardArr[i + 1] + ConfigData.globalParam().DestinyTurnCardAdd * (playerData.getCardRewardIdxSet().size() - 1);
            rewardArrayList.add(reward);
            goodsEntries.add(new GoodsEntry(reward.id, reward.count));
        }
        goodsService.addRewards(playerId, goodsEntries, LogConsume.DestinyCard);
        List<ActivityTask> updateTasks = Lists.newArrayList();
        //设置状态
        for (ActivityTask at : playerData.getActivityTasks().values()) {
            if (id == at.getId()) {
                at.setState(ActivityConsts.ActivityState.T_AWARD);
                at.setRewards(rewardArrayList);

                updateTasks.add(at);
                break;
            }
        }

        petGetRewardVO.rewards = rewardArrayList;
        petGetRewardVO.errCode = Response.SUCCESS;

        pushActivityUpdate(playerId, updateTasks);
        return petGetRewardVO;
    }

    /**
     * 刷新命运卡牌
     *
     * @param playerId 玩家id
     * @param id       活动id
     * @return
     */
    public Object refreshDestinyCard(int playerId, int id) {
        IntParam intParam = new IntParam();

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在，玩家ID=" + playerId);
            intParam.param = Response.ERR_PARAM;
            return intParam;
        }

        //检查次数
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, id);
        int refreshDestinyCardTimes = playerData.getRefreshDestinyCardTimes();
        if (refreshDestinyCardTimes > config.Param0) {
            ServerLogger.warn("命运卡牌刷新次数已用完" + playerId);
            intParam.param = Response.ARENA_NO_BUY;
            return intParam;
        }

        //检查物品
        GlobalConfig global = ConfigData.globalParam();
        if (global == null) {
            ServerLogger.warn("全局表不存在");
            intParam.param = Response.ERR_PARAM;
            return intParam;
        }
        List<GoodsEntry> goodsList = new ArrayList<>();
        goodsList.add(new GoodsEntry(global.DestinyCard[0], global.DestinyCard[1]));
        if (goodsService.checkHasEnough(playerId, goodsList) != Response.SUCCESS) {
            ServerLogger.warn("物品不足");
            intParam.param = Response.ERR_GOODS_COUNT;
            return intParam;
        }

        //扣除物品
        goodsService.decConsume(playerId, goodsList, LogConsume.DestinyCard);
        refreshDestinyCardTimes++;

        playerData.getCardRewardIdxSet().clear();
        playerData.getCardRewards().clear();

        //设置完成次数
        playerData.setRefreshDestinyCardTimes(refreshDestinyCardTimes);
        List<ActivityTask> tasks = new ArrayList<>();
        for (ActivityTask activityTask : playerData.getActivityTasks().values()) {
            if (id == activityTask.getActivityId()) {
                ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, activityTask.getId());
                if (activityTaskCfg == null) {
                    ServerLogger.warn("活动不存在，活动ID=" + activityTask.getId());
                    continue;
                }
                activityTask.setFinishNum(refreshDestinyCardTimes);
                activityTask.setState(ActivityConsts.ActivityState.T_UN_FINISH);
                tasks.add(activityTask);
            }
        }
        pushActivityUpdate(playerId, tasks);
        return intParam;
    }

    private void refreshDestinyCard(int playerId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在，玩家ID=" + playerId);
            return;
        }

        //检查次数
        playerData.getCardRewardIdxSet().clear();
        playerData.getCardRewards().clear();
        playerData.setRefreshDestinyCardTimes(0);
    }

    //检查活动完成情况
    public boolean checkFinish(int playerId, int activityId) {
        boolean finish = false;
        PlayerData data = playerService.getPlayerData(playerId);
        Map<Integer, ActivityTask> activityTasks = data.getActivityTasks();
        if (activityTasks == null || activityTasks.isEmpty()) {
            ServerLogger.warn("活动数据错误");
            return false;
        }
        ActivityTask activityTask = activityTasks.get(activityId);
        if (activityTask != null && activityTask.getState() == ActivityConsts.ActivityState.T_AWARD) {
            finish = true;
        }
        return finish;
    }

    //充值购买礼包
    public List<ActivityTask> completeActivityTaskByCharge(int playerId, int taskCondType, float value, int updateType, boolean toCli) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<ActivityTask> tasks = Lists.newArrayList();
        for (ActivityTask at : data.getAllActivityTasks()) {
            //全服登录人数特殊处理
            ActivityTaskCfg activityTaskCfgTemp = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
            if (activityTaskCfgTemp != null && activityTaskCfgTemp.Conds[0][0] == ActivityConsts.ActivityTaskCondType.T_FULL_SERVICE_ATTENDANCE) {
                at.getCond().setValue(value);
            }
            if (at.getCond().getCondType() == taskCondType) {
                if (updateType == ActivityConsts.UpdateType.T_ADD) {
                    at.getCond().setValue(at.getCond().getValue() + value);
                } else if (updateType == ActivityConsts.UpdateType.T_VALUE) {
                    at.getCond().setValue(value);
                }

                //条件读表
                ActivityTaskCfg taskCfg = ConfigData.getConfig(ActivityTaskCfg.class, at.getId());
                if (taskCfg != null) {
                    at.getCond().setTargetValue(taskCfg.Conds[0][1]);
                    if (at.getCond().checkComplete()) {
                        if (completeAndSendReward(playerId, tasks, at, taskCfg, true)) {
                            //当今日充值达到300钻时连锁追加累计充值一次
                            GlobalConfig globalParam = ConfigData.globalParam();
                            if (globalParam != null && taskCondType == ActivityConsts.ActivityTaskCondType.T_DAILY_RECHARGE_DIAMONDS && at.getCond().getTargetValue() == globalParam.Dailyrecharge) {
                                //增加累计充值次数
                                data.setAddUpRechargeDiamondsTimes(data.getAddUpRechargeDiamondsTimes() + 1);
                                Map<Integer, Integer> typeNumberMap = new HashMap<>();
                                for (ActivityTask activityTask : data.getAllActivityTasks()) {
                                    if (activityTask.getCond().getCondType() == ActivityConsts.ActivityTaskCondType.T_TIMED_MONEY_DIAMONDS) {
                                        ActivityTaskCfg activityTaskCfg = ConfigData.getConfig(ActivityTaskCfg.class, activityTask.getId());
                                        if (activityTaskCfg != null) {
                                            activityTask.setFinishNum(activityTask.getFinishNum() + 1);
                                            typeNumberMap.put((int) activityTaskCfg.Conds[0][2], data.getAddUpRechargeDiamondsTimes() * globalParam.Dailyrecharge);
                                        }
                                    }
                                }
                                completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_TIMED_MONEY_DIAMONDS, true, typeNumberMap, false);
                            }
                            integral(playerId, at);//巡礼活动
                        } else {
                            continue;
                        }
                    }
                    tasks.add(at);
                }
            }
        }
        if (toCli) {
            pushActivityUpdate(playerId, tasks);
        }

        return tasks;
    }
}