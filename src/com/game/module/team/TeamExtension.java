package com.game.module.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.CopyConfig;
import com.game.data.Response;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.copy.SEnterCopy;
import com.game.params.team.MyTeamVO;
import com.game.params.team.TeamVO;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class TeamExtension {

	public final static int REASON_SELF = 0;
	public final static int REASON_KICK = 1;
	public final static int REASON_DISSOLVE = 2;
	
	@Autowired
	private TeamService teamService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private CopyExtension copyExtension;
	@Autowired
	private CopyService copyService;
	
	@Command(3402)
	public Object getTeamList(int playerId, IntParam param){
		ListParam<TeamVO> result = new ListParam<TeamVO>();
		int type = param.param;
		List<Team> teams = teamService.getAllTeam();
		result.params = new ArrayList<TeamVO>();
		for(Team team : teams){
			if(team.getType() != type || !team.isOpen() || team.isRunning()){
				continue;
			}
			TeamVO vo = new TeamVO();
			vo.id = team.getId();
			vo.name = team.getName();
			vo.copyId = team.getCopyId();
			vo.member = team.getMembers().size();
			Player leader = playerService.getPlayer(team.getLeader());
			vo.leaderLev = leader.getLev();
			vo.leaderVocation = leader.getVocation();
			result.params.add(vo);
		}
		return result;
	}
	
	public static final int MY_TEAM_INFO = 3403;
	@Command(3403)
	public Object getMyTeam(int playerId, Object param){
		Player player = playerService.getPlayer(playerId);
		int teamId = player.getTeamId();
		if(teamId == 0){
			return new MyTeamVO();
		}
		Team team = teamService.getTeam(teamId);
		return teamService.wrapTeam(team);
	}
	
	@Command(3404)
	public Object setOpen(int playerId, IntParam param){
		Player player = playerService.getPlayer(playerId);
		Team team = teamService.getTeam(player.getTeamId());
		team.setOpen(param.param > 0);
		return param;
	}
	
	public static final int UPDATE_LEADER = 3410;
	@Command(3405)
	public Object setLeader(int playerId, IntParam param){
		Player player = playerService.getPlayer(playerId);
		Team team = teamService.getTeam(player.getTeamId());
		int oldLeader = team.getLeader();
		if(oldLeader != playerId){
			param.param = Response.ERR_PARAM;
			return param;
		}
		if(oldLeader != param.param){
			team.setLeader(param.param);
			IntParam result = new IntParam();
			result.param = param.param;
			sceneService.brocastToSceneCurLine(player, UPDATE_LEADER, result, null);
		}
		param.param = Response.SUCCESS;
		return param;
	}
	
	@Command(3406)
	public Object dissolve(int playerId, Object param){
		Player player = playerService.getPlayer(playerId);
		IntParam result = new IntParam();
		Team team = teamService.getTeam(player.getTeamId());
		if(team == null){
			result.param = Response.NO_TEAM;
			return result;
		}
		if(team.getLeader() != playerId){
			result.param = Response.ERR_PARAM;
			return result;
		}
		Int2Param msg = new Int2Param();
		msg.param2 = REASON_DISSOLVE;
		sceneService.brocastToSceneCurLine(player, LEAVE, msg, SessionManager.getInstance().getChannel(playerId));
		teamService.dissolve(team);
		return result;
		
	}
	
	@Command(3407)
	public Object kick(int playerId, IntParam param){
		Player player = playerService.getPlayer(playerId);
		Team team = teamService.getTeam(player.getTeamId());
		IntParam result = new IntParam();
		if(team.getLeader() != playerId){
			result.param = Response.ERR_PARAM;
			return result;
		}
		if(team.isRunning()){
			result.param = Response.TEAM_RUNNING_NO_LEAVE;
			return result;
		}
		Int2Param msg = new Int2Param();
		msg.param2 = REASON_KICK;
		Player other = playerService.getPlayer(param.param);
		sceneService.exitScene(other);
		SessionManager.getInstance().sendMsg(LEAVE, msg, other.getPlayerId());
		teamService.kick(team, param.param);
		return result;
	}
	
	
	
	public static final int LEAVE = 3408;
	@Command(3408)
	public Object leave(int playerId, Object param){
		Player player = playerService.getPlayer(playerId);
		Team team = teamService.getTeam(player.getTeamId());
		Int2Param result = new Int2Param();
		if(team == null){
			result.param1 = Response.ERR_PARAM;
			return result; 
		}
		if(team.getLeader() == playerId){
			return dissolve(playerId, param);
		}
		sceneService.exitScene(player);
		teamService.kick(team, playerId);
		return result;
	}
	
	@Command(3409)
	public Object join(int playerId, IntParam param){
		Int2Param result = new Int2Param();
		result.param1 = teamService.joinTeam(playerId, param.param);
		result.param2 = param.param;
		return result;
	}
	
	public static final int UPDATE_READY = 3412;
	@Command(3411)
	public Object setReady(int playerId, IntParam param){
		Player player = playerService.getPlayer(playerId);
		Team team = teamService.getTeam(player.getTeamId());
		Int2Param result = new Int2Param();
		if(team.isRunning()){
			result.param1 = Response.ERR_PARAM;
			return result;
		}
		TMember member =  team.getMembers().get(playerId);
		member.setReady(param.param > 0);
		Int2Param msg = new Int2Param();
		msg.param1 = playerId;
		msg.param2 = param.param;
		sceneService.brocastToSceneCurLine(player, UPDATE_READY, msg, SessionManager.getInstance().getChannel(playerId));
		result.param2 = param.param;
		return result;
	}
	
	@Command(3413)
	public Object enterCopy(int playerId, Object param){
		Player player = playerService.getPlayer(playerId);
		
		Team team = teamService.getTeam(player.getTeamId());
		for(TMember member : team.getMembers().values()){
			if(member.getPlayerId() != playerId && !member.isReady()){
				SEnterCopy result = new SEnterCopy();
				result.code = Response.TEAM_NO_READY;
				return result;
			}
		}
		CopyConfig copyConfig = ConfigData.getConfig(CopyConfig.class, team.getCopyId());
		IntParam intParam = new IntParam();
		intParam.param = team.getCopyId();
		SEnterCopy result = (SEnterCopy)copyExtension.enter(playerId, intParam);
		if(result.code > 0){
			SessionManager.getInstance().sendMsg(CopyExtension.ENTER_COPY, result, playerId);
		}else{	
			team.setRunning(true);
			int copyInstanceId = player.getCopyId();
			CopyInstance copyIns = copyService.getCopyInstance(copyInstanceId);
			if(copyConfig.type == CopyInstance.TYPE_TRAVERSING){				
				copyIns.setTraverseMap(playerService.getPlayerData(playerId).getTraverseMaps().get(team.getMapId()));
			}
			for(Map.Entry<Integer, TMember> entry : team.getMembers().entrySet()){
				int id = entry.getKey();
				TMember member = entry.getValue();
				Player otherPlayer = playerService.getPlayer(id);
				member.setCurHp(otherPlayer.getHp());
				otherPlayer.setCopyId(copyInstanceId);
				if(id != playerId){
					copyIns.getMembers().getAndIncrement();
				}
				SessionManager.getInstance().sendMsg(CopyExtension.ENTER_COPY, result, id);
			}
		}
		return null;
	}
	
}
