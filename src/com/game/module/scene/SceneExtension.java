package com.game.module.scene;

import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.scene.*;

import io.netty.channel.Channel;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.copy.CopyService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.Long2Param;
import com.game.params.LongParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.anotation.UnLogin;
import com.server.util.ServerLogger;

@Extension
public class SceneExtension {

	@Autowired
	private SceneService sceneService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private CopyService copyService;

	@Command(1101)
	public Object changeScene(int playerId, CEnterScene param) {
		int sceneId = param.sceneId;
		Player player = playerService.getPlayer(playerId);
		sceneService.exitScene(player);
		sceneService.enterScene(player, sceneId, param.x,param.z);

		return sceneService.getSceneInfo(player, sceneId);
	}

	// 离开场景
	public static final int EXIT_SCENE = 1102;
	// 进入场景
	public static final int ENTER_SCENE = 1103;
	// 玩家移动
	public static final int WALK_SCENE = 1107;
	public static final int NPC_MOVE = 1113;
	public static final int NPC_DIR = 4912;
	public static final int NPC_STATE = 4913;
	// 其他玩家移动
	public static final int STOP_WALK_SCENE = 1108;

	

	@UnLogin
	@Command(1106)
	public Object heart(int playerId, LongParam clientTime, Channel channel) {
		if (playerId > 0) {
			playerService.saveData(playerId);
		}
		Long2Param result = new Long2Param();
		result.param1 = System.currentTimeMillis();
		result.param2 = clientTime.param;
		return result;
	}
	
	@Command(1107)
	public Object walk(int playerId, MoveStart param) {
		sceneService.walk(playerId, param);
		return null;
	}
	
	@Command(1108)
	public Object stop(int playerId, MoveStop param) {
		sceneService.stop(playerId, param);
		return null;
	}

	// 中途退出场景
	@Command(1109)
	public Object exit(int playerId, Object param) {
		Player player = playerService.getPlayer(playerId);
		CEnterScene scene = new CEnterScene();
		scene.sceneId = player.getLastSceneId();
		ServerLogger.debug("exit and return to scene:" + scene.sceneId);
		scene.x = player.getLastPos()[0];
		scene.z = player.getLastPos()[2];

		return scene;
	}
		
	public static final int USE_SKILL = 1110;
	@Command(1110)
	public Object useSkill(int playerId, UseSkillVO param){
		sceneService.handlerUseSkill(playerId, param);
		return null;
	}

	public static final int SKILL_STOP = 1111;
	@Command(1111)
	public Object skillStop(int playerId, StopSkillVO param){
		sceneService.handlerStopSkill(playerId, param);
		return null;
	}

	public static final int SKILL_HURT = 1112;
	@Command(1112)
	public Object skillHurt(int playerId, SkillHurtVO param){
		sceneService.handleSkillHurt(playerId, param);
		return null;
	}

	@Command(1113)
	public Object npcMove(int playerId,NpcMoveStart vo) {
		sceneService.npcMove(playerId,vo);
		return null;
	}

	public static final int ADD_BUFF = 1114;
	@Command(1114)
	public Object addBuffToActor(int playerId,AddBuffVO vo) {
		sceneService.adBuffToActor(playerId,vo);
		return null;
	}

	public static final int DEL_BUFF = 1115;
	@Command(1115)
	public Object delBuffFromActor(int playerId,DelBuffVO vo) {
		sceneService.delBuffFromActor(playerId,vo);
		return null;
	}

	public static final int ACTOR_POS = 1105;
	@Command(1105)
	public Object actorPos(int playerId,ActorWaitState vo) {
		sceneService.actorPos(playerId,vo);
		return null;
	}

	public static final int ACTOR_MOVE = 1116;
	@Command(1116)
	public Object actorMove(int playerId,ActorMoveState vo) {
		sceneService.actorMove(playerId,vo);
		return null;
	}

	public static final int ACTOR_STRICK = 1117;
	@Command(1117)
	public Object actorStricken(int playerId,ActorStrickenState vo) {
		sceneService.actorStrick(playerId,vo);
		return null;
	}

	public static final int ACTOR_DEAD = 1118;
	@Command(1118)
	public Object actorDead(int playerId,IntParam vo) {
		sceneService.actorDead(playerId,vo);
		return null;
	}

	public static final int ACTOR_SKILL = 1119;
	@Command(1119)
	public Object actorActorSkill(int playerId,ActorSkillVO vo) {
		sceneService.actorActorSkill(playerId,vo);
		return null;
	}

	public static final int ACTOR_CARD = 1120;
	@Command(1120)
	public Object actorActorSkillCard(int playerId,SkillCardEffectVO vo) {
		sceneService.actorActorSkillCard(playerId,vo);
		return null;
	}

	@Command(4912)
	public Object npcDir(int playerId,Int2Param vo) {
		sceneService.monsterDir(playerId,vo);
		return null;
	}

	@Command(4913)
	public Object changeState(int playerId,ChangeFSMState vo) {
		sceneService.changeState(playerId,vo);
		return null;
	}
}
