package com.game.module.skill;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.params.Int2Param;
import com.game.params.IntList;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;

/**  
 * 技能系统
 */
@Extension
public class SkillExtension {

	@Autowired
	private SkillService skillService;
	
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
}
