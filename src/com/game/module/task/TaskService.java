package com.game.module.task;

import com.game.data.*;
import com.game.event.Dispose;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.attach.arena.ArenaLogic;
import com.game.module.attach.arena.ArenaPlayer;
import com.game.module.gang.GMember;
import com.game.module.gang.Gang;
import com.game.module.gang.GangService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.ladder.LadderService;
import com.game.module.log.LogConsume;
import com.game.module.player.Jewel;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.rank.RankEntity;
import com.game.module.rank.RankService;
import com.game.module.rank.RankingList;
import com.game.module.rank.vo.AchievementRankEntity;
import com.game.module.rank.vo.FightingRankEntity;
import com.game.module.serial.PlayerView;
import com.game.module.serial.SerialDataService;
import com.game.module.skill.SkillCard;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.AchievementSyncVo;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.task.SJointTaskVo;
import com.game.params.task.STaskVo;
import com.game.params.task.TaskListInfo;
import com.game.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO 优化方案，按任务类型分组存储，减少遍历次数
 */
@Service
public class TaskService implements Dispose {
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GangService gangService;
    @Autowired
    private TitleService titleService;
    @Autowired
    private RankService rankService;
    @Autowired
    private ArenaLogic arenaLogic;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private ActivityService activityService;

    private Map<Integer, PlayerTask> tasks = new ConcurrentHashMap<Integer, PlayerTask>();

    @Override
    public void removeCache(int playerId) {
        tasks.remove(playerId);
    }

    // 获取任务配置
    public TaskConfig getConfig(int taskId) {
        return GameData.getConfig(TaskConfig.class, taskId);
    }

    // 获取玩家任务
    public PlayerTask getPlayerTask(int playerId) {
        PlayerTask playerTasks = tasks.get(playerId);
        // 获取玩家任务
        if (playerTasks != null) {
            return playerTasks;
        }
        byte[] dbData = taskDao.select(playerId);
        if (dbData != null) {
            dbData = CompressUtil.decompressBytes(dbData);
            playerTasks = JsonUtils.string2Object(
                    new String(dbData, Charset.forName("utf-8")),
                    PlayerTask.class);
            if (playerTasks == null) {
                ServerLogger.warn("Err Player Task:", playerId, dbData.length);
                playerTasks = new PlayerTask();
            }
            List<Integer> errId = new ArrayList<Integer>();
            for (int id : playerTasks.getTasks().keySet()) {
                if (ConfigData.getConfig(TaskConfig.class, id) == null) {
                    ServerLogger.warn("FindErrTaskId:", id, playerId);
                    errId.add(id);
                }
            }
            for (int id : errId) {
                playerTasks.getTasks().remove(id);
            }
        } else {
            playerTasks = new PlayerTask();
        }
        tasks.put(playerId, playerTasks);
        return playerTasks;
    }

    // 获取当前的所有任务
    public TaskListInfo getCurTasks(int playerId) {
        TaskListInfo result = new TaskListInfo();
        result.task = new ArrayList<>();
        Player player = playerService.getPlayer(playerId);
        PlayerTask playerTask = getPlayerTask(playerId);
        List<Task> tasks = new ArrayList<>(playerTask.getTasks().values());
        if (player.getGangId() > 0) {
            Gang gang = gangService.getGang(player.getGangId());
            tasks.addAll(gang.getTasks().values());
        }
        for (Task task : tasks) {
            TaskConfig config = getConfig(task.getTaskId());
            if (!(config.taskType == 3 || config.taskType == 4)) {
                if (task.getState() == Task.STATE_INIT || (task.getState() == Task.STATE_SUBMITED && config.nextTaskId != 0))
                    continue;
            }
            STaskVo vo = new STaskVo();
            vo.id = task.getTaskId();
            vo.state = task.getState();
            vo.count = task.getCount();
            result.task.add(vo);
        }

        result.myJoint = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : playerTask.getMyJointTasks()
                .entrySet()) {
            SJointTaskVo param = new SJointTaskVo();
            param.id = entry.getKey();
            int parnterId = entry.getValue();
            param.playerId = parnterId;
            if (parnterId > 0) {
                Player parnter = playerService.getPlayer(parnterId);
                param.name = parnter.getName();
                param.lev = parnter.getLev();
                param.vocation = parnter.getVocation();
                param.online = SessionManager.getInstance().isActive(parnterId);
            }
            result.myJoint.add(param);
        }
        result.jointedCount = playerTask.getJointedCount();
        JointTask curJointed = playerTask.getCurrJointedTask();
        if (curJointed != null) {
            SJointTaskVo currJointedPartner = new SJointTaskVo();
            currJointedPartner.id = curJointed.getTaskId();
            int parnterId = curJointed.getParnterId();
            currJointedPartner.playerId = parnterId;
            if (parnterId > 0) {
                Player parnter = playerService.getPlayer(parnterId);
                currJointedPartner.name = parnter.getName();
                currJointedPartner.lev = parnter.getLev();
                currJointedPartner.vocation = parnter.getVocation();
                currJointedPartner.online = SessionManager.getInstance().isActive(parnterId);
            }
            result.currJointedPartner = currJointedPartner;

            STaskVo vo = new STaskVo();
            vo.id = curJointed.getTaskId();
            vo.state = curJointed.getState();
            vo.count = curJointed.getCount();
            vo.isJoint = true;
            result.task.add(vo);

        }

        result.jointedList = new ArrayList<SJointTaskVo>();
        for (String str : playerTask.getJointedTasks()) {
            String[] ss = str.split("_");
            SJointTaskVo param = new SJointTaskVo();
            param.id = Integer.parseInt(ss[0]);
            int parnterId = Integer.parseInt(ss[1]);
            param.playerId = parnterId;
            if (parnterId > 0) {
                Player parnter = playerService.getPlayer(parnterId);
                param.name = parnter.getName();
                param.lev = parnter.getLev();
                param.vocation = parnter.getVocation();
                param.online = SessionManager.getInstance().isActive(parnterId);
            }
            result.jointedList.add(param);
        }
        result.liveness = playerTask.getLiveness();
        if (!playerTask.getLiveBox().isEmpty()) {
            result.livebox = new ArrayList<IntParam>();
            for (int id : playerTask.getLiveBox()) {
                IntParam param = new IntParam();
                param.param = id;
                result.livebox.add(param);
            }
        }
        return result;
    }

    // 初始化任务系统
    public void initTask(int playerId) {
        PlayerTask task = new PlayerTask();
        tasks.put(playerId, task);
        for (Object config : ConfigData.getConfigs(TaskConfig.class)) {
            TaskConfig taskConfig = (TaskConfig) config;
            if (taskConfig.level <= 1 && taskConfig.taskType != Task.TYPE_JOINT && taskConfig.taskType != Task.TYPE_GANG) {
                addNewTask(playerId, taskConfig.id, false);
            }
        }
        updateDailyTasks(playerId);
        updateWeeklyTasks(playerId);
        updateJointTasks(playerId);
        taskDao.insert(playerId);
    }

    public void onLogin(int playerId) {
        Map<Integer, Task> taskMap = getPlayerTask(playerId).getTasks();
        for (Object config : ConfigData.getConfigs(TaskConfig.class)) {
            TaskConfig taskConfig = (TaskConfig) config;
            if (!taskMap.containsKey(taskConfig.id)) {
                if (taskConfig.level <= 1) {
                    addNewTask(playerId, taskConfig.id, false);
                }
            }
        }
    }

    public Task addNewTask(int playerId, int taskId) {
        return addNewTask(playerId, taskId, true);
    }

    public Task addNewTask(int playerId, int taskId, boolean refresh) {
        Player player = playerService.getPlayer(playerId);
        TaskConfig taskCfg = getConfig(taskId);
        Task task = new Task(taskId,
                player.getLev() >= taskCfg.level ? Task.STATE_ACCEPTED
                        : Task.STATE_INIT, taskCfg.finishType);
        getPlayerTask(playerId).getTasks().put(taskId, task);
        if (refresh) {
            updateTaskToClient(playerId, task);
        }
        return task;
    }

    // 检查任务是否完成
    public boolean checkFinished(Task task, int playerId) {
        TaskConfig config = getConfig(task.getTaskId());
        int[] targets = config.finishParam;

        if (task.getState() == Task.STATE_FINISHED || targets == null) {
            return true;
        }
        int count = targets[targets.length - 1];
        if (config.finishType == Task.TYPE_PASS_TIME
                || config.finishType == Task.TYPE_GANG_RANK
                || config.finishType == Task.TYPE_ARENA_RANK
                || config.finishType == Task.TYPE_FIGHT_RANK
                || config.finishType == Task.TYPE_LADDER_RANK
                || config.finishType == Task.TYPE_WB_RANK
                || config.finishType == Task.TYPE_ACHIEVEMENT_RANK) {
            if (task.getCount() != 0 && task.getCount() <= count) {
                task.setState(Task.STATE_FINISHED);
                return true;
            }
        } else if (config.finishType == Task.TYPE_HIT) {
            if (task.getCount() <= count) {
                task.setState(Task.STATE_FINISHED);
                return true;
            }
        } else {
            if (task.getCount() >= count) {
                task.setCount(count);
                task.setState(Task.STATE_FINISHED);
                if (config.taskType < 6) { //除开成就任务
                    //任务称号
                    titleService.complete(playerId, TitleConsts.TASK, task.getTaskId(), ActivityConsts.UpdateType.T_VALUE);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 完成任务接口
     *
     * @param playerId
     * @param type
     * @param params
     */
    private boolean doTasks(int playerId, int type, Task task, int... params) {
        try {
            Player player = playerService.getPlayer(playerId);
            if (player.getAccName().equals(PlayerService.ROBOT)) {
                return false;
            }
            PlayerData data = playerService.getPlayerData(playerId);
            Gang gang = null;
            if (player.getGangId() > 0) {
                gang = gangService.getGang(player.getGangId());
            }

            int oldState = task.getState();

            if (oldState == Task.STATE_INIT || oldState == Task.STATE_SUBMITED) {
                return false;
            }
            TaskConfig config = getConfig(task.getTaskId());

            if (config == null) {
                ServerLogger.warn("ErrorTaskId:", task.getTaskId());
                return false;
            }
            /*if (config.finishType != type) {
                return;
            }*/

            if (type != config.finishType) {
                return false;
            }

            if (player.getLev() < config.level) {
                return false;
            }

            int[] targets = config.finishParam;
            // 无任务目标的
            if (targets == null) {
                return false;
            }

            int count = targets.length;

            int oldCount = task.getCount();

            /*if (config.finishType == Task.FINISH_WEAR) {
                int request = targets[0];
                int curCount = 0;
                Collection<Goods> goods = goodsService.getPlayerBag(playerId)
                        .getAllGoods().values();
                for (Goods g : goods) {
                    if (!g.isInBag()) {
                        GoodsConfig goodsCfg = ConfigData.getConfig(
                                GoodsConfig.class, g.getGoodsId());
                        if (!CommonUtil.contain(
                                ConfigData.globalParam().equipTypes,
                                goodsCfg.type)) {
                            continue;
                        }
                        if (goodsCfg.color == request) {
                            curCount++;
                        }
                    }
                }
                task.setCount(curCount);
            } else*/
            if (config.finishType == Task.FINISH_STONE) {
                if (count == 1) {
                    task.alterCount(params[params.length - 1]);
                } else {
                    if (params.length != count) {
                        return false;
                    }
                    int reqLev = targets[0];
                    int reqPos = targets[1];
                    if (/*params[0] < reqLev ||*/ (reqPos > 0 && reqPos != params[1])) {
                        return false;
                    }
                    int curCount = 0;
                    if (reqPos > 0) {
                        Jewel jewel = data.getJewels().get(reqPos);
                        if (jewel != null && jewel.getLev() >= reqLev) {
                            curCount++;
                        }
                    } else {
                        for (Jewel jewel : data.getJewels().values()) {
                            if (jewel.getLev() >= reqLev) {
                                curCount++;
                            }
                        }
                    }
                    task.setCount(curCount);
                }
            } else if (config.finishType == Task.FINISH_STRONG) {
                task.alterCount(params[1]);
//                if (count == 1) {
//                    task.alterCount(params[params.length - 1]);
//                } else {
//                    int reqLev = targets[0];
//                    int reqPos = targets[1];
//                    if (/*params[0] < reqLev || */(reqPos > 0 && reqPos != params[1])) {
//                        return false;
//                    }
//                    Map<Integer, Integer> strengths = playerService
//                            .getPlayerData(playerId).getStrengths();
//                    int curCount = 0;
//                    if (reqPos == 0) {
//                        for (int str : strengths.values()) {
//                            if (str >= reqLev) {
//                                curCount++;
//                            }
//                        }
//                    } else if (strengths.getOrDefault(reqPos, 0) >= reqLev) {
//                        curCount = 1;
//                    }
//                    task.setCount(curCount);
//                }
            } else if (config.finishType == Task.FINISH_STAR) {
                task.alterCount(params[1]);
//                if (count == 1) {
//                    task.alterCount(params[params.length - 1]);
//                } else {
//                    int request = targets[0];
//                    if (params[0] < request) {
//                        return false;
//                    }
//                    Collection<Goods> goods = goodsService.getPlayerBag(playerId)
//                            .getAllGoods().values();
//                    int curCount = 0;
//                    for (Goods g : goods) {
//                        if (!g.isInBag()) {
//                            GoodsConfig goodsCfg = ConfigData.getConfig(
//                                    GoodsConfig.class, g.getGoodsId());
//                            if (!CommonUtil.contain(
//                                    ConfigData.globalParam().equipTypes,
//                                    goodsCfg.type)) {
//                                continue;
//                            }
//                            if (goodsCfg.color >= request) {
//                                curCount++;
//                            }
//                        }
//                    }
//                    task.setCount(curCount);
//                }
            } else if (config.finishType == Task.FINISH_SKILL) {
                List<Integer> skills = data.getCurSkills();
                List<Integer> curCards = data.getCurrCard();
                int curCount = 0;
                int reqLev = targets[0];
                int reqCLev = targets[1];
                for (int i = 1; i <= 4; i++) {
                    int skillId = skills.get(i);
                    if (skillId == 0) {
                        continue;
                    }
                    SkillConfig skillCfg = GameData.getConfig(SkillConfig.class, skillId);
                    if (skillCfg.lev < reqLev) {
                        continue;
                    }
                    if (reqCLev > 0) {
                        SkillCard skillCard = data.getSkillCards().get(curCards.get(i - 1));
                        if (skillCard == null) {
                            continue;
                        }
                        SkillCardConfig cardCfg = GameData.getConfig(SkillCardConfig.class, skillCard.getCardId());
                        if (cardCfg.lv < reqCLev) {
                            continue;
                        }
                    }
                    curCount++;
                }
                task.setCount(curCount);
            } else if (config.finishType == Task.FINISH_CARD_UPGRADE || config.finishType == Task.FINISH_CARD_COMPOSE) {
                if (count == 1) {
                    task.alterCount(params[params.length - 1]);
                } else {
                    int request = targets[0];
                    if (params[0] < request) {
                        return false;
                    }
                    int curCount = 0;
                    Collection<SkillCard> skillCards = data.getSkillCards().values();
                    for (SkillCard card : skillCards) {
                        SkillCardConfig cardCfg = GameData.getConfig(SkillCardConfig.class, card.getCardId());
                        int cur = config.finishType == Task.FINISH_CARD_UPGRADE ? cardCfg.lv : cardCfg.quality;
                        if (cur >= request) {
                            curCount++;
                        }
                    }
                    task.alterCount(curCount);
                }
            } else if (config.finishType == Task.TYPE_SQ_UP
                    || config.finishType == Task.TYPE_LEVEL
                    || config.finishType == Task.TYPE_ARENA_WINS
                    || config.finishType == Task.TYPE_MUTATE_PET
                    || config.finishType == Task.TYPE_FIGHT
                    || config.finishType == Task.ACHIEVEMENT_VIP
                    || config.finishType == Task.TYPE_GANG_TEC
                    || config.finishType == Task.TYPE_TITLE
                    || config.finishType == Task.TYPE_FRIEND_COUNT
                    || config.finishType == Task.TYPE_PET_ANY_ACTIVITY
                    || config.finishType == Task.TYPE_GANG_LEVEL) {
                if (task.getCount() < params[0]) {
                    task.setCount(params[0]);
                }
            } else if (config.finishType == Task.TYPE_GANG_RANK
                    || config.finishType == Task.TYPE_WB_RANK
                    || config.finishType == Task.TYPE_ARENA_RANK
                    || config.finishType == Task.TYPE_LADDER_RANK
                    || config.finishType == Task.TYPE_FIGHT_RANK
                    || config.finishType == Task.TYPE_ACHIEVEMENT_RANK) {
                if (task.getCount() == 0 || task.getCount() > params[0]) {
                    task.setCount(params[0]);
                }
            } else {
                if (params.length != count) {
                    return false;
                }
                int i = count - 2;
                for (; i >= 0; i--) {
                    if (targets[i] != 0 && targets[i] != params[i]) {
                        break;
                    }
                }
                if (i >= 0) {
                    return false;
                }

                if (config.finishType == Task.TYPE_PASS_TIME
                        || config.finishType == Task.TYPE_SKILL_LEVEL
                        || config.finishType == Task.TYPE_HIT) {
                    task.setCount(params[count - 1]);
                } else {
                    task.alterCount(params[count - 1]);
                }
            }

            checkFinished(task, playerId);
            if (task.getState() == Task.STATE_FINISHED) {
                if (config.taskType < 6) { //除开成就任务
                    data.setFinishTaskCount(1);
                    doTask(playerId, Task.TYPE_TASK_COUNT, data.getFinishTaskCount());
                }
            }

            if (task.getCount() != oldCount || task.getState() != oldState) {
                if (config.taskType == Task.TYPE_GANG) {
                    if (gang != null) {
                        GMember gMember = gang.getMembers().get(playerId);
                        if (gMember != null) {
                            gMember.alterTaskContribution(1);
                        }else{
                            ServerLogger.warn("member is not existent,gang=" + gang + ",playerId=" + playerId);
                        }
                        if (task.getState() == Task.STATE_FINISHED) {
                            gangService.sendTaskReward(player.getGangId(), config);
                            task.setState(Task.STATE_SUBMITED);

                            //公会任务活动
                            activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_GUILD_TASK);
                        }
                    }
                }
                // 发送到前端
                //updateTaskToClient(playerId, task);
                if (config.taskType == Task.TYPE_JOINT) {
                    // 邀请任务
                    checkJointTask(playerId, task);
                }
                return true;
            }
        } catch (Exception ex) {
            String paramStr = "";
            for (int i : params) {
                paramStr += " " + i;
            }
            ServerLogger.err(ex, "doTask error,type=" + type + ",params=" + paramStr);
        }
        return false;
    }

    /**
     * 完成任务接口
     *
     * @param playerId
     * @param type
     * @param params
     */
    public void doTask(int playerId, int type, Task newTask, int... params) {
        boolean result = doTasks(playerId, type, newTask, params);
        if (result) {
            updateTaskToClient(playerId, newTask);
        }
    }

    public void doTask(int playerId, Map<Integer, int[]> condParams) {
        if (condParams == null || condParams.isEmpty()) {
            return;
        }
        List<Map<Integer, int[]>> list = Lists.newArrayList();
        list.add(condParams);
        doTaskList(playerId, list);
    }

    public void doTaskList(int playerId, List<Map<Integer, int[]>> condParamsList) {
        if (condParamsList == null || condParamsList.isEmpty()) {
            return;
        }
        if (!SessionManager.getInstance().getAllSessions().containsKey(playerId)) {
            return;
        }
        Player player = playerService.getPlayer(playerId);
        if (player.getAccName().equals(PlayerService.ROBOT)) {
            return;
        }
        PlayerTask playerTask = getPlayerTask(playerId);
        List<Task> tasks = new ArrayList<>(playerTask.getTasks().values());
        if (playerTask.getCurrJointedTask() != null) {
            tasks.add(playerTask.getCurrJointedTask());
        }
        if (player.getGangId() > 0) {
            Gang gang = gangService.getGang(player.getGangId());
            if (gang != null) {
                tasks.addAll(gang.getTasks().values());
            } else {
                player.setGangId(0);
            }
        }

        List<Task> updateTasks = Lists.newArrayList();

        for (Map<Integer, int[]> condParams : condParamsList) {
            for (Task task : tasks) {
                TaskConfig config = getConfig(task.getTaskId());
                if (task.getState() == Task.STATE_FINISHED ||
                        task.getState() == Task.STATE_SUBMITED) {
                    continue;
                }
                int[] params = condParams.get(config.finishType);
                if (params == null) {
                    continue;
                }

                boolean result = doTasks(playerId, config.finishType, task, params);
                if (result) {
                    updateTasks.add(task);
                }
            }
        }
        updateTaskToClient(playerId, updateTasks);
    }

    public void doTask(int playerId, int type, int... params) {
        if (!SessionManager.getInstance().getAllSessions().containsKey(playerId)) {
            return;
        }
        Map<Integer, int[]> condParams = Maps.newHashMapWithExpectedSize(1);
        condParams.put(type, params);
        doTask(playerId, condParams);
    }


    private void checkJointTask(int playerId, Task task) {
        int taskId = task.getTaskId();
        PlayerTask playerTask = getPlayerTask(playerId);
        if (JointTask.class.isInstance(task)) {
            JointTask jointedTask = playerTask.getCurrJointedTask();
            Task _task = getPlayerTask(jointedTask.getParnterId()).getTasks()
                    .get(taskId);
            _task.setCount(task.getCount());
            _task.setState(task.getState());
            updateTaskToClient(jointedTask.getParnterId(), _task);
        } else {
            Integer partnerId = playerTask.getMyJointTasks().get(taskId);
            if (partnerId != null && partnerId > 0) {
                Task _task = getPlayerTask(partnerId).getCurrJointedTask();
                _task.setCount(task.getCount());
                _task.setState(task.getState());
                if (SessionManager.getInstance().isActive(partnerId)) {
                    updateTaskToClient(partnerId, _task);
                } else {
                    updateTask(partnerId);
                }
            }
        }
    }

    // 加任务奖励
    public boolean addTaskReward(int playerId, int taskId) {
        TaskConfig config = getConfig(taskId);

        Player player = playerService.getPlayer(playerId);
        int lev = (player.getLev() / 10) * 10;
        if (lev == 0) {
            lev = 1;
        }

        int[][] rewards = config.rewards;
        List<GoodsEntry> goods = new ArrayList<GoodsEntry>();
        for (int i = 0; i < rewards.length; i++) {
            int[] reward = rewards[i];
            int id = reward[0];
            int count = reward[1];

            goods.add(new GoodsEntry(id, count));
        }

        goodsService
                .addRewards(playerId, goods, LogConsume.TASK_REWARD, taskId);
        return true;
    }

    // 更新人物到前端
    public void updateTaskToClient(int playerId, Task task) {
        // 发送到前端
        ListParam<STaskVo> tasks = new ListParam<STaskVo>();
        tasks.params = new ArrayList<STaskVo>();
        STaskVo vo = new STaskVo();
        vo.count = task.getCount();
        vo.state = task.getState();
        vo.id = task.getTaskId();
        vo.isJoint = task == getPlayerTask(playerId).getCurrJointedTask();
        tasks.params.add(vo);
        SessionManager.getInstance().sendMsg(TaskExtension.TASK_UPDATE, tasks,
                playerId);
    }

    // 更新人物到前端
    public void updateTaskToClient(int playerId, List<Task> updateList) {
        if (updateList.isEmpty()) return;
        ListParam<STaskVo> tasks = new ListParam<STaskVo>();
        tasks.params = new ArrayList<STaskVo>();
        for (Task task : updateList) {
            STaskVo vo = new STaskVo();
            vo.count = task.getCount();
            vo.state = task.getState();
            vo.id = task.getTaskId();
            vo.isJoint = task == getPlayerTask(playerId).getCurrJointedTask();
            tasks.params.add(vo);
        }
        SessionManager.getInstance().sendMsg(TaskExtension.TASK_UPDATE, tasks,
                playerId);
    }

    // 更新数据库
    public void updateTask(int playerId) {
        PlayerTask data = tasks.get(playerId);
        if (data == null) {
            return;
        }
        String str = JsonUtils.object2String(data);

        byte[] dbData = str.getBytes(Charset.forName("utf-8"));
        taskDao.update(playerId, CompressUtil.compressBytes(dbData));
    }

    // 人物升级时检查是否完成
    public void checkTaskWhenLevUp(int playerId) {
        int lev = playerService.getPlayer(playerId).getLev();
        List<Task> updateList = new ArrayList<>();
        Map<Integer, Task> taskMap = getPlayerTask(playerId).getTasks();

        for (Object config : ConfigData.getConfigs(TaskConfig.class)) {
            TaskConfig taskConfig = (TaskConfig) config;
            if (!taskMap.containsKey(taskConfig.id)
                    && taskConfig.taskType != Task.TYPE_JOINT && taskConfig.taskType != Task.TYPE_GANG) {
                if (taskConfig.level <= lev) {
                    Task task = addNewTask(playerId, taskConfig.id, false);
                    updateList.add(task);
                }
            }
        }
        updateTaskToClient(playerId, updateList);
    }

    public void dailyReset(int playerId) {
        PlayerTask playerTask = getPlayerTask(playerId);
        updateDailyTasks(playerId);
        updateJointTasks(playerId);
        playerTask.setLiveness(0);
        playerTask.getLiveBox().clear();
        SessionManager.getInstance().sendMsg(TaskExtension.TASK_LIST_INFO,
                getCurTasks(playerId), playerId);
    }

    public void updateDailyTasks(int playerId) {
        updateTasks(playerId, ConfigData.getDailyTasks());
    }

    public void updateWeeklyTasks(int playerId) {
        updateTasks(playerId, ConfigData.getWeeklyTasks());
    }

    private void updateTasks(int playerId, List<Integer> tasks) {
        ListParam<STaskVo> newTasks = new ListParam<STaskVo>();
        newTasks.params = new ArrayList<>();
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return;
        }
        Map<Integer, Task> playerTasks = getPlayerTask(playerId).getTasks();
        for (int taskId : tasks) {
            Task task = playerTasks.get(taskId);
            if (task == null) {
                TaskConfig config = ConfigData.getConfig(TaskConfig.class, taskId);
                if (player.getLev() < config.level) {
                    continue;
                }
                task = addNewTask(playerId, taskId, false);
            } else {
                task.setCount(0);
                if (task.getState() != Task.STATE_INIT) {
                    task.setState(Task.STATE_ACCEPTED);
                }
            }
            playerTasks.put(taskId, task);

            STaskVo vo = new STaskVo();
            vo.id = task.getTaskId();
            vo.count = task.getCount();
            vo.state = task.getState();
            newTasks.params.add(vo);
        }
    }

    public void updateJointTasks(int playerId) {
        Player player = playerService.getPlayer(playerId);
        PlayerTask playerTask = getPlayerTask(playerId);
        playerTask.getJointedTasks().clear();
        playerTask.setJointedCount(0);
        playerTask.setCurrJointedTask(null);
        Map<Integer, Integer> myJointTasks = playerTask.getMyJointTasks();
        myJointTasks.clear();
        List<Integer> jointTasks = new ArrayList<>(ConfigData.getJointTasks());
        int size = jointTasks.size();
        Set<Integer> finishTypes = new HashSet<Integer>(3);
        while (myJointTasks.size() < 3) {
            Integer taskId = jointTasks.get(RandomUtil.randInt(size));
            TaskConfig cfg = getConfig(taskId);
            jointTasks.remove(taskId);
            size--;
            if (player.getLev() < cfg.level) {
                continue;
            }
            /*if (finishTypes.contains(cfg.finishType)) {
                continue;
            }*/
            playerTask.getTasks().remove(taskId);
            finishTypes.add(cfg.finishType);
            myJointTasks.putIfAbsent(taskId, 0);
        }
    }

    public int inviteJointTask(int playerId, int taskId, int partnerId) {
        PlayerTask playerTask = getPlayerTask(playerId);
        Map<Integer, Integer> myJointTasks = playerTask.getMyJointTasks();
        if (!myJointTasks.containsKey(taskId) || myJointTasks.get(taskId) > 0) {
            return Response.ERR_PARAM;
        }
        TaskConfig taskCfg = getConfig(taskId);
        Player partner = playerService.getPlayer(partnerId);
        if (partner.getLev() < taskCfg.level) {
            return Response.PARTNER_NO_LEV;
        }
        PlayerTask playerTask2 = getPlayerTask(partnerId);
        if (playerTask2.getCurrJointedTask() != null) {
            return Response.TASK_JOINTED;
        }
        if (playerTask2.getJointedCount() >= 3) {
            return Response.TASK_NO_NOINT;
        }
        /*if (partner.getTeamId() != 0) {
            return Response.ERR_PARAM;
		}*/

        String key = String.format("%d_%d", taskId, playerId);
        List<String> jointedTasks = playerTask2.getJointedTasks();
        jointedTasks.remove(key);
        jointedTasks.add(0, key);
        if (SessionManager.getInstance().isActive(partnerId)) {
            SessionManager.getInstance().sendMsg(TaskExtension.TASK_LIST_INFO,
                    getCurTasks(partnerId), partnerId);
        }
        return Response.SUCCESS;
    }

    public int refuseJoint(int playerId, int taskId, int partnerId) {
        PlayerTask playerTask = getPlayerTask(playerId);
        playerTask.getJointedTasks().remove(
                String.format("%d_%d", taskId, partnerId));

        return Response.SUCCESS;
    }

    public int acceptJoint(int playerId, int taskId, int partnerId) {
        PlayerTask playerTask = getPlayerTask(playerId);
        if (playerTask.getJointedCount() >= 3) {
            return Response.NO_TODAY_TIMES;
        }
        List<String> jointedTasks = playerTask.getJointedTasks();
        String key = String.format("%d_%d", taskId, partnerId);
        if (!jointedTasks.contains(key)) {
            return Response.ERR_PARAM;
        }
                                                                                                                                                                                                                      JointTask myCurrJointedTask = playerTask.getCurrJointedTask();
        if (myCurrJointedTask != null) {
            return Response.TASK_JOINTED;
        }
        PlayerTask playerTask2 = getPlayerTask(partnerId);
        Map<Integer, Integer> myJointTasks2 = playerTask2.getMyJointTasks();
        if (!myJointTasks2.containsKey(taskId)) {
            return Response.ERR_PARAM;
        }
        if (myJointTasks2.get(taskId) > 0) {
            // 已经有人接了
            return Response.TASK_PERFORMING;
        }

        playerTask.alterJointedCount(1);

        jointedTasks.remove(key);
        TaskConfig cfg = ConfigData.getConfig(TaskConfig.class, taskId);
        myCurrJointedTask = new JointTask(taskId, partnerId, cfg.taskType);
        playerTask.setCurrJointedTask(myCurrJointedTask);

        myJointTasks2.put(taskId, playerId);
        addNewTask(partnerId, taskId);
        //通知对方
        if (SessionManager.getInstance().isActive(partnerId)) {
            Int2Param notify = new Int2Param();
            notify.param1 = taskId;
            notify.param2 = playerId;
            SessionManager.getInstance().sendMsg(TaskExtension.ACCEPTED_JOINT, notify, partnerId);
        } else {
            updateTask(partnerId);
        }

        return Response.SUCCESS;
    }

    public IntParam achievementTask(int playerId, AchievementSyncVo param) {
        int[] params = new int[param.argsList.size()];
        for (int i = 0; i < param.argsList.size(); i++) {
            params[i] = param.argsList.get(i);
        }
        doTask(playerId, param.finishType, params);
        IntParam result = new IntParam();
        result.param = Response.SUCCESS;
        return result;
    }

    /**
     * 每天排行榜类成就发放
     */
    public void dailyAchievement() {
        ServerLogger.warn("achievement.............");
        /////成就
        RankingList<FightingRankEntity> fightRank = rankService.getRankingList(RankService.TYPE_FIGHTING);
        int i = 1;
        for (RankEntity rankEntity : fightRank.getOrderList()) {
            PlayerView playerView = serialDataService.getData().getPlayerView(rankEntity.getPlayerId());
            playerView.setLadderMaxRank(i);
            doTask(rankEntity.getPlayerId(), Task.TYPE_FIGHT_RANK, i);
            i++;
        }
        i = 1;
        RankingList<AchievementRankEntity> achievementRank = rankService.getRankingList(RankService.TYPE_ACHIEVEMENT);
        for (RankEntity rankEntity : achievementRank.getOrderList()) {
            PlayerView playerView = serialDataService.getData().getPlayerView(rankEntity.getPlayerId());
            playerView.setAchievementMaxRank(i);
            doTask(rankEntity.getPlayerId(), Task.TYPE_ACHIEVEMENT_RANK, i);
            i++;
        }

        for (i = 1; i <= 50; i++) {
            ArenaPlayer player = arenaLogic.getArenaPlayerByRank(i);
            if (serialDataService.getData() != null) {
                PlayerView playerView = serialDataService.getData().getPlayerView(player.getPlayerId());
                playerView.setAchievementMaxRank(i);
                doTask(player.getPlayerId(), Task.TYPE_ARENA_RANK, i);
            }
        }
/*
        ListParam<LadderRankVO> ladderRank = ladderService.getLadderRank();
        i = 1;
        for (LadderRankVO vo : ladderRank.params) {
            PlayerView playerView = serialDataService.getData().getPlayerView(vo.playerId);
            playerView.setAchievementMaxRank(i);
            doTask(vo.playerId, Task.TYPE_LADDER_RANK, i);
            i++;
        }*/
    }

    public IntParam getAllReward(int playerId) {
        PlayerTask playerTask = getPlayerTask(playerId);
        List<Task> tasks = new ArrayList<>(playerTask.getTasks().values());
        List<GoodsEntry> reward = Lists.newArrayList();
        List<Task> updateTasks = Lists.newArrayList();
        for (Task task : tasks) {
            TaskConfig config = ConfigData.getConfig(TaskConfig.class, task.getTaskId());
            if (config.taskType >= 6) {
                if (task.getState() == Task.STATE_FINISHED) {
                    task.setState(Task.STATE_SUBMITED);
                    updateTasks.add(task);
                    for (int[] arr : config.rewards) {
                        reward.add(new GoodsEntry(arr[0], arr[1]));
                    }
                }
            }
        }

        goodsService.addRewards(playerId, reward, LogConsume.ACHIEVEMENT_GET_ALL);
        updateTaskToClient(playerId, updateTasks);
        IntParam param = new IntParam();
        param.param = Response.SUCCESS;
        return param;
    }
}
