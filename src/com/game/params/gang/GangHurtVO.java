package com.game.params.gang;

import com.game.params.*;

//公会副本伤害信息(工具自动生成，请勿手动修改！）
public class GangHurtVO implements IProtocol {
	public String name;//角色名
	public int level;//角色等级
	public int vocation;//职业
	public int hurt;//伤害


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.level = bb.getInt();
		this.vocation = bb.getInt();
		this.hurt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.level);
		bb.putInt(this.vocation);
		bb.putInt(this.hurt);
	}
}
