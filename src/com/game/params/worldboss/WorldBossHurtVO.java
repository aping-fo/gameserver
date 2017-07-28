package com.game.params.worldboss;

import com.game.params.*;

//世界boss伤害(工具自动生成，请勿手动修改！）
public class WorldBossHurtVO implements IProtocol {
	public String nickName;//角色名称
	public int hurt;//伤害


	public void decode(BufferBuilder bb) {
		this.nickName = bb.getString();
		this.hurt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.nickName);
		bb.putInt(this.hurt);
	}
}
