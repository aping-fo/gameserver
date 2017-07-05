package com.game.module.attach.lottery;

import org.springframework.stereotype.Service;

import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.params.lottery.LotteryVO;

@Service
public class LotteryLogic extends AttachLogic<LotteryAttach> {

	@Override
	public byte getType() {
		return AttachType.LOTTERY;
	}

	@Override
	public LotteryAttach generalNewAttach(int playerId) {
		return new LotteryAttach(playerId, getType());
	}

	public void dailyReset(int playerId){
		LotteryAttach attach = getAttach(playerId);
		for(LotteryVO vo : attach.getRecords().values()){
			vo.count = 0;
			vo.freeCount = 0;
		}
		attach.commitSync();
	}
}
