package com.game.module.attach.training;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.Response;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.log.LogConsume;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.training.TrainOpponentVO;
import com.game.params.training.TrainingRewardVO;
import com.game.params.training.TrainingVO;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class TrainingExtension {

	@Autowired
	private trainingLogic logic;
	@Autowired
	private RandomRewardService rewardService;
	
	//获取相关信息
	@Command(3901)
	public TrainingVO getInfo(int playerId, Object param){
		TrainAttach attach = logic.getAttach(playerId);
		TrainingVO vo = new TrainingVO();
		vo.index = attach.getIndex();
		vo.hp = attach.getHp();
		vo.treasureBox = new ArrayList<Integer>(attach.getTreasureBox());
		List<TrainOpponentVO> list = new ArrayList<TrainOpponentVO>();
		List<Integer> ids = attach.getOpponents();
		for(int i = ids.size() - 1; i >= 0; i--){
			int id = ids.get(i);
			if(id == playerId) continue;
			TrainOpponent opponent = logic.getOpponent(id);
			TrainOpponentVO opp = new TrainOpponentVO();
			opp.playerId = opponent.getPlayerId();
			opp.name = opponent.getName();
			opp.level = opponent.getLevel();
			opp.exp = opponent.getExp();
			opp.vip = opponent.getVip();
			opp.vipExp = opponent.getVipExp();
			opp.gang = opponent.getGang();
			opp.vocation = opponent.getVocation();
			opp.fashionId = opponent.getFashionId();
			opp.weapon = opponent.getWeaponId();
			opp.curCards = opponent.getCurCards();
			opp.curSkills = opponent.getCurSkills();
			list.add(opp);
			if(list.size() >= 10) break;
		}
		vo.opponents = list;
		return vo;
	}
	
	//挑战
	@Command(3902)
	public IntParam challenge(int playerId, IntParam param){
		IntParam result = new IntParam();
		TrainAttach attach = logic.getAttach(playerId);
		if(attach.getHp() <= 0 || param.param >= logic.getMaxLevel() || param.param != attach.getIndex()){
			result.param = Response.ERR_PARAM;
		}
		return result;
		
	}
	
	//挑战胜利
	@Command(3903)
	public IntParam challengeWin(int playerId, Int2Param param){
		IntParam result = new IntParam();
		TrainAttach attach = logic.getAttach(playerId);
		if(param.param1 >= logic.getMaxLevel() || param.param1 != attach.getIndex() || attach.getHp() <param.param2){
			result.param = Response.ERR_PARAM;
		}else{			
			attach.setHp(param.param2);
			if(param.param2 > 0){				
				attach.setIndex(param.param1 + 1);
				attach.getTreasureBox().add(param.param1);
			}
			attach.commitSync();
		}
		return result;
	}
	
	//领取奖励
	@Command(3904)
	public TrainingRewardVO takeReward(int playerId, IntParam param){
		TrainingRewardVO result = new TrainingRewardVO();
		TrainAttach attach = logic.getAttach(playerId);
		int index = param.param;
		if(!attach.getTreasureBox().contains(index)){
			result.code = Response.ERR_PARAM;
		}else{
			attach.getTreasureBox().remove(index);
			int groupId = ConfigData.globalParam().exprienceRewards[index];
			result.rewards = rewardService.getRewards(playerId, groupId, LogConsume.EXPRIENCE_REWARD);
			attach.commitSync();
		}
		result.index = index;
		return result;
	}
}
