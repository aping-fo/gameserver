package com.game.params;


//称号(工具自动生成，请勿手动修改！）
public class TitleVO implements IProtocol {
	public int id;//称号ID
	public int time;//拥有时间
	public boolean openFlag;//是否打开过


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.time = bb.getInt();
		this.openFlag = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.time);
		bb.putBoolean(this.openFlag);
	}
}
