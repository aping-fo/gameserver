package com.game.params.pet;

import com.game.params.*;

//宠物(工具自动生成，请勿手动修改！）
public class PetVO implements IProtocol {
	public int id;//宠物ID
	public int passiveSkillId;//宠物被动技能ID
	public int configId;//宠物配置ID
	public String name;//宠物名字


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.passiveSkillId = bb.getInt();
		this.configId = bb.getInt();
		this.name = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.passiveSkillId);
		bb.putInt(this.configId);
		bb.putString(this.name);
	}
}
