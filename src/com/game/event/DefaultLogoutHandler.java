package com.game.event;
import java.util.Date;

import com.game.module.group.GroupService;
import com.game.module.ladder.LadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.SceneConfig;
import com.game.module.attach.arena.ArenaLogic;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsService;
import com.game.module.log.LoggerService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.Scene;
import com.game.module.scene.SceneService;
import com.game.module.task.TaskService;
import com.game.module.team.TeamService;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.server.LogoutHandler;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import com.server.validate.AntiCheatService;

@Service
public class DefaultLogoutHandler implements LogoutHandler{
	@Autowired
	private PlayerService playerService;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private CopyService copyService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private LoggerService loggerService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private ArenaLogic arenaLogic;
	@Autowired
	private TeamService teamService;
	@Autowired
	private LadderService ladderService;
	@Autowired
	private GroupService groupService;

	public void handleLogout(final int playerId) {
		Context.getThreadService().execute(new Runnable() {
			@Override
			public void run() {
				logout(playerId);
			}
		});
	}

	/**
	 * 下线处理 要处理下线，就实现LogoutListener 接口， 不要在这边直接调用模块代码
	 */
	public void logout(int playerId) {
		try {

			ServerLogger.info(".....user logout:" + playerId);

			Player player = playerService.getPlayer(playerId);
			player.setLastLogoutTime(new Date());
			
			//清除副本实例
			copyService.removeCopy(playerId);
			
			// 退出场景
			sceneService.exitScene(player);
			teamService.quit(playerId);
			// 退出副本场景
			SceneConfig scene = ConfigData.getConfig(SceneConfig.class, player.getSceneId());
			if(player.getLastSceneId()>0&&scene.type !=Scene.CITY){
				player.setSceneId(player.getLastSceneId());
				float[]pos = player.getLastPos();
				player.setX(pos[0]);
				player.setY(pos[1]);
				player.setZ(pos[2]);
			}
			
			
			// 保证以下的代码在最后
			// 清除session
			playerService.update(player);
			playerService.updatePlayerData(playerId);
			taskService.updateTask(playerId);
			goodsService.updateBag(playerId);
			arenaLogic.quit(playerId);

			ladderService.onLogout(playerId);
			groupService.onLogout(playerId);

			SessionManager.getInstance().removeSession(playerId);
			AntiCheatService.getInstance().clear(playerId);

			DisposeHandler.dispose(playerId);// 清除缓存
			
			AntiCheatService.getInstance().clear(playerId);
			
		} catch (Exception e) {
			ServerLogger.err(e, "handle logout err!");
		}
	}

}
