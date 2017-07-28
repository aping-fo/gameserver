package com.game.module.scene;

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
	
}
