package com.game.module.skill;

import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntList;
import com.game.params.IntParam;
import com.game.params.skill.SkillCardGroupInfo;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**  
 * 技能系统
 */
@Extension
public class SkillExtension {

	@Autowired
	private SkillService skillService;
	@Autowired
	private PlayerService playerService;
	
	public static final int UPDATE_SKILL = 2001;
	@Command(2001)
	public Object getSkillInfo(int playerId,Object p){
		return skillService.getInfo(playerId);
	}
	
	@Command(2002)
	public Object upgradeSkill(int playerId,Int2Param param){
		IntParam result = new IntParam();
		result.param = skillService.upgradeSkill(playerId, param.param1,param.param2);
		return result;
	}
	
	@Command(2003)
	public Object setCard(int playerId,Int2Param card){
		IntParam result = new IntParam();
		result.param = skillService.setCard(playerId, card.param1, card.param2);
		return result;
	}
	
	
	@Command(2004)
	public Object upgradeCard(int playerId,IntList card){
		IntParam result = new IntParam();
		result.param = skillService.upgradeSkillCard(playerId, card.iList);
		return result;
	}
	
	@Command(2005)
	public Object compose(int playerId,IntList card){
		IntParam result = new IntParam();
		result.param = skillService.composeCard(playerId, card.iList);
		return result;
	}
	
	@Command(2006)
	public Object setCardGroup(int playerId, IntParam param)
	{
		PlayerData data = playerService.getPlayerData(playerId);
		Player player = playerService.getPlayer(playerId);
		List<List<Integer>> cardgroup = data.getSkillCardSets();
		int vip = player.getVip();
		int count = 0;
		for(int vipLev : ConfigData.globalParam().skillCardGroupOpenVip) {
			if(vipLev <= vip) {
				count += 1;
			}
		}

		List<Integer> cards = null;
		while (cardgroup.size() < count) {
			cards = Arrays.asList(0, 0, 0, 0);
			cardgroup.add(cards);
		}

		if(param.param >= cardgroup.size()){
			cards = Arrays.asList(0, 0, 0, 0);
			cardgroup.add(cards);
			param.param = cardgroup.size() - 1;
		}else{
			cards = cardgroup.get(param.param);
		}
		data.setCurCardId(param.param);
		SkillCardGroupInfo info = new SkillCardGroupInfo();
		info.curGroupId = param.param;
		info.curCards = cards;
		return info;
	}
}
