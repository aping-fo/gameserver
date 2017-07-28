package com.game.module.traversing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.CopyConfig;
import com.game.data.Response;
import com.game.module.copy.CopyService;
import com.game.module.copy.TraverseMap;
import com.game.module.goods.Goods;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.team.Team;
import com.game.module.team.TeamExtension;
import com.game.module.team.TeamService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.traversing.TraverseMapVO;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class TraversingExtension {

	@Autowired
	private PlayerService playerService;
	@Autowired
	CopyService copyService;
	@Autowired
	TeamService teamService;
	@Autowired
	TeamExtension teamExtension;
	
	@Command(4001)
	public Object getInfo(int playerId, Object param){
		ListParam<TraverseMapVO> result = new ListParam<TraverseMapVO>();
		List<TraverseMapVO> list = new ArrayList<TraverseMapVO>();
		PlayerData data = playerService.getPlayerData(playerId);
		for(TraverseMap map : data.getTraverseMaps().values()){
			TraverseMapVO mapVO = new TraverseMapVO();
			mapVO.copyCfgId = map.getCopyId();
			mapVO.mapId = map.getMapId();
			mapVO.goodsCfgId = map.getGoodsCfgId();
			if(map.getAffixs() != null){
				mapVO.affixs = new ArrayList<Integer>();
				for(int id : map.getAffixs()){
					mapVO.affixs.add(id);
				}
			}
			list.add(mapVO);
		}
		
		result.params = list;
		return result;
	}
	
	@Command(4002)
	public Object singleChellenge(int playerId, IntParam param){
		/**
		Player player = playerService.getPlayer(playerId);
		PlayerData playerData = playerService.getPlayerData(playerId);
		TraverseMap map = playerData.getTraverseMaps().get(param.param);
		SEnterCopy result = new SEnterCopy();
		if(map == null){
			result.code = Response.ERR_PARAM;
			return result;
		}
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, map.getCopyId());
		if(!playerService.verifyCurrency(playerId, Goods.TRAVERSING_ENERGY, cfg.needEnergy)){
			result.code = Response.NO_TRAVERSING_ENERGY;
			return result;
		}
		result.code = copyService.enter(playerId, map.getCopyId());
		if(result.code == Response.SUCCESS){
			int copyInstanceId = player.getCopyId();
			CopyInstance instance = copyService.getCopyInstance(copyInstanceId);
			instance.setTraverseMap(map);
			result.sceneId = cfg.scenes[0];// 第一个场景id
			result.copyId = instance.getCopyId();
			result.passId = instance.getPassId();
		}
		SessionManager.getInstance().sendMsg(CopyExtension.ENTER_COPY, result, playerId);*/
		Int2Param result = multiChellenge(playerId, param);
		if(result.param1 > 0){
			IntParam intParam = new IntParam();
			intParam.param = result.param1;
			return intParam;
		}
		teamExtension.enterCopy(playerId, null);
		return null;
	}
	
	
	@Command(4003)
	public Int2Param multiChellenge(int playerId, IntParam param){
		PlayerData playerData = playerService.getPlayerData(playerId);
		TraverseMap map = playerData.getTraverseMaps().get(param.param);
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, map.getCopyId());
		Int2Param result = new Int2Param();
		if(!playerService.verifyCurrency(playerId, Goods.TRAVERSING_ENERGY, cfg.needEnergy)){
			result.param1 = Response.NO_TRAVERSING_ENERGY;
		}else{
			Player player = playerService.getPlayer(playerId);
			if(player.getTeamId() > 0){
				result.param1 = Response.IN_TEAMING;
				return result;
			}
			Team team = teamService.createTeam(playerId, Team.TYPE_TRAVERSING, player.getName(), map.getCopyId());
			team.setMapId(param.param);
			result.param2 = team.getId();
		}
		return result;
	}
}
