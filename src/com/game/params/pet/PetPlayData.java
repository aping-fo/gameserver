package com.game.params.pet;

import java.util.List;
import com.game.params.*;

//宠物玩法请求参数(工具自动生成，请勿手动修改！）
public class PetPlayData implements IProtocol {
	public int activityId;//活动ID
	public List<Integer> petIds;//宠物ID列表


	public void decode(BufferBuilder bb) {
		this.activityId = bb.getInt();
		this.petIds = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.activityId);
		bb.putIntList(this.petIds);
	}
}
