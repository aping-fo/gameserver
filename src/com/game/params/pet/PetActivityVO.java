package com.game.params.pet;

import java.util.List;
import com.game.params.*;

//宠物玩法信息(工具自动生成，请勿手动修改！）
public class PetActivityVO implements IProtocol {
	public int id;//玩法配置ID
	public int remainTime;//剩余时间
	public List<Integer> petId;//参加活动的宠物ID 列表


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.remainTime = bb.getInt();
		this.petId = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.remainTime);
		bb.putIntList(this.petId);
	}
}
