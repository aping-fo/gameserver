package com.game.params.worldboss;

import com.game.params.*;

//BOSS信息(工具自动生成，请勿手动修改！）
public class BossVo implements IProtocol {
	public int monsterId;//怪物id
	public int hp;//总血量
	public int curHp;//当前血量


	public void decode(BufferBuilder bb) {
		this.monsterId = bb.getInt();
		this.hp = bb.getInt();
		this.curHp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.monsterId);
		bb.putInt(this.hp);
		bb.putInt(this.curHp);
	}
}
