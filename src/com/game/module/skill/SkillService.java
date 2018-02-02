package com.game.module.skill;

import com.game.data.Response;
import com.game.data.SkillCardConfig;
import com.game.data.SkillConfig;
import com.game.module.log.LogConsume;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.skill.SkillCardGroupInfo;
import com.game.params.skill.SkillCardVo;
import com.game.params.skill.SkillInfo;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**  
 * 技能系统
 */
@Service
public class SkillService {
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private TaskService taskService;


	//获取技能信息
	public SkillInfo getInfo(int playerId){
		SkillInfo info = new SkillInfo();
		PlayerData data = playerService.getPlayerData(playerId);
		info.cardGroupInfo = new SkillCardGroupInfo();
		info.cardGroupInfo.curGroupId = data.getCurCardId();
		info.cardGroupInfo.curCards = new ArrayList<>(data.getCurrCard());
		info.curSkills = new ArrayList<>(data.getCurSkills());
		info.skills = new ArrayList<>(data.getSkills());
		info.skillCards = new ArrayList<>(data.getSkillCards().size());
		for(Entry<Integer, SkillCard> card:data.getSkillCards().entrySet()){
			SkillCardVo vo = new SkillCardVo();
			vo.id = card.getKey();
			vo.exp = card.getValue().getExp();
			vo.cardId = card.getValue().getCardId();
			vo.lev = card.getValue().getLev();
			info.skillCards.add(vo);
		}
		return info;
	}
	
	//升级技能
	public int upgradeSkill(int playerId,int skillId){
		//是否已经满级
		SkillConfig cfg = ConfigData.getConfig(SkillConfig.class, skillId);
		if(cfg.nextId==0){
			return Response.MAX_LEV;
		}
		//扣除金币
		if(!playerService.decCoin(playerId, cfg.coin, LogConsume.SKILL_UPGRADE)){
			return Response.NO_COIN;
		}
		//设置id
		PlayerData data = playerService.getPlayerData(playerId);
		int index = data.getSkills().indexOf(skillId);
		if(index == -1) {
			ServerLogger.warn("skill not found ,skillId ==>" + skillId);
			return Response.ERR_PARAM;
		}
		data.getSkills().set(index, cfg.nextId);
		index = data.getCurSkills().indexOf(skillId);
		if(index>=0){
			data.getCurSkills().set(index, cfg.nextId);
		}
		taskService.doTask(playerId, Task.FINISH_SKILL);
		taskService.doTask(playerId, Task.TYPE_SKILL_LEVEL,cfg.nextId);

		updateSkill2Client(playerId);
		return Response.SUCCESS;
	}
	
	//更新技能信息
	public void updateSkill2Client(int playerId){
		SessionManager.getInstance().sendMsg(SkillExtension.UPDATE_SKILL, getInfo(playerId), playerId);
	}
	
	//升级技能卡
	public int upgradeSkillCard(int playerId,List<Integer> ids){
		PlayerData data = playerService.getPlayerData(playerId);
		int id = ids.get(0);
		SkillCard card = data.getSkillCards().get(id);
		//已经是最高级
		SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
		if(cfg.nextCard==0){
			return Response.MAX_LEV;
		}
		if(cfg.type == SkillCard.SPECIAL){
			return Response.ERR_PARAM;
		}
		//逐个经验添加
		boolean full = false;
		for(int i=1;i<ids.size();i++){
			SkillCard del = data.getSkillCards().get(ids.get(i));
			SkillCardConfig delCfg = ConfigData.getConfig(SkillCardConfig.class, del.getCardId());
			card.setExp(card.getExp()+delCfg.decompose);
			//判断升级
			while(card.getExp()>=cfg.exp){
				card.setLev(card.getLev()+1);
				card.setExp(card.getExp()-cfg.exp);
				card.setCardId(cfg.nextCard);
				
				cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
				if(cfg.nextCard==0){
					full = true;
					break;
				}
			}
			//扣除
			data.getSkillCards().remove(ids.get(i));
			for(List<Integer> group : data.getSkillCardSets()){
				for(int j = 0; j < 4; j++){
					if(group.get(j) == ids.get(i)){
						group.set(j, 0);
					}
				}
			}
			if(full){
				break;
			}
		}
		taskService.doTask(playerId, Task.FINISH_CARD_UPGRADE, cfg.lv);
		//更新前端
		updateSkill2Client(playerId);
		return Response.SUCCESS;
	}
	
	//合成技能卡
	public int composeCard(int playerId, List<Integer> ids) {
		PlayerData data = playerService.getPlayerData(playerId);
		SkillCard card = data.getSkillCards().get(ids.get(0));
		if(card == null) {
			return Response.ERR_PARAM;
		}
		SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
		int newCardID = cfg.nextQualityCard;

		if(ids.size() < 5) {
			return Response.ERR_PARAM;
		}
		//检查副卡类型，品质
		for(int i = 1; i < ids.size();i++){
			SkillCard sc = data.getSkillCards().get(ids.get(i));
			SkillCardConfig scc = ConfigData.getConfig(SkillCardConfig.class, sc.getCardId());
			if(scc.type == SkillCard.SPECIAL || scc.quality != cfg.quality || scc.quality == 5
					|| cfg.goodsid != scc.goodsid){
				return Response.ERR_PARAM;
			}
		}

		SkillCard newCard = playerService.addSkillCard(playerId, newCardID);
		for (int delId : ids) {
			//扣除
			SkillCard del = data.getSkillCards().remove(delId);
			for (List<Integer> group : data.getSkillCardSets()) {
				for (int i = 0; i < 4; i++) {
					if (group.get(i) == delId) {
						group.set(i, 0);
					}
				}
			}
			SkillCardConfig delCfg = ConfigData.getConfig(SkillCardConfig.class, del.getCardId());
			//增加经验
			newCard.setExp(newCard.getExp() + del.getExp() + delCfg.decompose);
		}
		//检查升级
		cfg = ConfigData.getConfig(SkillCardConfig.class, newCard.getCardId());
		newCard.setExp(newCard.getExp() - cfg.decompose);
		while (newCard.getExp() >= cfg.exp) {
			newCard.setLev(newCard.getLev() + 1);
			newCard.setExp(newCard.getExp() - cfg.exp);
			newCard.setCardId(cfg.nextCard);

			cfg = ConfigData.getConfig(SkillCardConfig.class, newCard.getCardId());
			if (cfg.nextCard == 0) {
				break;
			}
		}
		taskService.doTask(playerId, Task.FINISH_CARD_COMPOSE, cfg.quality + 1, 1);
		//更新前端
		updateSkill2Client(playerId);
		return Response.SUCCESS;
	}

	//使用卡牌
	public int setCard(int playerId,int index,int id){
		PlayerData data = playerService.getPlayerData(playerId);
		if(id>0){
			if(!data.getSkillCards().containsKey(id)){
				return Response.ERR_PARAM;
			}
			SkillCard card = data.getSkillCards().get(id);
			SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
			if(cfg.type == SkillCard.SPECIAL){
				return Response.ERR_PARAM;
			}
			int oldIndex = data.getCurrCard().indexOf(id);
			if(oldIndex>=0){
				data.getCurrCard().set(oldIndex, 0);
			}
		}
		data.getCurrCard().set(index, id);
		updateSkill2Client(playerId);
		playerService.refreshPlayerToClient(playerId);
		taskService.doTask(playerId, Task.FINISH_SKILL);
		return Response.SUCCESS;
	}

	public void gmAddSkillCard(int playerId,int cardId) {
		playerService.addSkillCard(playerId, cardId);
		updateSkill2Client(playerId);
	}
}
