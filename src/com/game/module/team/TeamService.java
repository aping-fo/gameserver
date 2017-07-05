package com.game.module.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.CopyConfig;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.copy.TraverseMap;
import com.game.module.goods.Goods;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.params.Int2Param;
import com.game.params.copy.CopyResult;
import com.game.params.scene.HurtVO;
import com.game.params.scene.SMonsterVo;
import com.game.params.scene.SkillHurtVO;
import com.game.params.team.MyTeamVO;
import com.game.params.team.TeamMemberVO;
import com.server.SessionManager;
import com.server.util.GameData;

//TODO 暂时有线程安全问题
@Service
public class TeamService implements InitHandler {

	public static int MAX_MEMBER = 3;

	@Autowired
	private PlayerService playerService;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private CopyService copyService;

	private volatile int maxTeamId = 1000;
	private Map<Integer, Team> teams = new ConcurrentHashMap<Integer, Team>();

	@Override
	public void handleInit() {

	}

	public Team getTeam(int teamId) {
		return teams.get(teamId);
	}

	public List<Team> getAllTeam() {
		return new ArrayList<Team>(teams.values());
	}

	public void dissolve(Team team) {
		for (TMember member : team.getMembers().values()) {
			kick(team, member.getPlayerId());
		}
		teams.remove(team.getId());
	}

	public void quit(int playerId) {
		Player player = playerService.getPlayer(playerId);
		if (player.getTeamId() > 0) {
			Team team = teams.get(player.getTeamId());
			if (team.getLeader() == playerId) {
				dissolve(team);
				Int2Param msg = new Int2Param();
				msg.param2 = TeamExtension.REASON_DISSOLVE;
				sceneService.brocastToSceneCurLine(player, TeamExtension.LEAVE,
						msg, SessionManager.getInstance().getChannel(playerId));
			} else {
				kick(team, playerId);
			}
		}
	}

	public void kick(Team team, int playerId) {
		Player player = playerService.getPlayer(playerId);
		player.setTeamId(0);
		team.getMembers().remove(playerId);
	}

	public Team createTeam(int playerId, int type, String name, int copyId) {
		int teamId = maxTeamId++;
		Team team = new Team(teamId, type, name, playerId);
		team.setCopyId(copyId);
		;
		team.addMember(new TMember(playerId));
		teams.put(teamId, team);
		Player player = playerService.getPlayer(playerId);
		player.setTeamId(teamId);
		return team;
	}

	public int joinTeam(int playerId, int teamId) {
		Team team = getTeam(teamId);
		if (team == null) {
			return Response.NO_TEAM;
		}
		if (team.isRunning()) {
			return Response.TEAM_RUNNING_NO_JOIN;
		}
		if (team.getMembers().size() >= 3) {
			return Response.TEAM_FULL;
		}
		int type = team.getType();
		if (type == Team.TYPE_TRAVERSING) {
			PlayerData playerData = playerService.getPlayerData(team
					.getLeader());
			TraverseMap map = playerData.getTraverseMaps().get(team.getMapId());
			GoodsConfig goodsCfg = GameData.getConfig(GoodsConfig.class,
					map.getGoodsCfgId());
			if (!playerService.verifyCurrency(playerId,
					Goods.TRAVERSING_ENERGY, goodsCfg.param1[1])) {
				return Response.NO_TRAVERSING_ENERGY;
			}
		} else {
			Player player = playerService.getPlayer(playerId);
			CopyConfig copyCfg = GameData.getConfig(CopyConfig.class,
					team.getCopyId());
			// 检查等级
			if (player.getLev() < copyCfg.lev) {
				return Response.NO_LEV;
			}
			// 次数
			if (copyCfg.count > 0) {
				Integer curCount = playerService.getPlayerData(playerId)
						.getCopyTimes().get(copyCfg.id);
				if (curCount == null) {
					curCount = 0;
				}
				if (curCount >= copyCfg.count) {
					return Response.NO_TODAY_TIMES;
				}
			}
			if (copyCfg.needEnergy > 0) {
				if (player.getEnergy() < copyCfg.needEnergy) {
					return Response.NO_ENERGY;
				}
			}
		}
		TMember member = new TMember(playerId);
		team.addMember(member);
		Player player = playerService.getPlayer(playerId);
		player.setTeamId(teamId);
		return Response.SUCCESS;
	}

	public MyTeamVO wrapTeam(Team team) {
		MyTeamVO vo = new MyTeamVO();
		vo.leader = team.getLeader();
		vo.member = new ArrayList<TeamMemberVO>();
		for (TMember member : team.getMembers().values()) {
			if (member.getPlayerId() == vo.leader)
				continue;
			vo.member.add(wrapMember(member));
		}
		return vo;
	}

	public TeamMemberVO wrapMember(TMember member) {
		TeamMemberVO vo = new TeamMemberVO();
		vo.ready = member.isReady();
		vo.memberId = member.getPlayerId();
		return vo;
	}

	// 玩家技能处理
	public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
		Team team = getTeam(player.getTeamId());
		int sceneId = player.getSceneId();
		CopyInstance copy = copyService.getCopyInstance(player.getCopyId());
		Map<Integer, SMonsterVo> monsters = copy.getMonsters().get(sceneId);
		for(HurtVO hurt : hurtVO.hurts){
			if(hurt.type == 0){
				TMember member = team.getMembers().get(hurt.targetId);
				member.decHp(hurt.hurtValue);
				if(member.getCurHp() <= 0){
					boolean isLost = true;
					for(TMember tm : team.getMembers().values()){
						if(tm.getCurHp() > 0){
							isLost = false;
							break;
						}
					}
					if(isLost){
						//副本失败
						sceneService.brocastToSceneCurLine(player, CopyExtension.COPY_FAIL, null);
					}
					break;
				}
			}else{
				SMonsterVo monster = monsters.get(hurt.targetId);
				monster.curHp -= hurt.hurtValue;
				if (monster.curHp <= 0) {
					if(copy.isOver()){
						//副本胜利
						
						CopyResult result = new CopyResult();
						copyService.getRewards(player.getPlayerId(), copy.getCopyId(), result);
						// 更新次数,星级
						copyService.updateCopy(player.getPlayerId(), copy, result);							
						sceneService.brocastToSceneCurLine(player, CopyExtension.TAKE_COPY_REWARDS, result);
						for(TMember tm : team.getMembers().values()){
							// 清除
							copyService.removeCopy(tm.getPlayerId());							
						}
						dissolve(team);
					}
				}
			}
		}
	}

}
