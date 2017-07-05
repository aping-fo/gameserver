package com.game.params.goods;

import com.game.params.*;

//宝石(工具自动生成，请勿手动修改！）
public class Jewel implements IProtocol {
	public int type;//部位
	public int lev;//等级
	public int exp;//经验值


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.lev = bb.getInt();
		this.exp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.lev);
		bb.putInt(this.exp);
	}
}
