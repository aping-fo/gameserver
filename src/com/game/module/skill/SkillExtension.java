package com.game.module.skill;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntList;
import com.game.params.IntParam;
import com.game.params.skill.SkillCardGroupInfo;
import com.server.anotation.Command;
import com.server.anotation.Extension;

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
	public Object upgradeSkill(int playerId,IntParam id){
		IntParam result = new IntParam();
		result.param = skillService.upgradeSkill(playerId, id.param);
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
		List<List<Integer>> cardgroup = data.getSkillCardSets();
		List<Integer> cards = null;
		if(param.param >= cardgroup.size()){
			cards = Arrays.asList(0, 0, 0, 0);
			cardgroup.add(cards);
			param.param = cardgroup.size() - 1;
		}else{
			cards = cardgroup.get(param.param);
			data.setCurCardId(param.param);
		}
		SkillCardGroupInfo info = new SkillCardGroupInfo();
		info.curGroupId = param.param;
		info.curCards = cards;
		return info;
	}
}
