package com.game.params.pet;

import com.game.params.*;

//变异请求参数(工具自动生成，请勿手动修改！）
public class MutateVO implements IProtocol {
	public int mutateID;//变异宠物ID
	public int consumeID;//消耗宠物ID
	public int newSkillID;//技能ID


	public void decode(BufferBuilder bb) {
		this.mutateID = bb.getInt();
		this.consumeID = bb.getInt();
		this.newSkillID = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.mutateID);
		bb.putInt(this.consumeID);
		bb.putInt(this.newSkillID);
	}
}
