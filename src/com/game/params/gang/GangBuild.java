package com.game.params.gang;

import com.game.params.*;

//公会建筑(工具自动生成，请勿手动修改！）
public class GangBuild implements IProtocol {
	public int type;//类型
	public int lev;//等级


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.lev = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.lev);
	}
}
