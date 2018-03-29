package com.game.params;


//3个int类型(工具自动生成，请勿手动修改！）
public class Int3Param implements IProtocol {
	public int param1;//int1
	public int param2;//int2
	public int param3;//int3


	public void decode(BufferBuilder bb) {
		this.param1 = bb.getInt();
		this.param2 = bb.getInt();
		this.param3 = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.param1);
		bb.putInt(this.param2);
		bb.putInt(this.param3);
	}
}
