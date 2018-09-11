package com.game.event;

import com.game.data.SceneConfig;
import com.game.module.attach.arena.ArenaLogic;
import com.game.module.copy.CopyService;
import com.game.module.friend.FriendService;
import com.game.module.gang.GangDungeonService;
import com.game.module.goods.GoodsService;
import com.game.module.group.GroupService;
import com.game.module.ladder.LadderService;
import com.game.module.pet.PetService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.Scene;
import com.game.module.scene.SceneService;
import com.game.module.task.TaskService;
import com.game.module.team.TeamService;
import com.game.sdk.erating.ERatingService;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.server.LogoutHandler;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import com.server.validate.AntiCheatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultLogoutHandler implements LogoutHandler {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private TaskService taskService;
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
    @Autowired
    private GangDungeonService gangDungeonService;
    @Autowired
    private PetService petService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private ERatingService ratingService;

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
            if (player == null) {
                return;
            }
            int teamId = player.getTeamId();
            PlayerData playerData = playerService.getPlayerData(playerId);
            if (player.onlineTime != 0) {
                int passTime = (int) ((System.currentTimeMillis() - player.onlineTime) / 1000);
                playerData.setOnlineTime(playerData.getOnlineTime() + passTime);
            }

            player.setLastLogoutTime(new Date());
            playerService.removeChannel(player.getAccName(), SessionManager.getInstance().getChannel(playerId));

            //清除副本实例
            copyService.removeCopy(playerId);
            // 退出场景
            sceneService.exitScene(player);
            teamService.quit(playerId, teamId);
            // 退出副本场景
            SceneConfig scene = ConfigData.getConfig(SceneConfig.class, player.getSceneId());
            if (player.getLastSceneId() > 0 && scene.type != Scene.CITY) {
                player.setSceneId(player.getLastSceneId());
                float[] pos = player.getLastPos();
                player.setX(pos[0]);
                player.setY(pos[1]);
                player.setZ(pos[2]);
            }

            /*
            先执行一次保存，防止切换角色是获取的数据是旧的
            （执行下面updateTask更新任务数据时，写数据库有可能造成线程阻塞，令1001协议线程先执行）
             */
            playerService.update(player);
            ServerLogger.info("更新角色信息完毕" + player.getPlayerId());
            taskService.updateTask(playerId);
            goodsService.updateBag(playerId);
            petService.onLogout(playerId);
            arenaLogic.quit(playerId);

            ladderService.onLogout(playerId);
            groupService.onLogout(playerId);
            gangDungeonService.onLogout(playerId);
            friendService.onLogout(playerId);

            // 保证以下的代码在最后
            // 清除session
            playerService.update(player);
            ServerLogger.info("更新角色信息" + player.getPlayerId());
            playerService.updatePlayerData(playerId);
            //report logout log
            ratingService.reportRoleLogout(player, playerData.getRoleId());
            SessionManager.getInstance().removeSession(playerId);
            AntiCheatService.getInstance().clear(playerId);
            DisposeHandler.dispose(playerId);// 清除缓存
            AntiCheatService.getInstance().clear(playerId);
            playerService.removeCache(playerId);
            player.online = false;
        } catch (Exception e) {
            ServerLogger.err(e, "handle logout err!");
        }
    }

}
