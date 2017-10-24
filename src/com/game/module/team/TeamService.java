package com.game.module.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.module.group.Group;
import com.game.module.group.GroupTeam;
import com.game.params.worldboss.MonsterHurtVO;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.CopyConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.Goods;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.worldboss.WorldBossService;
import com.game.params.Int2Param;
import com.game.params.copy.CopyResult;
import com.game.params.scene.SMonsterVo;
import com.game.params.scene.SkillHurtVO;
import com.game.params.team.MyTeamVO;
import com.game.params.team.TeamMemberVO;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.util.GameData;

@Service
public class TeamService implements InitHandler {

	public static int MAX_MEMBER = 3;

	@Autowired
	private PlayerService playerService;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private CopyService copyService;
	@Autowired
	private WorldBossService worldBossService;

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
				Int2Param msg = new Int2Param();
				msg.param2 = TeamExtension.REASON_DISSOLVE;
				sceneService.brocastToSceneCurLine(player, TeamExtension.LEAVE,
						msg, SessionManager.getInstance().getChannel(playerId));
				dissolve(team);
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
		Player player = playerService.getPlayer(playerId);
		if(player.getTeamId() > 0){
			return Response.IN_TEAMING;
		}
		Team team = getTeam(teamId);
		if (team == null) {
			return Response.NO_TEAM;
		}
		if (team.isRunning()) {
			return Response.TEAM_RUNNING_NO_JOIN;
		}
		if (team.getMembers().size() >= ConfigData.globalParam().teamLimit.getOrDefault(team.getType(), 3)) {
			return Response.TEAM_FULL;
		}
		int type = team.getType();
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
			if (type == Team.TYPE_TRAVERSING) {
				if(!playerService.verifyCurrency(playerId,
				Goods.TRAVERSING_ENERGY, copyCfg.needEnergy)){
					return Response.NO_TRAVERSING_ENERGY;
				}
				
			}else if (player.getEnergy() < copyCfg.needEnergy) {
				return Response.NO_ENERGY;
			}
		}
		TMember member = new TMember(playerId);
		team.addMember(member);
		player.setTeamId(teamId);
		return Response.SUCCESS;
	}

	public MyTeamVO wrapTeam(Team team) {
		MyTeamVO vo = new MyTeamVO();
		vo.leader = team.getLeader();
		vo.member = new ArrayList<>();
		for (TMember member : team.getMembers().values()) {
			vo.member.add(wrapMember(member));
		}
		return vo;
	}

	public TeamMemberVO wrapMember(TMember member) {
		TeamMemberVO vo = new TeamMemberVO();
		vo.ready = member.isReady();
		vo.memberId = member.getPlayerId();
		Player player = playerService.getPlayer(member.getPlayerId());
		vo.fight = player.getFight();
		vo.lev = player.getLev();
		return vo;
	}

	private static final int CMD_MONSTER_INFO = 4910; //同步怪物相关信息
	// 玩家技能处理
	public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
		Team team = getTeam(player.getTeamId());
		int sceneId = player.getSceneId();
		CopyInstance copy = copyService.getCopyInstance(player.getCopyId());
		Map<Integer, SMonsterVo> monsters = copy.getMonsters().get(sceneId);
		if(hurtVO.targetType == 0){
			TMember member = team.getMembers().get(hurtVO.targetId);
			member.decHp(hurtVO.hurtValue);
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
			}
		}else{
			SMonsterVo monster = monsters.get(hurtVO.targetId);
			monster.curHp -= hurtVO.hurtValue;
			MonsterHurtVO ret = new MonsterHurtVO();
			ret.monsterId = hurtVO.targetId;
			ret.curHp = monster.curHp;
			ret.hurt = hurtVO.hurtValue;
			ret.type = 1;
			sceneService.brocastToSceneCurLine(player, CMD_MONSTER_INFO, ret, null);
			if (monster.curHp <= 0) {
				monsters.remove(hurtVO.targetId);
				if(copy.isOver()){
					//副本胜利

					CopyResult result = new CopyResult();
					copyService.getRewards(player.getPlayerId(), copy.getCopyId(), result);
					// 更新次数,星级
					copyService.updateCopy(player.getPlayerId(), copy, result);
					sceneService.brocastToSceneCurLine(player, CopyExtension.TAKE_COPY_REWARDS, result, null);
					for(TMember tm : team.getMembers().values()){
						// 清除
						copyService.removeCopy(tm.getPlayerId());
					}
					dissolve(team);
				}
			}
		}
	}

	/**
	 * 进入战斗
	 *
	 * @param playerId
	 * @return
	 */
	public int onEnterBattle(int playerId) {
		Player player = playerService.getPlayer(playerId);
		Team team = teams.get(player.getTeamId());
		return team.getLeader() * 100;
	}

	public void updateAttr(int playerId) {
		Player player = playerService.getPlayer(playerId);
		Team team = getTeam(player.getTeamId());
		if (team == null) {
			return ;
		}
		MyTeamVO vo = wrapTeam(team);
		sceneService.brocastToSceneCurLine(player, TeamExtension.MY_TEAM_INFO, vo,null);
	}
}
