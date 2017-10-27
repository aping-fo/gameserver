package com.game.params.pet;

import com.game.params.*;

//宠物(工具自动生成，请勿手动修改！）
public class PetVO implements IProtocol {
	public int id;//宠物ID
	public int skillId;//宠物主动技能ID
	public int passiveSkillId;//宠物被动技能ID


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.skillId = bb.getInt();
		this.passiveSkillId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.skillId);
		bb.putInt(this.passiveSkillId);
	}
}
