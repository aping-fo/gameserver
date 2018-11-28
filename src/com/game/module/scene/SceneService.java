package com.game.module.scene;

import com.game.SysConfig;
import com.game.data.AwakeningSkillCfg;
import com.game.data.SceneConfig;
import com.game.event.InitHandler;
import com.game.module.awakeningskill.AwakeningSkillService;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.gang.GangDungeonService;
import com.game.module.gang.GangService;
import com.game.module.goods.EquipService;
import com.game.module.group.GroupService;
import com.game.module.ladder.LadderService;
import com.game.module.multi.MultiService;
import com.game.module.pet.Pet;
import com.game.module.pet.PetService;
import com.game.module.player.CheatReventionService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.team.TeamService;
import com.game.module.worldboss.WorldBossService;
import com.game.params.IProtocol;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.scene.*;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SceneService implements InitHandler {
    private static final Logger logger = Logger.getLogger(SceneService.class);
    private static final int LINE_MAX = 9;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private WorldBossService worldBossService;
    @Autowired
    private GangService gangService;
    @Autowired
    private MultiService multiService;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GangDungeonService gangDungeonService;
    @Autowired
    private PetService petService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private AwakeningSkillService awakeningSkillService;

    private Map<Integer, Scene> scenes = new ConcurrentHashMap<Integer, Scene>();

    private Map<String, Integer> useSkills = new ConcurrentHashMap<String, Integer>();

    private Map<Integer, Set<Integer>> lineMap = new ConcurrentHashMap<>();

    // private ExecutorService executors = Executors.newSingleThreadExecutor();

    @Override
    public void handleInit() {
        for (Object scene : GameData.getConfigs(SceneConfig.class)) {
            SceneConfig cfg = (SceneConfig) scene;
            Scene s = new Scene();
            s.setId(cfg.id);
            scenes.put(cfg.id, s);
        }
    }

    // 获取场景配置
    public Scene getScene(int sceneId) {
        return scenes.get(sceneId);
    }

    // 获得场景group key,感觉会把人份到不同组
    public String getGroupKey(Player player) {
        int sceneId = player.getSceneId();
        int subLine = player.getSubLine();

        SceneConfig cfg = ConfigData.getConfig(SceneConfig.class, sceneId);
        if (cfg.sceneSubType == Scene.MULTI_GANG) {
            return String.format("%d_%d_%d", sceneId, player.getGangId(), player.getSubLine());
        } else if (cfg.sceneSubType == Scene.MULTI_TEAM || cfg.sceneSubType == Scene.MULTI_PVE) {
            return String.format("%d_%d_%d", sceneId, player.getTeamId(),
                    player.getSubLine());
        } else if (cfg.sceneSubType == Scene.MULTI_GROUP_ROOM) { //团队副本房间，不需要subline
            return String.format("%d_%d", sceneId, player.getGroupId());
        } else if (cfg.sceneSubType == Scene.MULTI_GROUP) { //团队副本，不需要subline
            return String.format("%d_%d_%d", sceneId, player.getGroupId(), player.getGroupTeamId());
        } else if (cfg.sceneSubType == Scene.MULTI_LADDER) {
            return String.format("%d_%d", sceneId, player.getRoomId());
        } else if (cfg.sceneSubType == Scene.MULTI_GANG_BOSS) {
            return String.format("%d_%d_%d", sceneId, player.getGangId(), player.getSubLine());
        } else {
            return String.format("%d_%d", sceneId, subLine);
        }
    }

    // 推送场景广播
    public void brocastToSceneCurLine(Player player, int cmd, IProtocol param) {
        brocastToSceneCurLine(player, cmd, param, SessionManager.getInstance().getChannel(player.getPlayerId()));
    }

    // 推送场景广播
    public void brocastToSceneCurLine(Player player, int cmd, IProtocol param,
                                      Channel me) {
        int sceneId = player.getSceneId();
        Scene scene = getScene(sceneId);
        if (scene == null) {
            return;
        }

        String key = getGroupKey(player); // 副本的,不处理 SceneConfig cfg =
        SceneConfig cfg = GameData.getConfig(SceneConfig.class, sceneId);
        if (cfg.type != Scene.MULTI) {
            return;
        }

        SessionManager.getInstance().sendMsgToGroup(key, cmd, param, me);
    }

    // 离开场景
    public void exitScene(Player player) {
        if (player.getSubLine() == 0 || player.getSceneId() == 0) {
            return;
        }

        int playerId = player.getPlayerId();
        int sceneId = player.getSceneId();
        Scene lastScene = getScene(sceneId);
        String key = getGroupKey(player);

        //退出世界boss场景
        SceneConfig lastCfg = GameData.getConfig(SceneConfig.class, sceneId);
        if (lastCfg.sceneSubType == Scene.WORLD_BOSS_PVE) {
            worldBossService.removePlayer(player.getPlayerId());
        } else if (lastCfg.sceneSubType == Scene.MULTI_PVE
                || lastCfg.sceneSubType == Scene.MULTI_GANG_BOSS
                || lastCfg.sceneSubType == Scene.MULTI_GROUP) { // 其他多人本 PVE，
            multiService.onExit(player.getPlayerId());
        }

        if (lastCfg.sceneSubType == Scene.MULTI_GROUP) {
            groupService.onExitBattle(playerId);
        } else if (lastCfg.sceneSubType == Scene.MULTI_LADDER
                || lastCfg.sceneSubType == Scene.MULTI_LADDER_AI) {
            ladderService.onLogout(playerId);
        } else if (lastCfg.sceneSubType == Scene.MULTI_GANG_BOSS) {
            gangDungeonService.onLogout(playerId);
        }

        ////主城分线处理
        if (lastCfg.sceneSubType == Scene.MULTI_MAIN_CITY || lastCfg.sceneSubType == Scene.MULTI_CITY) {
            Set<Integer> set = lineMap.get(lastCfg.sceneSubType);
            if (set != null) {
                set.remove(player.getPlayerId());
            }
            removeMe(player, sceneId);
        } else {
            SessionManager.getInstance().removeFromGroup(key, player.getPlayerId());

            Int2Param param = new Int2Param();
            param.param1 = sceneId;
            param.param2 = playerId;

            Channel channel = SessionManager.getInstance().getChannel(
                    player.getPlayerId());
            brocastToSceneCurLine(player, SceneExtension.EXIT_SCENE, param, channel);
        }

        lastScene.exitSubLine(player.getSubLine());
        player.setSubLine(0);
        ServerLogger.info("...exit", playerId, player.getSceneId());

        CheatReventionService.resetRecords(playerId);
    }

    public void removeMe(Player player, int sceneId) {
        Int2Param param = new Int2Param();
        param.param1 = sceneId;
        param.param2 = player.getPlayerId();

        for (int pid : player.seeMeSet) {
            Player otherPlayer = playerService.getPlayer(pid);
            otherPlayer.seeMeSet.remove(player.getPlayerId());
            SessionManager.getInstance().sendMsg(SceneExtension.EXIT_SCENE, param, pid);
            ServerLogger.info("line,to exit = " + pid);
        }
        player.seeMeSet.clear();
    }

    private void canSeeMe(Player player, int sceneType) {
        player.seeMeSet.clear();
        Set<Integer> set = lineMap.get(sceneType);
        if (set == null) {
            return;
        }
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        for (int playerId : set) {
            if (player.seeMeSet.size() >= LINE_MAX) {
                break;
            }
            if (playerId == player.getPlayerId()) {
                continue;
            }
            if (!data.getFriends().containsKey(playerId)) {
                continue;
            }

            Player otherPlayer = playerService.getPlayer(playerId);
            if (otherPlayer.seeMeSet.size() >= LINE_MAX) {
                continue;
            }
            player.seeMeSet.add(playerId);
            otherPlayer.seeMeSet.add(player.getPlayerId());
            ServerLogger.info("line,to add = " + playerId);
        }

        //好友筛选不够
        if (player.seeMeSet.size() < LINE_MAX) {
            for (int playerId : set) {
                if (player.seeMeSet.size() >= LINE_MAX) {
                    break;
                }
                if (playerId == player.getPlayerId()) {
                    continue;
                }
                Player otherPlayer = playerService.getPlayer(playerId);
                if (otherPlayer.seeMeSet.size() >= LINE_MAX) {
                    continue;
                }
                if (data.getFriends().containsKey(playerId)) {
                    continue;
                }
                player.seeMeSet.add(playerId);
                otherPlayer.seeMeSet.add(player.getPlayerId());
                ServerLogger.info("line,to add = " + playerId);
            }
        }

        for (int playerId : player.seeMeSet) {
            SessionManager.getInstance().sendMsg(SceneExtension.ENTER_SCENE, toVo(player), playerId);
            ServerLogger.info("line,to enter = " + playerId);
        }
    }

    // 进入场景
    public void enterScene(Player player, int sceneId, float x, float z) {
        Scene scene = getScene(sceneId);
        if (scene == null) {
            ServerLogger.warn("scene is null , sceneId = " + sceneId);
            return;
        }
        int curScene = player.getSceneId();
        if (curScene == scene.getId() && player.getSubLine() > 0) {// 同一个场景
            return;
        }

        SceneConfig lastCfg = GameData.getConfig(SceneConfig.class, curScene);
        if (lastCfg.type == Scene.CITY || lastCfg.sceneSubType == Scene.MULTI_CITY
                || lastCfg.sceneSubType == Scene.MULTI_GANG || lastCfg.sceneSubType == Scene.MULTI_MAIN_CITY) {
            player.setLastSceneId(curScene);
            player.setLastPos(new float[]{player.getX(), player.getY(),
                    player.getZ()});

        }
        SceneConfig cfg = GameData.getConfig(SceneConfig.class, sceneId);
        if (cfg.type == Scene.CITY || cfg.sceneSubType == Scene.MULTI_CITY
                || cfg.sceneSubType == Scene.MULTI_GANG || cfg.sceneSubType == Scene.MULTI_MAIN_CITY
                || cfg.sceneSubType == Scene.MULTI_GROUP_ROOM) {
            // 清除副本实例
            teamService.quit(player.getPlayerId());
            copyService.removeCopy(player.getPlayerId());
        }

        player.setSceneId(sceneId);
        player.setX(x);
        player.setZ(z);
        player.sethMoveDir(0);
        player.setvMoveDir(0);

        //主城分线处理
        if (cfg.sceneSubType == Scene.MULTI_MAIN_CITY || cfg.sceneSubType == Scene.MULTI_CITY) {
            Set<Integer> set = lineMap.get(cfg.sceneSubType);
            if (set == null) {
                set = Sets.newConcurrentHashSet();
                lineMap.put(cfg.sceneSubType, set);
            }
            set.add(player.getPlayerId());
            canSeeMe(player, cfg.sceneSubType);
            player.setSubLine(9999);
        } else {
            int subLine = scene.getNewSubLine();
            scene.enterSubLine(subLine);
            player.setSubLine(subLine);

            String key = getGroupKey(player);
            Channel channel = SessionManager.getInstance().getChannel(player.getPlayerId());
            SessionManager.getInstance().addToGroup(key, channel);
            ServerLogger.info("line ==========" + key);
            if (cfg.sceneSubType == Scene.WORLD_BOSS_PVE) {
                worldBossService.addPlayer(player.getPlayerId());
            } else if (cfg.sceneSubType == Scene.MULTI_GROUP
                    || cfg.sceneSubType == Scene.MULTI_PVE
                    || cfg.sceneSubType == Scene.MULTI_GANG_BOSS) {
                multiService.onEnter(player.getPlayerId());
            }
            // 广播消息
            brocastToSceneCurLine(player, SceneExtension.ENTER_SCENE, toVo(player), channel);
        }
    }

    // 转成vo
    public SScenePlayerVo toVo(Player player) {
        SScenePlayerVo vo = new SScenePlayerVo();
        vo.playerId = player.getPlayerId();
        vo.name = player.getName();
        vo.hp = player.getHp();
        vo.lev = player.getLev();
        vo.sex = player.getSex();
        vo.vocation = player.getVocation();
        vo.x = player.getX();
        vo.z = player.getZ();
        vo.hMoveDir = player.gethMoveDir();
        vo.vMoveDir = player.getvMoveDir();
        vo.fashionId = player.getFashionId();
        vo.weapon = player.getWeaponId();
        vo.fight = player.getFight();
        vo.title = player.getTitle();
        vo.attack = player.getAttack();
        vo.defense = player.getDefense();
        vo.crit = player.getCrit();
        vo.symptom = player.getSymptom();
        vo.fu = player.getFu();
        vo.vip = player.getVip();

        vo.roomTeam = player.roomTeamId;
        vo.head = playerService.getPlayerData(player.getPlayerId()).getCurHead();
        Pet pet = petService.getFightPet(player.getPlayerId());
        if (pet != null) {
            vo.fightPetConfigId = pet.getConfigId();
            vo.fightPetName = pet.getName();
        }
        Pet showPet = petService.getShowPet(player.getPlayerId());
        if (showPet != null) {
            vo.showPetConfigId = showPet.getShowConfigID();
            vo.showPetName = showPet.getName();
        }
        if (player.getGangId() > 0) {
            vo.gang = gangService.getGang(player.getGangId()).getName();
        }
        List<Integer> list = equipService.getBufferList(player.getPlayerId());
        vo.buffList = Lists.newArrayList(list);
        List<Int2Param> awakeningSkillList = awakeningSkillService.getAwakeningSkillList(player.getPlayerId());
        if (awakeningSkillList != null && awakeningSkillList.size() > 0) {
            vo.awakenSkillList = awakeningSkillList;
        }
        return vo;
    }

    // 获取场景玩家
    public SSceneInfo getSceneInfo(Player player, int sceneId) {
        SceneConfig cfg = GameData.getConfig(SceneConfig.class, sceneId);
        if (cfg == null) {
            ServerLogger.warn("SceneConfig is null, SceneConfig id =" + sceneId);
        }
        SSceneInfo sceneInfo = genMonster(cfg, player);// 生成怪物信息
        sceneInfo.players = new ArrayList<SScenePlayerVo>();
        sceneInfo.sceneId = sceneId;
        if (cfg.type == Scene.COPY) {
            if (sceneInfo.monsters == null || sceneInfo.monsters.isEmpty()) {
                ServerLogger.warn("err enter copy:" + sceneId + " playerId:"
                        + player.getPlayerId());
            }
        }

        List<Integer> list = equipService.getBufferList(player.getPlayerId());
        sceneInfo.bufferList = Lists.newArrayList(list);
        if (cfg.type != Scene.MULTI) {// 副本的只返回自己的
            //sceneInfo.players.add(toVo(player));
            return sceneInfo;
        }

        if (cfg.sceneSubType == Scene.MULTI_MAIN_CITY || cfg.sceneSubType == Scene.MULTI_CITY) {
            for (int playerId : player.seeMeSet) {
                Player p = playerService.getPlayer(playerId);
                if (p != null) {
                    sceneInfo.players.add(toVo(p));
                }
            }
            ServerLogger.info("line players = " + JsonUtils.object2String(player.seeMeSet));
        } else {
            String key = getGroupKey(player);
            Collection<Channel> channels = SessionManager.getInstance().getGroupChannels(key);
            //List<Integer> ids = new ArrayList<Integer>(10);
            for (Channel channel : channels) {
                int playerId = SessionManager.getInstance().getPlayerId(channel);
                if (playerId == 0 || playerId == player.getPlayerId()) {
                    continue;
                }
                Player p = playerService.getPlayer(playerId);
                sceneInfo.players.add(toVo(p));
                //ids.add(playerId);
            }
        }

        return sceneInfo;
    }

    // 生成怪物
    public SSceneInfo genMonster(SceneConfig cfg, Player player) {
        SSceneInfo sceneInfo = new SSceneInfo();
        try {
            List<SMonsterVo> monsters = new ArrayList<SMonsterVo>();
            if (cfg.type == Scene.COPY
                    || (cfg.type == Scene.MULTI &&
                    (cfg.sceneSubType == Scene.MULTI_PVE || cfg.sceneSubType == Scene.WORLD_BOSS_PVE
                            || cfg.sceneSubType == Scene.MULTI_GROUP || cfg.sceneSubType == Scene.MULTI_GANG_BOSS))) {// 普通副本，多人PVE
                int copyInstance = player.getCopyId();
                CopyInstance copy = copyService.getCopyInstance(copyInstance);
                monsters.addAll(copy.getMonsters(cfg.id).values());

                if (cfg.sceneSubType == Scene.MULTI_GANG_BOSS) { //公会副本怪物信息处理
                    monsters.clear();
                    for (SMonsterVo vo : copy.getMonsters(cfg.id).values()) {
                        if (!gangDungeonService.checkDeath(player, vo.id)) {
                            monsters.add(vo);
                        }
                    }
                }

                if (copy.getTraverseMap() != null) {
                    int[] affixs = copy.getTraverseMap().getAffixs();
                    ServerLogger.info("Traverse length: " + affixs.length);
                    if (affixs != null) {
                        sceneInfo.affixs = new ArrayList<Integer>();
                        for (int id : affixs) {
                            sceneInfo.affixs.add(id);
                        }
                    }
                }
            }
            sceneInfo.monsters = monsters;
        } catch (Exception e) {
            logger.error("create monster", e);
            throw new RuntimeException(e);
        }
        return sceneInfo;
    }

    // 场景移动
    public void walk(int playerId, MoveStart vo) {
        Player player = playerService.getPlayer(playerId);
        player.setvMoveDir(vo.vMoveDir);
        player.sethMoveDir(vo.hMoveDir);
        player.setX(vo.x);
        player.setZ(vo.z);

        int curScene = player.getSceneId();
        SceneConfig cfg = GameData.getConfig(SceneConfig.class, curScene);
        if (cfg.sceneSubType == Scene.MULTI_MAIN_CITY || cfg.sceneSubType == Scene.MULTI_CITY) {
            for (int pid : player.seeMeSet) {
                SessionManager.getInstance().sendMsg(SceneExtension.WALK_SCENE, vo, pid);
            }
        } else {
            Channel me = SessionManager.getInstance().getChannel(playerId);
            brocastToSceneCurLine(player, SceneExtension.WALK_SCENE, vo, me);
        }
    }

    // 场景停止移动
    public void stop(int playerId, MoveStop vo) {
        Player player = playerService.getPlayer(playerId);
        if (vo.type == 0) {
            player.setvMoveDir(0);
            player.sethMoveDir(0);
            player.setX(vo.x);
            player.setZ(vo.z);
        }

        int curScene = player.getSceneId();
        SceneConfig cfg = GameData.getConfig(SceneConfig.class, curScene);
        if (cfg.sceneSubType == Scene.MULTI_MAIN_CITY || cfg.sceneSubType == Scene.MULTI_CITY) {
            for (int pid : player.seeMeSet) {
                SessionManager.getInstance().sendMsg(SceneExtension.STOP_WALK_SCENE, vo, pid);
            }
        } else {
            Channel me = SessionManager.getInstance().getChannel(playerId);
            brocastToSceneCurLine(player, SceneExtension.STOP_WALK_SCENE, vo, me);
        }
    }

    // 怪物移动
    public void npcMove(int playerId, NpcMoveStart vo) {
        Player player = playerService.getPlayer(playerId);
        Channel me = SessionManager.getInstance().getChannel(playerId);
        brocastToSceneCurLine(player, SceneExtension.NPC_MOVE, vo, me);
    }


    // 怪物方向
    public void monsterDir(int playerId, Int2Param vo) {
        Player player = playerService.getPlayer(playerId);
        Channel me = SessionManager.getInstance().getChannel(playerId);
        brocastToSceneCurLine(player, SceneExtension.NPC_DIR, vo, me);
    }

    // 怪物方向
    public void changeState(int playerId, ChangeFSMState vo) {
        Player player = playerService.getPlayer(playerId);
        Channel me = SessionManager.getInstance().getChannel(playerId);
        brocastToSceneCurLine(player, SceneExtension.NPC_STATE, vo, me);
    }

    public void handlerUseSkill(int playerId, UseSkillVO skillVO) {
        if (!SysConfig.debug) {
            //非调试状态下需要检验伤害,CD
        }
        useSkills.put(String.format("%d_%d", skillVO.attackId, skillVO.skillId), skillVO.type);
        //ServerLogger.warn("handler skill" + String.format("%d_%d", skillVO.attackId, skillVO.skillId));
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.USE_SKILL, skillVO);
    }

    public void adBuffToActor(int playerId, AddBuffVO vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ADD_BUFF, vo);
    }

    public void delBuffFromActor(int playerId, DelBuffVO vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.DEL_BUFF, vo);
    }

    /**
     * 同步位置信息
     *
     * @param playerId
     * @param vo
     */
    public void actorPos(int playerId, ActorWaitState vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_POS, vo);
    }

    public void actorMove(int playerId, ActorMoveState vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_MOVE, vo);
    }

    public void actorStrick(int playerId, ActorStrickenState vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_STRICK, vo);
    }

    public void actorDead(int playerId, IntParam vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_DEAD, vo);
    }

    public void actorActorSkill(int playerId, ActorSkillVO vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_SKILL, vo);
    }

    public void actorActorSkillCard(int playerId, SkillCardEffectVO vo) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.ACTOR_CARD, vo);
    }

    /**
     * 停止使用技能
     *
     * @param playerId
     * @param skillVO
     */
    public void handlerStopSkill(int playerId, StopSkillVO skillVO) {
        Player player = playerService.getPlayer(playerId);
        brocastToSceneCurLine(player, SceneExtension.SKILL_STOP, skillVO, SessionManager.getInstance().getChannel(player.getPlayerId()));
    }

    // 玩家技能处理
    public void handleSkillHurt(int playerId, SkillHurtVO hurtVO) {
        Player player = playerService.getPlayer(playerId);
        Integer type = useSkills.get(String.format("%d_%d", hurtVO.attackId, hurtVO.skillId));
        if (type == null) {
            //ServerLogger.warn("handler skill hurt,there is no use skill record!" + String.format("%d_%d", hurtVO.attackId, hurtVO.skillId));
            //return;
        }
        if (!SysConfig.debug) {
            //非调试状态下需要检验伤害
        }

        SceneConfig cfg = GameData.getConfig(SceneConfig.class, player.getSceneId());
        if (cfg.sceneSubType == Scene.MULTI_GROUP) {
            groupService.handleSkillHurt(player, hurtVO);
        } else if (cfg.sceneSubType == Scene.WORLD_BOSS_PVE) {
            worldBossService.handleSkillHurt(player, hurtVO);
        } else if (cfg.sceneSubType == Scene.MULTI_LADDER) {
            ladderService.handleSkillHurt(player, hurtVO);
        } else if (cfg.sceneSubType == Scene.MULTI_GANG_BOSS) {
            //brocastToSceneCurLine(player, SceneExtension.SKILL_HURT, hurtVO);
            gangDungeonService.handleSkillHurt(player, hurtVO);
        }


        if (player.getTeamId() > 0) {
            teamService.handleSkillHurt(player, hurtVO);
        } else {
            //worldBossService.handleSkillHurt(player,hurtVO);
            brocastToSceneCurLine(player, SceneExtension.SKILL_HURT, hurtVO);
        }
    }
}
