package com.game.params.gang;

import com.game.params.*;

//怪物信息(工具自动生成，请勿手动修改！）
public class MonsterVo implements IProtocol {
	public int id;//怪物ID
	public int curHp;//当前血量
	public int hp;//总血量


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.curHp = bb.getInt();
		this.hp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.curHp);
		bb.putInt(this.hp);
	}
}
