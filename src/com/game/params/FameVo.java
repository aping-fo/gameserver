package com.game.params;


//声望数据(工具自动生成，请勿手动修改！）
public class FameVo implements IProtocol {
	public int camp;//阵营
	public int lev;//等级
	public int exp;//经验
	public int totalExp;//累计声望


	public void decode(BufferBuilder bb) {
		this.camp = bb.getInt();
		this.lev = bb.getInt();
		this.exp = bb.getInt();
		this.totalExp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.camp);
		bb.putInt(this.lev);
		bb.putInt(this.exp);
		bb.putInt(this.totalExp);
	}
}
