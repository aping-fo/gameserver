package com.game.module.scene;

import com.game.params.scene.*;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.data.SceneConfig;
import com.game.event.InitHandler;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.gang.GangService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.team.TeamService;
import com.game.module.worldboss.WorldBossService;
import com.game.params.IProtocol;
import com.game.params.Int2Param;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;

@Service
public class SceneService implements InitHandler {

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
	
	private Map<Integer, Scene> scenes = new ConcurrentHashMap<Integer, Scene>();
	
	private Map<String, Integer> useSkills = new ConcurrentHashMap<String, Integer>();

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
			return String.format("%d_%d_%d", sceneId, player.getGangId(),
					player.getSubLine());
		} else if (cfg.sceneSubType == Scene.MULTI_TEAM || cfg.sceneSubType == Scene.MULTI_PVE) {
			return String.format("%d_%d_%d", sceneId, player.getTeamId(),
					player.getSubLine());
		} else {
			return String.format("%d_%d", sceneId, subLine);
		}
	}

	// 推送场景广播
	public void brocastToSceneCurLine(Player player, int cmd, IProtocol param) {
		brocastToSceneCurLine(player, cmd, param,SessionManager.getInstance().getChannel(player.getPlayerId()));
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
		if(lastCfg.sceneSubType == Scene.WORLD_BOSSS_PVE) {
			worldBossService.removePlayer(player.getPlayerId());
		}

		SessionManager.getInstance().removeFromGroup(key, player.getPlayerId());

		Int2Param param = new Int2Param();
		param.param1 = sceneId;
		param.param2 = playerId;

		Channel channel = SessionManager.getInstance().getChannel(
				player.getPlayerId());
		brocastToSceneCurLine(player, SceneExtension.EXIT_SCENE, param, channel);

		lastScene.exitSubLine(player.getSubLine());
		player.setSubLine(0);


		ServerLogger.info("...exit", playerId, player.getSceneId());
	}

	// 进入场景
	public void enterScene(Player player, int sceneId, float x, float z) {
		Scene scene = getScene(sceneId);
		int curScene = player.getSceneId();
		if (curScene == scene.getId() && player.getSubLine() > 0) {// 同一个场景
			return;
		}

		SceneConfig lastCfg = GameData.getConfig(SceneConfig.class, curScene);
		if (lastCfg.type == Scene.CITY || lastCfg.sceneSubType == Scene.MULTI_CITY || lastCfg.sceneSubType == Scene.MULTI_GANG) {
			player.setLastSceneId(curScene);
			player.setLastPos(new float[] { player.getX(), player.getY(),
					player.getZ() });

		}
		SceneConfig cfg = GameData.getConfig(SceneConfig.class, sceneId);
		if (cfg.type == Scene.CITY || cfg.sceneSubType == Scene.MULTI_CITY || cfg.sceneSubType == Scene.MULTI_GANG) {
			// 清除副本实例
			teamService.quit(player.getPlayerId());
			copyService.removeCopy(player.getPlayerId());
		}

		player.setSceneId(sceneId);
		player.setX(x);
		player.setZ(z);
		player.sethMoveDir(0);
		player.setvMoveDir(0);

		int subLine = scene.getNewSubLine();
		scene.enterSubLine(subLine);
		player.setSubLine(subLine);

		String key = getGroupKey(player);
		Channel channel = SessionManager.getInstance().getChannel(
				player.getPlayerId());
		SessionManager.getInstance().addToGroup(key, channel);

		if(cfg.sceneSubType == Scene.WORLD_BOSSS_PVE) {
			worldBossService.addPlayer(player.getPlayerId());
		}
		// 广播消息
		brocastToSceneCurLine(player, SceneExtension.ENTER_SCENE, toVo(player), channel);
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
		vo.head = playerService.getPlayerData(player.getPlayerId())
				.getCurHead();
		if(player.getGangId() > 0){
			vo.gang = gangService.getGang(player.getGangId()).getName();
		}
		return vo;
	}

	// 获取场景玩家
	public SSceneInfo getSceneInfo(Player player, int sceneId) {
		SceneConfig cfg = GameData.getConfig(SceneConfig.class, sceneId);
		SSceneInfo sceneInfo = genMonster(cfg, player);// 生成怪物信息
		sceneInfo.players = new ArrayList<SScenePlayerVo>();
		sceneInfo.sceneId = sceneId;
		if (cfg.type == Scene.COPY) {
			if (sceneInfo.monsters == null || sceneInfo.monsters.isEmpty()) {
				ServerLogger.warn("err enter copy:" + sceneId + " playerId:"
						+ player.getPlayerId());
			}
		}

		if (cfg.type != Scene.MULTI) {// 副本的只返回自己的
			sceneInfo.players.add(toVo(player));
			return sceneInfo;
		}

		String key = getGroupKey(player);

		Collection<Channel> channels = SessionManager.getInstance()
				.getGroupChannels(key);

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

		return sceneInfo;
	}

	// 生成怪物
	public SSceneInfo genMonster(SceneConfig cfg, Player player) {
		SSceneInfo sceneInfo = new SSceneInfo();
		List<SMonsterVo> monsters = new ArrayList<SMonsterVo>();
		if (cfg.type == Scene.COPY
				|| (cfg.type == Scene.MULTI &&
				(cfg.sceneSubType == Scene.MULTI_PVE || cfg.sceneSubType == Scene.WORLD_BOSSS_PVE))) {// 普通副本，多人PVE
			int copyInstance = player.getCopyId();
			CopyInstance copy = copyService.getCopyInstance(copyInstance);
			monsters.addAll(copy.getMonsters().get(cfg.id).values());

			if (copy.getTraverseMap() != null) {
				int[] affixs = copy.getTraverseMap().getAffixs();
				if (affixs != null) {
					sceneInfo.affixs = new ArrayList<Integer>();
					for (int id : affixs) {
						sceneInfo.affixs.add(id);
					}
				}
			}

		}
		sceneInfo.monsters = monsters;
		return sceneInfo;
	}

	// 场景移动
	public void walk(int playerId, MoveStart vo) {
		Player player = playerService.getPlayer(playerId);
		player.setvMoveDir(vo.vMoveDir);
		player.sethMoveDir(vo.hMoveDir);
		player.setX(vo.x);
		player.setZ(vo.z);
		Channel me = SessionManager.getInstance().getChannel(playerId);
		brocastToSceneCurLine(player, SceneExtension.WALK_SCENE, vo, me);
	}

	// 场景停止移动
	public void stop(int playerId, MoveStop vo) {
		Player player = playerService.getPlayer(playerId);
		if(vo.type == 0) {
			player.setvMoveDir(0);
			player.sethMoveDir(0);
			player.setX(vo.x);
			player.setZ(vo.z);
		}
		Channel me = SessionManager.getInstance().getChannel(playerId);
		brocastToSceneCurLine(player, SceneExtension.STOP_WALK_SCENE, vo, me);
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

	public void handlerUseSkill(int playerId, UseSkillVO skillVO){
		if(!SysConfig.debug){
			//非调试状态下需要检验伤害,CD
		}
		useSkills.put(String.format("%d_%d", skillVO.attackId, skillVO.skillId), skillVO.type);
		ServerLogger.warn("handler skill" + String.format("%d_%d", skillVO.attackId, skillVO.skillId));
		Player player = playerService.getPlayer(playerId);
		brocastToSceneCurLine(player, SceneExtension.USE_SKILL, skillVO);
	}

	/**
	 * 停止使用技能
	 * @param playerId
	 * @param skillVO
	 */
	public void handlerStopSkill(int playerId, StopSkillVO skillVO){
		Player player = playerService.getPlayer(playerId);
		if(skillVO.type == 0)
			skillVO.attackId = playerId;
		brocastToSceneCurLine(player, SceneExtension.SKILL_STOP, skillVO,SessionManager.getInstance().getChannel(player.getPlayerId()));
	}

	// 玩家技能处理
	public void handleSkillHurt(int playerId, SkillHurtVO hurtVO) {
		Player player = playerService.getPlayer(playerId);
		Integer type = useSkills.get(String.format("%d_%d", hurtVO.attackId, hurtVO.skillId));
		if (type == null) {
			ServerLogger.warn("handler skill hurt,there is no use skill record!" + String.format("%d_%d", hurtVO.attackId, hurtVO.skillId));
			//return;
		}
		if (!SysConfig.debug) {
			//非调试状态下需要检验伤害
		}

		//SceneConfig cfg = GameData.getConfig(SceneConfig.class, player.getSceneId());
		//brocastToSceneCurLine(player, SceneExtension.SKILL_HURT, hurtVO);
		if (player.getTeamId() > 0) {
			teamService.handleSkillHurt(player, hurtVO);
		}else {
			worldBossService.handleSkillHurt(player,hurtVO);
		}
	}
}
