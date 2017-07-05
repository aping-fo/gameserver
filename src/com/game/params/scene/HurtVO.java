package com.game.params.scene;

import com.game.params.*;

//伤害效果(工具自动生成，请勿手动修改！）
public class HurtVO implements IProtocol {
	public int type;//目标类型 0玩家 1怪物
	public int targetId;//受伤者id（怪物，是唯一id;玩家，是playerId)
	public int hurtValue;//伤害


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.targetId = bb.getInt();
		this.hurtValue = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.targetId);
		bb.putInt(this.hurtValue);
	}
}
