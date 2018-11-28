package com.game.module.gm;

import com.game.SysConfig;
import com.game.data.CopyConfig;
import com.game.data.GangTrainingCfg;
import com.game.data.ModuleOpenCfg;
import com.game.data.Response;
import com.game.module.activity.ActivityService;
import com.game.module.activity.WelfareCardService;
import com.game.module.admin.MessageService;
import com.game.module.artifact.ArtifactService;
import com.game.module.attach.arena.ArenaExtension;
import com.game.module.attach.arena.ArenaLogic;
import com.game.module.attach.endless.EndlessAttach;
import com.game.module.attach.endless.EndlessLogic;
import com.game.module.attach.lottery.LotteryExtension;
import com.game.module.attach.training.TrainingExtension;
import com.game.module.attach.training.trainingLogic;
import com.game.module.chat.ChatExtension;
import com.game.module.copy.Copy;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.daily.DailyService;
import com.game.module.fame.FameService;
import com.game.module.fashion.FashionService;
import com.game.module.friend.FriendService;
import com.game.module.gang.*;
import com.game.module.goods.EquipService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.ladder.LadderService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.pet.PetService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.rank.RankService;
import com.game.module.serial.SerialDataService;
import com.game.module.shop.ShopService;
import com.game.module.skill.SkillService;
import com.game.module.task.Task;
import com.game.module.task.TaskExtension;
import com.game.module.task.TaskService;
import com.game.module.title.TitleService;
import com.game.module.traversing.TraversingExtension;
import com.game.module.vip.VipService;
import com.game.module.worldboss.WorldBossService;
import com.game.params.*;
import com.game.params.chat.ChatInfoList;
import com.game.params.chat.ChatVo;
import com.game.params.copy.CopyInfo;
import com.game.params.copy.CopyResult;
import com.game.params.ladder.TrainingResultVO;
import com.game.params.pet.StartPetActivityVO;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.game.util.TimeUtil;
import com.google.common.collect.Lists;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GmService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MailService mailService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private DailyService dailyService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private VipService vipService;
    @Autowired
    private GangService gangService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private ArenaExtension arenaExtension;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private TrainingExtension trainingExtension;
    @Autowired
    private TraversingExtension traversingExtension;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private WorldBossService worldBossService;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private CopyExtension copyExtension;

    public void handle(int playerId, String gm) {
        try {
            String[] params = gm.substring(1).split("　");//注意这里用的是全角空格
            if (params.length == 1) {
                params = gm.substring(1).split(" ");//
            }
            String methodName = params[0].replace("_", "").toLowerCase();
            Method[] methods = this.getClass().getDeclaredMethods();
            boolean find = false;
            for (Method method : methods) {
                if (method.getName().toLowerCase().equals(methodName)) {
                    find = true;
                    String[] funParams = null;
                    if (params.length > 1) {
                        funParams = Arrays.copyOfRange(params, 1, params.length);
                    }
                    method.invoke(this, playerId, funParams);
                    sendResult(playerId, true);
                    break;
                }
            }
            if (!find) {
                sendResult(playerId, false);
            }
        } catch (Exception e) {
            ServerLogger.err(e, String.format("handle gm err by:%d ,param:%s", playerId, gm));
            sendResult(playerId, false);
        }
    }

    public void sendResult(int playerId, boolean ok) {
        ChatVo vo = new ChatVo();
        vo.channel = ChatExtension.WORLD;
        vo.content = ok ? "GM Success!Please Login Again." : "GM Error,Check again!";
        vo.sender = "GM";

        ChatInfoList chats = new ChatInfoList();
        chats.chatInfoVoList = new ArrayList<ChatVo>();
        chats.chatInfoVoList.add(vo);
        chats.errorCode = Response.SUCCESS;
        SessionManager.getInstance().sendMsg(ChatExtension.CHAT, chats, playerId);
    }

    public void sendMsg(int playerId, String... msg) {
        ChatVo vo = new ChatVo();
        vo.channel = ChatExtension.WORLD;
        vo.content = msg[0];
        vo.sender = "GM";
        vo.broadcast = true;

        ListParam<ChatVo> chats = new ListParam<ChatVo>();
        chats.params = new ArrayList<ChatVo>();
        chats.params.add(vo);
        SessionManager.getInstance().sendMsgToAll(ChatExtension.CHAT, chats);
    }

    public void refreshShop(int playerId, String... params) {
        shopService.refreshCommon();
    }

    // 加经验
    public void addExp(int playerId, String... params) {
        int exp = Integer.valueOf(params[0]);
        playerService.addExp(playerId, exp, LogConsume.GM);
    }

    // 完成任务
    public void doTask(int playerId, String... param) {
        int type = Integer.valueOf(param[0]);
        int[] params = new int[param.length - 1];
        for (int i = 0; i < params.length; i++) {
            params[i] = Integer.valueOf(param[i + 1]);
        }
        taskService.doTask(playerId, type, params);
    }

    // 设置任务
    public void setTask(int playerId, String... param) {
        int id = Integer.valueOf(param[0]);
        int count = Integer.valueOf(param[1]);
        Task task = taskService.getPlayerTask(playerId).getTasks().get(id);
        task.setCount(count);
        taskService.checkFinished(task, playerId);
        taskService.updateTaskToClient(playerId, task);
    }

    public void addTask(int playerId, String... param) {
        int id = Integer.valueOf(param[0]);
        taskService.addNewTask(playerId, id);
    }

    // 加任何物品
    public void addGoods(int playerId, String... param) {
        int id = Integer.valueOf(param[0]);
        int count = Integer.valueOf(param[1]);
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        rewards.add(new GoodsEntry(id, count));
        goodsService.addRewards(playerId, rewards, LogConsume.GM);
    }

    // 加任何物品
    public void decGoods(int playerId, String... param) {
        int id = Integer.valueOf(param[0]);
        int count = Integer.valueOf(param[1]);
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        rewards.add(new GoodsEntry(id, count));
        goodsService.decConsume(playerId, rewards, LogConsume.GM);
    }

    // 发送系统消息
    public void sendSysMsg(int playerId, String... msg) {
        messageService.sendSysMsg(MessageService.SYS, msg[0]);
    }

    // 发送系统邮件
    public void sendSysMail(int playerId, String... param) {
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        if (param != null) {
            String[] item = param[0].split(";");
            for (String reward : item) {
                String[] info = reward.split(":");
                rewards.add(new GoodsEntry(Integer.valueOf(info[0]), Integer.valueOf(info[1])));
            }
        }
        mailService.sendSysMail("gm mail", "gm mail", rewards, playerId, LogConsume.GM);
    }

    //变强
    public void strong(int playerId, String... param) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (Object o : ConfigData.getConfigs(CopyConfig.class)) {
            CopyConfig cfg = (CopyConfig) o;
            if (cfg.type == CopyInstance.TYPE_COMMON || cfg.type == CopyInstance.TYPE_EXPERIENCE) {
                Copy copy = new Copy();
                copy.setState(3);
                data.getCopys().put(cfg.id, copy);
            }
        }
        CopyInfo info = copyService.getCopyInfo(playerId);
        SessionManager.getInstance().sendMsg(CopyExtension.CMD_REFRESH, info, playerId);
        playerService.addExp(playerId, 100000, LogConsume.GM);
        //开启所有功能
        for (Object object : ConfigData.getConfigs(ModuleOpenCfg.class)) {
            ModuleOpenCfg cfg = (ModuleOpenCfg) object;
            if (!data.getModules().contains(cfg.id)) {
                data.getModules().add(cfg.id);
            }
            data.getHitModulesState().put(cfg.id, 1);
        }
    }

    //通关指定副本
    public void strongId(int playerId, String... param) {
        PlayerData data = playerService.getPlayerData(playerId);
        int idStart = Integer.parseInt(param[0]);
        int idEnd = Integer.parseInt(param[1]);
        for (int i = idStart; i <= idEnd; i++) {
            Copy copy = new Copy();
            copy.setState(3);
            data.getCopys().put(i, copy);
        }
        CopyInfo info = copyService.getCopyInfo(playerId);
        SessionManager.getInstance().sendMsg(CopyExtension.CMD_REFRESH, info, playerId);
    }

    //重置每日数据
    public void resetDaily(int playerId, String... params) {
        PlayerData data = playerService.getPlayerData(playerId);
        dailyService.resetDailyData(data);
        dailyService.resetWeekly();
        dailyService.refreshDailyVo(playerId);
        playerService.addEnergy(playerId, 120, LogConsume.BUY_ENERGY);
        activityService.resetDailyData();
        gangService.daily();
    }

    // 重启更新服务器
    public void restart(int playerId, String... params) {
        Runtime rt = Runtime.getRuntime();
        try {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("win")) {
                String[] stopCmd = new String[]{"cmd.exe", "/C", "start restart.bat"};
                rt.exec(stopCmd);
            } else {
                String[] stopCmd = new String[]{"/bin/sh", "-c", "nohup ./restart.sh > myout.file 2>&1 &"};
                Runtime.getRuntime().exec(stopCmd);
            }
        } catch (IOException e) {
            ServerLogger.err(e, "restart err!");
        }
    }

    public void addFashion(int playerId, String... params) {
        fashionService.addFashion(playerId, Integer.valueOf(params[0]), 0);
    }

    public void arena(int playerId, String... params) {
        if (params[0].equals("getInfo")) {
            arenaExtension.getInfo(playerId, null);
        } else if (params[0].equals("getOpponent")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            arenaExtension.getOpponentList(playerId, param);
        } else if (params[0].equals("challenge")) {
            Int2Param param = new Int2Param();
            param.param1 = Integer.parseInt(params[1]);
            param.param2 = 1;
            arenaExtension.challenge(playerId, param);
        } else if (params[0].equals("result")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            arenaExtension.takeResult(playerId, param);
        }
    }

    public void experience(int playerId, String... params) {
        if (params[0].equals("getInfo")) {
            trainingExtension.getInfo(playerId, null);
        } else if (params[0].equals("challenge")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            //trainingExtension.challenge(playerId, param);
        } else if (params[0].equals("win")) {
            TrainingResultVO param = new TrainingResultVO();
            param.index = Integer.parseInt(params[1]);
            param.hp = Integer.parseInt(params[2]);
            param.victory = true;
            trainingExtension.challengeWin(playerId, param);
        } else if (params[0].equals("reward")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            trainingExtension.takeReward(playerId, param);
        }
    }

    public void skill(int playerId, String... params) {
        List<Integer> ids = new ArrayList<Integer>();
        for (String p : params) {
            ids.add(Integer.parseInt(p));
        }
        skillService.composeCard(playerId, ids);
    }

    @Autowired
    private GangExtension gangExtension;

    public void gang(int playerId, String... params) {
        if (params[0].equals("create")) {
            String2Param param = new String2Param();
            param.param1 = params[1];
            param.param2 = params[2];
            gangExtension.create(playerId, param);
        } else if (params[0].equals("myGang")) {
            System.out.println(gangExtension.getMyGang(playerId, null).basicInfo.id);
        } else if (params[0].equals("apply")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            gangExtension.apply(playerId, param);
        } else if (params[0].equals("list")) {
            IntParam param = new IntParam();
            param.param = Integer.parseInt(params[1]);
            gangExtension.getGangList(playerId, param);
        } else if (params[0].equals("training1")) {
            gangExtension.getTrainingInfo(playerId, null);
        } else if (params[0].equals("training2")) {
            gangExtension.launchTraining(playerId, toIntParam(params[1]));
        } else if (params[0].equals("training3")) {
            gangExtension.closeTraining(playerId, null);
        } else if (params[0].equals("training4")) {
            gangExtension.startTraining(playerId, null);
        } else if (params[0].equals("training5")) {
            gangExtension.takeTrainingReward(playerId, null);
        } else if (params[0].equals("training")) {
            ListParam<Reward> result = new ListParam<Reward>();
            Player player = playerService.getPlayer(playerId);
            Gang gang = gangService.getGang(player.getGangId());
            GMember member = gang.getMembers().get(playerId);
            GTRoom room = gang.getGtRoom();
            GangTrainingCfg cfg = GameData.getConfig(GangTrainingCfg.class, room.getId());
            float plus = Math.min(room.getMax() * cfg.rewardPlus[1], cfg.rewardPlus[0]);
            int hour = Integer.parseInt(params[1]) / 60;
            if (hour < 1) {
                return;
            }
            int max = (int) (cfg.maxTime - (member.getStartTraining() - room.getCreateTime()) / TimeUtil.ONE_MIN / 60);
            if (hour > max) {
                hour = max;
            }
            if (hour + (int) member.getTrainingTime() > cfg.validTime) {
                hour = cfg.validTime - (int) member.getTrainingTime();
            }
            member.alterTrainingTime(hour);
            int[][] rewards = Arrays.copyOfRange(cfg.reward, 0, cfg.reward.length);
            if (rewards != null) {
                for (int[] reward : rewards) {
                    reward[1] = (int) (reward[1] * plus * hour);
                }
                member.setStartTraining(0L);

                result.params = new ArrayList<Reward>();
                goodsService.addRewards(playerId, rewards, LogConsume.GANG_TRAINING_REWARD, room.getId(), member.getTrainingTime() - hour);
                for (int[] reward : rewards) {
                    Reward re = new Reward();
                    re.id = reward[0];
                    re.count = reward[1];
                    result.params.add(re);
                }
            }
            SessionManager.getInstance().sendMsg(2527, result, playerId);
        }
    }

    @Autowired
    private TaskExtension taskExtension;

    public void task(int playerId, String... params) {
        String cmd = params[0];
        if (cmd.equals("get")) {

        } else if (cmd.equals("submit")) {
            Int2Param param = new Int2Param();
            param.param1 = Integer.parseInt(params[1]);
            param.param2 = Integer.parseInt(params[1]);
            taskExtension.submit(playerId, param);
        } else if (cmd.equals("invite")) {
            taskExtension.inviteJoint(playerId, toInt2Param(params[1], params[2]));
        } else if (cmd.equals("accept")) {
            taskExtension.acceptJoint(playerId, toInt2Param(params[1], params[2]));
        }
    }

    public void trsing(int playerId, String... params) {
        String cmd = params[0];
        if (cmd.equals("info")) {
            traversingExtension.getInfo(playerId, null);
        } else if (cmd.equals("chellenge")) {
            traversingExtension.singleChellenge(playerId, toIntParam(params[1]));
        } else if (cmd.equals("reward")) {
            copyExtension.getRewards(playerId, new CopyResult());
        }
    }

    @Autowired
    private LotteryExtension lotteryLogic;

    public void lottery(int playerId, String... params) {
        String cmd = params[0];
        if (cmd.equals("get")) {
            lotteryLogic.getInfo(playerId, null);
        } else if (cmd.equals("take")) {
            lotteryLogic.takeReward(playerId, toInt2Param(params[1], params[2]));
        }
    }

    @Autowired
    private EndlessLogic endlessLogic;

    public void endless(int playerId, String... params) {
        int layer = Integer.parseInt(params[0]);
        EndlessAttach attack = endlessLogic.getAttach(playerId);
        attack.setChallenge(1);
        attack.setCurrLayer(layer);
        attack.setMaxLayer(layer);
        attack.commitSync();
    }


    IntParam toIntParam(String param1) {
        IntParam param = new IntParam();
        param.param = Integer.parseInt(param1);
        return param;
    }


    Int2Param toInt2Param(String param1, String param2) {
        Int2Param param = new Int2Param();
        param.param1 = Integer.parseInt(param1);
        param.param2 = Integer.parseInt(param2);
        return param;
    }

    public void openWorld(int playerId, String... params) {
        worldBossService.gmReset();
    }

    public void sendAward(int playerId, String... params) {
        worldBossService.sendAward1();
    }

    @Autowired
    private FameService fameService;

    public void addCampExp(int playerId, String... params) {
        int camp = Integer.valueOf(params[0]);
        int exp = Integer.valueOf(params[1]);
        fameService.addFame(playerId, camp, exp);
    }

    @Autowired
    private ShopService shopService;

    public void buy(int playerId, String... params) {
        int id = Integer.valueOf(params[0]);
        shopService.buy(playerId, id, 1);
    }


    public void send(int playerId, String... params) {
        StringParam param = new StringParam();
        param.param = params[0];
        SessionManager.getInstance().sendMsgToAll(ChatExtension.SYS_NOTICE, param);
    }

    public void sendNotice(int playerId, String... params) {
        StringParam param = new StringParam(

        );
        param.param = params[0];
        SessionManager.getInstance().sendMsgToAll(ChatExtension.SYS_NOTICE, param);
    }

    public void addLadderScore(int playerId, String... params) {
        int score = Integer.valueOf(params[0]);
        ladderService.gmAddScore(playerId, score);
    }

    public void ladderRankSort(int playerId, String... params) {
        ladderService.gmSort();
    }

    @Autowired
    private ArenaLogic arenaLogic;

    public void sendAreanReward(int playerId, String... params) {
        arenaLogic.sendRankReward();
    }

    public void ladderReward(int playerId, String... params) {
        ladderService.weeklyAward();
    }

    public void addSkillCard(int playerId, String... params) {
        StringParam param = new StringParam();
        int id = Integer.valueOf(params[0]);
        skillService.gmAddSkillCard(playerId, id);
    }

    @Autowired
    private PetService petService;

    public void addPetPlayTimes(int playerId, String... params) {
        int type = Integer.valueOf(params[0]);
        int times = Integer.valueOf(params[1]);
        petService.gmAddPetPlayTimes(playerId, type, times);
    }

    public void addPet(int playerId, String... params) {
        int petId = Integer.valueOf(params[0]);
        petService.addPet(playerId, petId);
    }

    public void addAllPet(int playerId, String... params) {
        petService.addAllPet(playerId);
    }

    public void getPetActivity(int playerId, String... params) {
        petService.getPetActivity(playerId);
    }

    public void startPetActivity(int playerId, String... params) {
        int activityId = Integer.valueOf(params[0]);
        int petId = Integer.valueOf(params[1]);
        Int2Param param = new Int2Param();
        param.param1 = activityId;
        param.param2 = petId;
        StartPetActivityVO vo = new StartPetActivityVO();
        vo.petActivitys = Lists.newArrayList();
        //vo.petActivitys.add(param);
        petService.startPetActivity(playerId, vo);
    }

    public void finishPetActivity(int playerId, String... params) {
        int activityId = Integer.valueOf(params[0]);
        // petService.finishPetActivity(playerId, activityId);
        petService.getPetActivityRewards(playerId, activityId);
    }

    public void addPetMaterial(int playerId, String... params) {
        int petId = Integer.valueOf(params[0]);
        int count = Integer.valueOf(params[1]);
        petService.addPetMaterial(playerId, petId, count, true);
    }

    public void improveQuality(int playerId, String... params) {
        int petId = Integer.valueOf(params[0]);
        petService.improveQuality(playerId, petId, 0);
    }

    /**
     * public void mutate(int playerId, String... params) {
     * int mutateID = Integer.valueOf(params[0]);
     * int consumeID = Integer.valueOf(params[1]);
     * int count = Integer.valueOf(params[2]);
     * Int2Param p = new Int2Param();
     * int itemId = Integer.valueOf(params[3]);
     * petService.mutate(playerId, mutateID, Lists.newArrayList(p), itemId);
     * }
     * <p>
     * public void decompose(int playerId, String... params) {
     * int petId = Integer.valueOf(params[0]);
     * petService.decompose(playerId, petId);
     * }
     * <p>
     * public void compound(int playerId, String... params) {
     * int petId = Integer.valueOf(params[0]);
     * int count = Integer.valueOf(params[1]);
     * petService.compound(playerId, petId, count);
     * }
     **/
    public void gainPet(int playerId, String... params) {
        int petId = Integer.valueOf(params[0]);
        petService.gainPet(playerId, petId);
    }

    public void ladderDebug(int playerId, String... params) {
        int d = Integer.valueOf(params[0]);
        ladderService.gmDebug(d);
    }

    @Autowired
    private ActivityService activityService;

    public void getOpenActivitys(int playerId, String... params) {
        activityService.getPlayerActivitys(playerId, null);
    }

    public void getActivityAwards(int playerId, String... params) {
        int taskId = Integer.valueOf(params[0]);
        activityService.getActivityAwards(playerId, taskId);
    }

    public void openActivity(int playerId, String... params) {
        int activityId = Integer.valueOf(params[0]);
        activityService.openActivity(playerId, activityId);
    }

    public void getAwardAgain(int playerId, String... params) {
        int taskId = Integer.valueOf(params[0]);
        activityService.fixedActivityAwards(playerId, taskId);
    }

    @Autowired
    private GangDungeonService gangDungeonService;

    public void gangCopyReset(int playerId, String... params) {
        gangDungeonService.gmGangCopyRest(playerId);
    }

    @Autowired
    private WelfareCardService welfareCardService;

    public void welfare(int playerId, String... params) {
        welfareCardService.daily(playerId);
    }

    @Autowired
    private TitleService titleService;

    public void getTitles(int playerId, String... params) {
        ListParam result = titleService.getTitles(playerId);
        System.out.println(JsonUtils.object2String(result));
    }

    public void equipTitle(int playerId, String... params) {
        int taskId = Integer.valueOf(params[0]);
        Int2Param result = titleService.equipTitle(playerId, taskId);
        System.out.println(JsonUtils.object2String(result));
    }

    public void reloadConfig(int playerId, String... params) throws Exception {
        GameData.loadConfigData();
        ConfigData.init();
    }

    // 重启更新服务器
    public void reload(int playerId, String... params) {
        Runtime rt = Runtime.getRuntime();
        try {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("win")) {
                String[] stopCmd = new String[]{"cmd.exe", "/C", "start reload.bat"};
                rt.exec(stopCmd);
            } else {
                String[] stopCmd = new String[]{"/bin/sh", "-c", "nohup ./reload.sh > myout.file 2>&1 &"};
                Runtime.getRuntime().exec(stopCmd);
            }
        } catch (IOException e) {
            ServerLogger.err(e, "restart err!");
        }
    }

    // 重置服务器时间
    public void datetime(int playerId, String... params) {
        Runtime rt = Runtime.getRuntime();
        String date = params[0] + " " + params[1];
        try {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("win")) {
            } else {
                String[] stopCmd = new String[]{"/bin/sh", "-c", "nohup ./datetime.sh " + date};
                Runtime.getRuntime().exec(stopCmd);
            }
        } catch (IOException e) {
            ServerLogger.err(e, "restart err!");
        }
    }

    public void vip(int playerId, String... params) {
        int id = Integer.valueOf(params[0]);
        int count = Integer.valueOf(params[1]);
        vipService.addCharge(playerId, id, 1, "test", "CNY", System.currentTimeMillis() + "", SysConfig.serverId);
    }

    public void taskRank(int playerId, String... params) {
        taskService.dailyReset(playerId);
    }

    @Autowired
    trainingLogic trainin;

    public void resetOpponent(int playerId, String... params) {
        trainin.resetOpponent();
    }

    @Autowired
    ArtifactService artifactService;

    public void artifact(int playerId, String... params) {
        artifactService.gmArtifact(playerId);
    }

    public void clearGuildTime(int playerId, String... params) {
        gangService.clearGuildTime(playerId);
    }

    public void gangUpgrade(int playerId, String... params) {
        gangService.addGangPoint(playerId);
    }

    public void getAllFashion(int playerId, String... params) {
        fashionService.getAllFashion(playerId);
    }

    @Autowired
    RankService rankService;

    public void stateRank(int playerId, String... params) {
        rankService.stateRank();
    }

    public void addContribute(int playerId, String... params) {
        gangService.addContribute(playerId, Integer.valueOf(params[0]));
    }

    public void fastPass(int playerId, String... params) {
        CopyResult result = new CopyResult();
        result.id = Integer.valueOf(params[0]);
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
            return;
        }
        playerData.setGm(true);
        copyExtension.getRewards(playerId, result);
        SessionManager.getInstance().sendMsg(CopyExtension.TAKE_COPY_REWARDS, result, playerId);
    }
}
