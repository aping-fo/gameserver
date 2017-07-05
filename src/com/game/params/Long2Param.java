package com.game.params;


//包含2个long类型的vo(工具自动生成，请勿手动修改！）
public class Long2Param implements IProtocol {
	public long param1;//long参数1
	public long param2;//long参数2


	public void decode(BufferBuilder bb) {
		this.param1 = bb.getLong();
		this.param2 = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putLong(this.param1);
		bb.putLong(this.param2);
	}
}
