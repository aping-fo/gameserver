package com.game.module.attach.charge;

import java.util.List;

import org.springframework.stereotype.Service;

import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;

@Service
public class ChargeActivityLogic extends AttachLogic<ChargeActivity> {
	@Override
	public byte getType() {
		return AttachType.CHARGE_REWARD;
	}

	@Override
	public ChargeActivity generalNewAttach(int playerId) {
		ChargeActivity chargeActivity = new ChargeActivity();
		chargeActivity.setPlayerId(playerId);
		chargeActivity.setType(getType());
		return chargeActivity;
	}

	// 清除数据
	public void clear() {
		attachService.clear(getType());
	}

	// 检测
	public boolean check(int playerId, int charge) {
		ChargeActivity reward = attachService.getAttach(playerId, getType());
		return reward.getCharge() >= charge && !reward.getRecords().contains(charge);
	}

	// 已经领取的记录
	public List<Integer> getRewards(int playerId) {
		ChargeActivity reward = attachService.getAttach(playerId, getType());
		return reward.getRecords();
	}

	public int getChargeCount(int playerId) {
		ChargeActivity reward = attachService.getAttach(playerId, getType());
		return reward.getCharge();
	}

	// 更新
	public void updateTaken(int playerId, int charge) {
		ChargeActivity reward = attachService.getAttach(playerId, getType());
		if (!reward.getRecords().contains(charge)) {
			reward.getRecords().add(charge);
		}
		reward.commitSync();
	}

	// 更新充值值
	public void updateCharge(int playerId, int charge) {
		ChargeActivity reward = attachService.getAttach(playerId, getType());
		reward.setCharge(reward.getCharge() + charge);
		reward.commitSync();
	}
}
