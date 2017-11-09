package com.game.params.worldboss;

import com.game.params.*;

//受击伤害(工具自动生成，请勿手动修改！）
public class MonsterHurtVO implements IProtocol {
	public int actorId;//角色 id(包括玩家和怪物)
	public int curHp;//当前血量
	public int hurt;//当前伤害
	public byte type;//类型，0：玩家，1：怪物
	public boolean isCrit;//是否是暴击


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.curHp = bb.getInt();
		this.hurt = bb.getInt();
		this.type = bb.getByte();
		this.isCrit = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.curHp);
		bb.putInt(this.hurt);
		bb.putByte(this.type);
		bb.putBoolean(this.isCrit);
	}
}
