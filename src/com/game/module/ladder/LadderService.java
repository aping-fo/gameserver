package com.game.module.ladder;

import com.game.data.ErrCode;
import com.game.data.LadderCfg;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.serial.SerialDataService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.ladder.LadderVO;
import com.game.params.rank.LadderRankVO;
import com.game.params.scene.SkillHurtVO;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.game.util.TimerService;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/9/13.
 * 排位赛
 */
@Service
public class LadderService implements InitHandler {

    //大段里面小段为
    private final static int STAGE_NUM = 3;
    //段位信息
    private final static int CMD_LADDER_INFO = 6007;
    //匹配失败
    private final static int CMD_MATCHING_FAIL = 6006;
    //匹配成功
    private final static int CMD_START_GAME = 6004;

    @Autowired
    private TimerService timerService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailService mailService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private GoodsService goodsService;
    private List<Ladder> ladders = new ArrayList<>();
    /**
     * ID生成
     */
    private final AtomicInteger IdGen = new AtomicInteger(100);
    /**
     * 匹配房间,玩家ID 对房间
     */
    private final Map<Integer, Room> allRooms = new ConcurrentHashMap<>();
    /**
     * 房间类型--- <房间人数---房间>
     */
    private final Map<Integer, Map<Integer, List<Room>>> matchRooms = new ConcurrentHashMap<>();
    /**
     * 排行榜信息
     */
    private final ListParam<LadderRankVO> LADDER_RANK = new ListParam();

    @Override
    public void handleInit() {
        //5S定时
        timerService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    doMatching();
                } catch (Exception e) {
                    ServerLogger.err(e, "排位赛匹配异常");
                }
            }
        }, 10, 4, TimeUnit.SECONDS);
    }

    /**
     * 匹配
     */
    private void doMatching() {
        for (Room source : allRooms.values()) {
            if (source.matchFlag) {
                continue;
            }
            if (source.time == 8) { // 匹配失败
                matchingFail(source);
            }
            for (Room target : allRooms.values()) {
                if (source.exitFlag || target.exitFlag || target.matchFlag) {
                    break;
                }
                if (source.id == target.id) {
                    continue;
                }

                if (source.time == 0) { //-5%
                    if (target.score <= source.score && target.score >= source.score * (1 - 0.05)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 1) { // 5%
                    if (target.score >= source.score && target.score <= source.score * (1 + 0.05)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 2) { // -15%
                    if (target.score <= source.score && target.score >= source.score * (1 - 0.15)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 3) { // 15%
                    if (target.score >= source.score && target.score <= source.score * (1 + 0.15)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 4) { // -35%
                    if (target.score <= source.score && target.score >= source.score * (1 - 0.35)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 5) { // 35%
                    if (target.score >= source.score && target.score <= source.score * (1 + 0.35)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 6) { // -60%
                    if (target.score <= source.score && target.score >= source.score * (1 - 0.6)) {
                        startGame(source, target);
                        break;
                    }
                } else if (source.time == 7) { // 60%
                    if (target.score >= source.score && target.score <= source.score * (1 + 0.6)) {
                        startGame(source, target);
                        break;
                    }
                } else {
                    matchingFail(source);
                }
                source.time += 1;
            }
        }
    }

    /**
     * 匹配失败
     *
     * @param source
     */
    private void matchingFail(Room source) {
        allRooms.remove(source.id);
        IntParam param = new IntParam();
        for (int playerId : source.roomPlayers.keySet()) {
            SessionManager.getInstance().sendMsg(CMD_MATCHING_FAIL, param, playerId);
        }
    }

    /**
     * 开始游戏
     *
     * @param source
     * @param target
     */
    private void startGame(Room source, Room target) {
        source.matchFlag = true;
        target.matchFlag = true;

        IntParam param = new IntParam();
        param.param = Response.SUCCESS;
        source.roomPlayers.putAll(target.roomPlayers);
        int roomId = source.id;
        for (int playerId : source.roomPlayers.keySet()) {
            Player player = playerService.getPlayer(playerId);
            player.setRoomId(roomId);
            SessionManager.getInstance().sendMsg(CMD_START_GAME, param, playerId);
        }
    }

    /**
     * 获取排位赛相关信息
     *
     * @param playerId
     */
    public LadderVO getLadderInfo(int playerId) {
        Ladder ladder = serialDataService.getData().getLadderMap().get(playerId);
        if (ladder == null) {
            ladder = new Ladder();
            serialDataService.getData().getLadderMap().put(playerId, ladder);
        }

        LadderVO vo = new LadderVO();
        vo.score = ladder.getScore();
        vo.level = ladder.getLevel();
        return vo;
    }

    /**
     * 段位更新
     *
     * @param playerId
     * @param ladder
     */
    public void pushLadderInfo(int playerId, Ladder ladder) {
        LadderVO vo = new LadderVO();
        vo.score = ladder.getScore();
        vo.level = ladder.getLevel();
        SessionManager.getInstance().sendMsg(CMD_LADDER_INFO, vo, playerId);
    }

    /**
     * 开始匹配
     *
     * @param playerId
     */
    public IntParam startMatching(int playerId) {
        IntParam param = new IntParam();
        if (!checkOpen()) {
            param.param = Response.TEAM_TIME_OVER;
            return param;
        }
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getLadderTimes() < 1) {
            param.param = Response.NO_TODAY_TIMES;
            return param;
        }
        Ladder ladder = serialDataService.getData().getLadderMap().get(playerId);
        if (ladder == null) {
            ladder = new Ladder();
            serialDataService.getData().getLadderMap().put(playerId, ladder);
        }
        Player player = playerService.getPlayer(playerId);
        Room room = new Room(IdGen.getAndDecrement(), ladder.getScore(), 0);
        room.roomPlayers.put(playerId, new RoomPlayer(player.getHp()));

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 取消匹配
     *
     * @param playerId
     */
    public IntParam cancelMatching(int playerId) {
        Room room = allRooms.get(playerId);
        room.exitFlag = true;
        room.remove(playerId);
        IntParam param = new IntParam();
        param.param = Response.SUCCESS;
        return param;
    }


    public void onGameOver(Room room, Player winPlayer, Player failPlayer) {
        winPlayer.setRoomId(0);
        failPlayer.setRoomId(0);

        Ladder winLadderInfo = serialDataService.getData().getLadderMap().get(winPlayer.getPlayerId());
        Ladder failLadderInfo = serialDataService.getData().getLadderMap().get(failPlayer.getPlayerId());

        int winStage = winLadderInfo.getLevel() / STAGE_NUM + 1;
        int failStage = failLadderInfo.getLevel() / STAGE_NUM + 1;

        LadderCfg winCfg = ConfigData.getConfig(LadderCfg.class, winLadderInfo.getLevel());
        LadderCfg failCfg = ConfigData.getConfig(LadderCfg.class, failLadderInfo.getLevel());

        List<GoodsEntry> winRewards = new ArrayList<>();
        for (int i = 0; i < winCfg.PkReward.length; i += 2) {
            winRewards.add(new GoodsEntry(winCfg.PkReward[i], winCfg.PkReward[i + 1]));
        }
        List<GoodsEntry> failRewards = new ArrayList<>();
        for (int i = 0; i < failCfg.PkReward.length; i += 2) {
            winRewards.add(new GoodsEntry(failCfg.PkReward[i], Math.round(failCfg.PkReward[i + 1] * 0.5f)));
        }

        int winScore = winCfg.addScore;
        int failScore = winCfg.decScore;
        if (winStage - failStage >= ConfigData.globalParam().PkProtect) { //段位保护
            failScore = 0;
        }
        if (winStage < failStage - 1) { //加分加成，同时失败减分加成
            winScore = Math.round(winScore * (1 + ConfigData.globalParam().PkAdd));
            failScore = Math.round(failScore * (1 + ConfigData.globalParam().PkDec));
        }

        goodsService.addGoodsToBag(winPlayer.getPlayerId(), winRewards, LogConsume.LADDER_AWARD);
        goodsService.addGoodsToBag(failPlayer.getPlayerId(), failRewards, LogConsume.LADDER_AWARD);

        winLadderInfo.setScore(winLadderInfo.getScore() + winScore);
        int score = failLadderInfo.getScore() - failScore > 0 ? failLadderInfo.getScore() - failScore : 0;
        failLadderInfo.setScore(score);

        calcLadderLevelUp(winPlayer, winLadderInfo);
        calcLadderLevelUp(failPlayer, failLadderInfo);

        allRooms.remove(room.id);
    }

    /**
     * 计算升级或者降级
     *
     * @param player
     * @param ladderInfo
     */
    private void calcLadderLevelUp(Player player, Ladder ladderInfo) {
        int newLevel = 0;
        int level = ladderInfo.getLevel();
        int totalScore = ladderInfo.getScore();
        int maxLevel = ConfigData.getConfigs(LadderCfg.class).size();
        for (int i = 0; i <= maxLevel; i++) {
            LadderCfg cfg = ConfigData.getConfig(LadderCfg.class, i);
            if (totalScore < cfg.score) {
                break;
            }
            newLevel += 1;
            totalScore -= cfg.score;
        }
        if (newLevel != level) {
            ladderInfo.setLevel(newLevel);
            pushLadderInfo(player.getPlayerId(), ladderInfo);
        }
    }


    /**
     * 掉线，退出处理
     *
     * @param playerId
     */
    public void onLogout(int playerId) {
        Player failPlayer = playerService.getPlayer(playerId);
        Room room = allRooms.get(failPlayer.getRoomId());
        if (room == null) {
            return;
        }
        Player winPlayer = null;
        for (int id : room.roomPlayers.keySet()) {
            if (id != playerId) {
                winPlayer = playerService.getPlayer(id);
            }
        }
        if (winPlayer == null) {
            return;
        }
        onGameOver(room, winPlayer, failPlayer);
    }

    public void ladderSort() {
        if (serialDataService.getData() != null) {
            ServerLogger.warn("ladder sort ...........");
            List<Ladder> list = new ArrayList<>(serialDataService.getData().getLadderMap().values());
            Collections.sort(list, COMPARATOR);
            LADDER_RANK.params = new ArrayList<>();
            for (Ladder ladder : list) {
                Player player = playerService.getPlayer(ladder.getPlayerId());
                if (player == null) {
                    continue;
                }

                LadderRankVO vo = new LadderRankVO();
                vo.name = player.getName();
                vo.level = ladder.getLevel();
                vo.vocation = player.getVocation();
                vo.score = ladder.getScore();

                LADDER_RANK.params.add(vo);
            }
        }
    }

    public ListParam getLadderRank() {
        return LADDER_RANK;
    }

    /**
     * 检测活动是否开启
     *
     * @return
     */
    public boolean checkOpen() {
        String beginDateStr = ConfigData.globalParam().PKBeginDate;
        String endDateStr = ConfigData.globalParam().PKEndDate;

        Date beginDate = TimeUtil.parseDateTime(beginDateStr).getTime();
        Date endDate = TimeUtil.parseDateTime(endDateStr).getTime();

        Date today = new Date();
        if (today.after(endDate) || today.before(beginDate)) {
            return false;
        }
        return TimeUtil.checkTimeIn(ConfigData.globalParam().PKTime);
    }

    // 玩家技能处理
    public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
        Room room = allRooms.get(player.getRoomId());
        if (room != null && hurtVO.targetType == 0) {
            RoomPlayer roomPlayer = room.getRoomPlayer(hurtVO.targetId);
            roomPlayer.decreaseHp(hurtVO.hurtValue);
            if (roomPlayer.checkDeath()) { //屎了?
                Player failPlayer = player;
                Player winPlayer = null;
                for (int id : room.roomPlayers.keySet()) {
                    if (id != player.getPlayerId()) {
                        winPlayer = playerService.getPlayer(id);
                    }
                }
                if (winPlayer == null) {
                    return;
                }
                onGameOver(room, winPlayer, failPlayer);
            }
        }
    }


    /**
     * 每周一定时奖励
     */
    public void weeklyAward() {
        if (!checkOpen()) {
            return;
        }

        ServerLogger.warn("send ladder reward...........");
        String awardTitle = ConfigData.getConfig(ErrCode.class, Response.LADDER_MAIL_TITLE).tips;
        String awardContent = ConfigData.getConfig(ErrCode.class, Response.LADDER_MAIL_CONTENT).tips;
        List<GoodsEntry> rewards = new ArrayList<>();
        for (Ladder ladder : ladders) {
            LadderCfg cfg = ConfigData.getConfig(LadderCfg.class, ladder.getLevel());
            for (int i = 0; i < cfg.FinalReward.length; i += 2) {
                rewards.add(new GoodsEntry(cfg.FinalReward[i], cfg.FinalReward[i + 1]));
            }
            String content = String.format(awardContent, ladder.getLevel());

            ladder.setLastTime(0);
            ladder.setLevel(1);
            ladder.setScore(0);
            mailService.sendSysMail(awardTitle, content, rewards, ladder.getPlayerId(), LogConsume.LADDER_AWARD);
        }
    }

    /**
     * 排序
     */
    private static final Comparator<Ladder> COMPARATOR = new Comparator<Ladder>() {
        @Override
        public int compare(Ladder o1, Ladder o2) {
            if (o1.getScore() == o2.getScore()) {
                return (int) (o2.getLastTime() - o1.getLastTime());
            }
            return o1.getScore() - o2.getScore();
        }
    };
}
