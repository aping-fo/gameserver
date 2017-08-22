package com.game.params.worldboss;

import com.game.params.*;

//怪物伤害(工具自动生成，请勿手动修改！）
public class MonsterHurtVO implements IProtocol {
	public int monsterId;//怪物ID
	public int curHp;//当前HP
	public int hurt;//当前伤害


	public void decode(BufferBuilder bb) {
		this.monsterId = bb.getInt();
		this.curHp = bb.getInt();
		this.hurt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.monsterId);
		bb.putInt(this.curHp);
		bb.putInt(this.hurt);
	}
}
