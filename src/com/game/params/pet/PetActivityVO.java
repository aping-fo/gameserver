package com.game.params.pet;

import com.game.params.*;

//宠物玩法信息(工具自动生成，请勿手动修改！）
public class PetActivityVO implements IProtocol {
	public int id;//玩法配置ID
	public int remainTime;//剩余时间
	public int petId;//参加活动的宠物ID


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.remainTime = bb.getInt();
		this.petId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.remainTime);
		bb.putInt(this.petId);
	}
}
