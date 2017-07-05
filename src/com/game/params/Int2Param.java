package com.game.params;


//包含2个int类型的vo(工具自动生成，请勿手动修改！）
public class Int2Param implements IProtocol {
	public int param1;//int参数1
	public int param2;//int参数2


	public void decode(BufferBuilder bb) {
		this.param1 = bb.getInt();
		this.param2 = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.param1);
		bb.putInt(this.param2);
	}
}
