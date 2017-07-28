package com.game.params.scene;

import com.game.params.*;

//使用技能(工具自动生成，请勿手动修改！）
public class StopSkillVO implements IProtocol {
	public int type;//攻击者类型 0玩家 1怪物
	public int attackId;//攻击者id


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.attackId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.attackId);
	}
}
