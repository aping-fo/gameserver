package com.game.params;


//包含1个int 1个string的类型(工具自动生成，请勿手动修改！）
public class IntStringParam implements IProtocol {
	public int param1;//int参数
	public String param2;//String参数


	public void decode(BufferBuilder bb) {
		this.param1 = bb.getInt();
		this.param2 = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.param1);
		bb.putString(this.param2);
	}
}
