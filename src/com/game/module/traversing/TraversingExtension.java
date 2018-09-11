package com.game.module.traversing;

import java.util.ArrayList;
import java.util.List;

import com.game.module.copy.CopyExtension;
import com.game.module.team.TMember;
import com.game.params.scene.SScenePlayerVo;
import com.game.util.RandomUtil;
import com.google.common.collect.Lists;
import com.server.SessionManager;
import com.server.util.ServerLogger;
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
        data.setSingleAndMulti(0);
		return result;
	}

    @Command(4002)
    public Object singleChellenge(int playerId, IntParam param) {
        Int2Param result = multiChellenge(playerId, param);
        if (result.param1 > 0) {
            IntParam intParam = new IntParam();
            intParam.param = result.param1;
            return intParam;
        }
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在=" + playerId);
        } else {
            playerData.setSingleAndMulti(0);
        }


        Player player = playerService.getPlayer(playerId);
        int gender = player.getSex();
        SScenePlayerVo robot = new SScenePlayerVo();
        List<String> firstNames = ConfigData.FirstNameList.get(gender);
        List<String> secondNames = ConfigData.SecondNameList.get(gender);

        String name = firstNames.get(RandomUtil.randInt(firstNames.size()));
        name += secondNames.get(RandomUtil.randInt(secondNames.size()));
        robot.lev = player.getLev();
        float rate = ConfigData.globalParam().ladderAiRate;
        robot.attack = (int) (player.getAttack() * rate);
        robot.defense = (int) (player.getDefense() * rate);
        robot.crit = (int) (player.getCrit() * rate);
        robot.symptom = (int) (player.getSymptom() * rate);
        robot.fu = (int) (player.getFu() * rate);
        robot.hp = (int) (player.getHp() * rate);
        robot.curCards = playerData.getCurrCardIds();
        robot.curSkills = Lists.newArrayList();
        int voction = RandomUtil.randInt(3) + 1;
        int[] skills = ConfigData.globalParam().playerDefaultSkill[voction - 1];
        for (int skill : skills) {
            robot.curSkills.add(skill);
        }
        robot.name = name;
        robot.vocation = voction;
        robot.playerId = Integer.MAX_VALUE;

        int head = ConfigData.headList.get(voction).get(RandomUtil.randInt(ConfigData.headList.size()));
        int cloth = ConfigData.clothList.get(voction).get(RandomUtil.randInt(ConfigData.clothList.size()));
        int weapon = ConfigData.weaponList.get(voction).get(RandomUtil.randInt(ConfigData.weaponList.size()));
        robot.head = head;
        robot.fashionId = cloth;
        robot.weapon = weapon;

        SessionManager.getInstance().sendMsg(4002, robot, playerId);
        teamExtension.enterCopy(playerId, null);

        Team team = teamService.getTeam(player.getTeamId());
        if (team != null) { //加入机器人
            TMember member = new TMember();
            member.setPlayerId(robot.playerId);
            member.setTotalHp(robot.hp);
            member.setCurHp(robot.hp);
            member.setReady(true);
            team.addMember(member);
        }
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
            playerData.setSingleAndMulti(1);
		}
		return result;
	}
}
