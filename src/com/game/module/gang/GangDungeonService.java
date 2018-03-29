package com.game.module.gang;

import com.game.data.*;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.serial.PlayerView;
import com.game.module.serial.SerialDataService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.gang.*;
import com.game.params.scene.SkillHurtVO;
import com.game.params.worldboss.MonsterHurtVO;
import com.game.util.ConfigData;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lucky on 2017/10/10.
 * <p>
 * 公会副本
 */
@Service
public class GangDungeonService {
    private static final int T_DONT_OPEN = 0; //副本未开启
    public static final int T_OPEN = 1; //副本开启
    public static final int T_PASS = 2; //副本通关
    private static final int CMD_GANG_COPY_INFO = 2528;

    @Autowired
    private GangService gangService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailService mailService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SceneService sceneService;

    /**
     * 获取公会副本信息
     *
     * @param playerId
     */
    public GangCopyVO getGangCopyInfo(int playerId) {
        GangCopyVO vo = new GangCopyVO();
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            vo.errCode = Response.GUILD_NOT_EXIST;
            return vo;
        }
        Gang gang = gangService.getGang(player.getGangId());
        if (gang == null) {
            vo.errCode = Response.GUILD_NOT_EXIST;
            return vo;
        }
        GMember member = gang.getMembers().get(playerId);
        vo.remainTimes = member.getChallengeTimes();

        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        if (gangDungeon == null) {
            gangDungeon = new GangDungeon();
            gangDungeon.setLayer(1);
            serialDataService.getData().getGangMap().putIfAbsent(player.getGangId(), gangDungeon);
        }
        vo.errCode = Response.SUCCESS;
        vo.layer = gangDungeon.getLayer();
        vo.progress = gangDungeon.getProgress();
        vo.hasOpen = gangDungeon.getHasOpen();
        vo.playerId = gangDungeon.fighter;
        if (vo.hasOpen == T_DONT_OPEN) {
            vo.progress = 0;
        }
        return vo;
    }

    /**
     * 开启挑战
     *
     * @param playerId
     */
    public IntParam openChallenge(int playerId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            param.param = Response.GUILD_NOT_EXIST;
            return param;
        }

        Gang gang = gangService.getGang(player.getGangId());
        GMember member = gang.getMembers().get(playerId);
        if (member.getPosition() == Gang.MEMBER) {
            param.param = Response.GANG_NO_PRIVILEGE;
            return param;
        }

        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        if (gangDungeon == null) {
            gangDungeon = new GangDungeon();
            serialDataService.getData().getGangMap().put(player.getGangId(), gangDungeon);
        }

        if (gangDungeon.getHasOpen() == T_OPEN) { //已经开启过了
            param.param = Response.GUILD_COPY_HAS_OPEN;
            return param;
        }

        GangCopyCfg cfg = ConfigData.getConfig(GangCopyCfg.class, gangDungeon.getLayer());
        if (gang.getAsset() < cfg.needCredit) {
            param.param = Response.NO_GANG_ASSET;
            return param;
        }

        gangDungeon.setHasOpen(T_OPEN);
        gang.setAsset(gang.getAsset() - cfg.needCredit);
        gangDungeon.getMonsterMap().clear();
        gangDungeon.getAwardStep().clear();

        List<MonsterRefreshConfig> monsters = ConfigData.GangMonsters.get(cfg.copyId);
        for (MonsterRefreshConfig conf : monsters) {
            MonsterConfig monsterConfig = ConfigData.getConfig(MonsterConfig.class, conf.monsterId);
            Monster m = new Monster();
            m.setId(conf.id);
            m.setMonsterId(conf.monsterId);
            m.setHp(monsterConfig.hp);
            m.setCurrentHp(monsterConfig.hp);
            gangDungeon.getMonsterMap().put(conf.id, m);
        }
        broadcastState(playerId, gang);
        param.param = Response.SUCCESS;
        return param;
    }


    /**
     * 开始挑战
     *
     * @param playerId
     */
    public GangCopyChallengeVO startChallenge(int playerId) {
        GangCopyChallengeVO vo = new GangCopyChallengeVO();
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            vo.errCode = Response.ERR_PARAM;
            return vo;
        }

        Gang gang = gangService.getGang(player.getGangId());
        GMember member = gang.getMembers().get(playerId);
        if (member.getChallengeTimes() < 1) {
            vo.errCode = Response.GUILD_COPY_COUNT_NOT_ENOUGH;
            return vo;
        }

        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        if (gangDungeon.getHasOpen() == T_DONT_OPEN) {
            vo.errCode = Response.GUILD_COPY_DO_NOT_OPEN;
            return vo;
        }

        if (gangDungeon.getHasOpen() == T_PASS) {
            vo.errCode = Response.ERR_PARAM;
            return vo;
        }

        GangCopyCfg cfg = ConfigData.getConfig(GangCopyCfg.class, gangDungeon.getLayer());
        CopyConfig copyConfig = ConfigData.getConfig(CopyConfig.class, cfg.copyId);
        if (player.getLev() < copyConfig.lev) {
            vo.errCode = Response.NO_LEV;
            return vo;
        }
        if (!gangDungeon.checkAndSet(playerId)) {
            vo.errCode = Response.GUILD_COPY_FIGHTING;
            return vo;
        }

        gangDungeon.hasOver = false;
        member.setChallengeTimes(member.getChallengeTimes() - 1);
        member.hp = player.getHp();
        member.hurt = 0;
        vo.errCode = Response.SUCCESS;
        vo.monsters = new ArrayList<>();
        for (Monster m : gangDungeon.getMonsterMap().values()) {
            if (m.getCurrentHp() <= 0) {
                continue;
            }
            MonsterVo svo = new MonsterVo();
            svo.curHp = m.getCurrentHp();
            svo.hp = m.getHp();
            svo.id = m.getId();
            vo.monsters.add(svo);
        }
        broadcastState(playerId, gang);
        return vo;
    }

    public boolean checkDeath(Player player, int monsterId) {
        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        Monster m = gangDungeon.getMonsterMap().get(monsterId);
        if (m == null) {
            return true;
        }
        return m.getCurrentHp() <= 0;
    }

    private static final int CMD_BATTLE_END = 2531; //推送战斗结束
    private static final int CMD_MONSTER_INFO = 4910; //同步怪物相关信息

    // 玩家技能处理
    public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
        if (player.getGangId() == 0) {
            return;
        }
        Gang gang = gangService.getGang(player.getGangId());
        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        GMember member = gang.getMembers().get(player.getPlayerId());
        if (member == null) {
            return;
        }
        if (hurtVO.targetType == 0) { //角色
            member.hp = member.hp - hurtVO.hurtValue;
            if (member.hp <= 0) { //屎了?
                onBattleEnd(player);
            }
        } else { //怪物
            int maxHurt = (int) (player.getFight() * 0.06 * 1.65 * 3 * 2.05 + 4185);
            if (hurtVO.hurtValue > maxHurt) {
                ServerLogger.warn("==================== 作弊玩家 Id = " + player.getPlayerId());
                return;
            }

            Monster m = gangDungeon.getMonsterMap().get(hurtVO.targetId);
            if (m.getCurrentHp() <= 0) {
                ServerLogger.info("==================== Monster Die，Monster Id = " + m.getId());
                return;
            }
            int hp = m.getCurrentHp() - hurtVO.hurtValue > 0 ? m.getCurrentHp() - hurtVO.hurtValue : 0;
            if (hp == 0) {
                MonsterConfig monsterCfg = GameData.getConfig(MonsterConfig.class, m.getMonsterId());
                Map<Integer, int[]> condParams = Maps.newHashMap();
                condParams.put(Task.FINISH_KILL, new int[]{monsterCfg.type, m.getMonsterId(), 1});
                condParams.put(Task.TYPE_KILL, new int[]{monsterCfg.type, 1});
                condParams.put(Task.TYPE_KILL, new int[]{0, 1});
                taskService.doTask(player.getPlayerId(), condParams);
            }
            m.setCurrentHp(hp);
            MonsterHurtVO vo = new MonsterHurtVO();
            vo.actorId = hurtVO.targetId;
            vo.curHp = hp;
            vo.hurt = hurtVO.hurtValue;
            vo.isCrit = hurtVO.isCrit;
            vo.type = 1;
            //SessionManager.getInstance().sendMsg(CMD_MONSTER_INFO, vo, player.getPlayerId());
            sceneService.brocastToSceneCurLine(player, CMD_MONSTER_INFO, vo, null);

            member.hurt += hurtVO.hurtValue;
            if (gangDungeon.checkDeath()) { //怪死了
                ServerLogger.info("====================>>>>>>  All Monster Die,Begin To Send Fight Over");
                int maxLayer = ConfigData.getConfigs(GangCopyCfg.class).size();
                if (gangDungeon.getLayer() < maxLayer) {
                    for (int pid : gang.getMembers().keySet()) { //成就
                        PlayerView playerView = serialDataService.getData().getPlayerView(pid);
                        if (playerView.getGuildLayer() < gangDungeon.getLayer()) {
                            playerView.setGuildLayer(gangDungeon.getLayer());
                        }
                        taskService.doTask(pid, Task.TYPE_GUILD_COPY, gangDungeon.getLayer());
                    }

                    gangDungeon.setHasOpen(T_DONT_OPEN); //怪物全死了，重置
                    gangDungeon.setLayer(gangDungeon.getLayer() + 1);
                } else {
                    gangDungeon.setHasOpen(T_PASS);
                }
                onBattleEnd(player);
            }
        }

        GangCopyCfg cfg = ConfigData.getConfig(GangCopyCfg.class, gangDungeon.getLayer());
        float progress = gangDungeon.getProgress();
        for (int i = 0; i < cfg.progress.length; i++) {
            if (progress >= cfg.progress[i]) {
                int step = i + 1;
                if (gangDungeon.checkAndAdd(step)) {
                    ServerLogger.info("============ send guild copy step award" + cfg.progress[i] + "   progress = " + progress);
                    List<GoodsEntry> rewards = new ArrayList<>();
                    int[][] itemArr = cfg.progressRewards.get(step);
                    for (int[] item : itemArr) {
                        GoodsEntry goodsEntry = new GoodsEntry(item[0], item[1]);
                        rewards.add(goodsEntry);
                    }
                    String title = ConfigData.getConfig(ErrCode.class, Response.GUILD_COPY_MAIL_TITLE).tips;
                    String content = ConfigData.getConfig(ErrCode.class, Response.GUILD_COPY_MAIL_CONTENT).tips;
                    for (int pid : gang.getMembers().keySet()) {
                        mailService.sendSysMail(title, content, rewards, pid, LogConsume.GUILD_COPY_REWARD);
                    }
                    break;
                }
            }
        }
    }

    private void onBattleEnd(Player player) {
        Gang gang = gangService.getGang(player.getGangId());
        GMember member = gang.getMembers().get(player.getPlayerId());

        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(gang.getId());
        if (gangDungeon == null) {
            return;
        }
        if (gangDungeon.hasOver) {
            return;
        }
        gangDungeon.hasOver = true;
        GangHurt hurt = gangDungeon.getHurtMap().get(player.getPlayerId());
        if (hurt == null) {
            hurt = new GangHurt(player.getPlayerId(), player.getName(), player.getVocation());
            hurt.setLevel(player.getLev());
            hurt.setVip(player.getVip());
            hurt.setHurt(member.hurt);
            gangDungeon.getHurtMap().put(player.getPlayerId(), hurt);
        } else {
            hurt.setLevel(player.getLev());
            hurt.setVip(player.getVip());
            gangDungeon.hurtRankMap.remove(hurt);
            hurt.setHurt(hurt.getHurt() + member.hurt);
        }
        gangDungeon.hurtRankMap.put(hurt, hurt);


        GangCopyEndVO vo = new GangCopyEndVO();
        vo.currentHurt = member.hurt;
        vo.totalHurt = hurt.getHurt();
        vo.progress = gangDungeon.getProgress();
        vo.gangContribution = Math.round(member.hurt / (gangDungeon.getTotalHp() * 1.0f) * ConfigData.globalParam().guildRewardRate);
        gangService.addContribute(player.getPlayerId(),vo.gangContribution);
        int rank = 0;
        for (GangHurt gh : gangDungeon.hurtRankMap.values()) {
            if (gh.getPlayerId() == player.getPlayerId()) {
                rank += 1;
                break;
            }
        }
        vo.rank = rank;

        SessionManager.getInstance().sendMsg(CMD_BATTLE_END, vo, player.getPlayerId());
        member.hp = 0;
        member.hurt = 0;
    }

    /**
     * 掉线，退出处理
     *
     * @param playerId
     */
    public void onLogout(int playerId) {
        try {
            onExitDungeon(playerId);
        } catch (Exception e) {
            ServerLogger.err(e, "=========== guild copy ");
        }
    }

    public void dailyReset(int playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            return;
        }

        Gang gang = gangService.getGang(player.getGangId());
        if (gang != null) {
            GMember member = gang.getMembers().get(playerId);
            member.setChallengeTimes(ConfigData.globalParam().guildCopyTimes);
        }
    }

    /**
     * 退出副本
     *
     * @param playerId
     */
    public void onExitDungeon(int playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            return;
        }
        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        if (gangDungeon == null) {
            return;
        }
        if (!gangDungeon.clearFighter(playerId)) {
            return;
        }
        Gang gang = gangService.getGang(player.getGangId());
        if (gang == null) {
            ServerLogger.warn("guild == null? guild id = " + player.getGangId());
            return;
        }
        broadcastState(playerId, gang);
        GMember member = gang.getMembers().get(playerId);
        if (member == null || member.hurt < 1) {
            return;
        }
        onBattleEnd(player);
    }

    /**
     * 获取伤害列表
     *
     * @param playerId
     * @return
     */
    public ListParam<GangHurtVO> getHurtRankList(int playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player.getGangId() == 0) {
            return null;
        }

        ListParam<GangHurtVO> cli = new ListParam<>();
        cli.params = new ArrayList<>();
        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        for (GangHurt gh : gangDungeon.hurtRankMap.values()) {
            if (gh.getHurt() == 0) {
                continue;
            }
            GangHurtVO vo = new GangHurtVO();
            vo.name = gh.getName();
            vo.hurt = gh.getHurt();
            vo.level = gh.getLevel();
            vo.vocation = gh.getVocation();

            cli.params.add(vo);
        }

        return cli;
    }

    /**
     * 每周一定时奖励
     */
    public void weekly() {
        ServerLogger.warn("gang copy reset...........");
        for (GangDungeon gd : serialDataService.getData().getGangMap().values()) {
            gd.reset();
        }
    }

    public void gmGangCopyRest(int playerId) {
        Player player = playerService.getPlayer(playerId);
        GangDungeon gangDungeon = serialDataService.getData().getGangMap().get(player.getGangId());
        if (gangDungeon == null) {
            return;
        }
        gangDungeon.reset();
    }

    private void broadcastState(int playerId, Gang gang) {
        GangCopyVO vo = getGangCopyInfo(playerId);
        if (gang == null || vo == null) {
            ServerLogger.info("gang = " + gang + " copy = " + vo);
            return;
        }
        for (GMember member : gang.getMembers().values()) {
            vo.remainTimes = member.getChallengeTimes();
            SessionManager.getInstance().sendMsg(CMD_GANG_COPY_INFO, vo, member.getPlayerId());
        }
    }
}
