package com.game.params;


//包含2个string类型的vo(工具自动生成，请勿手动修改！）
public class String2Param implements IProtocol {
	public String param1;//string参数1
	public String param2;//string参数2


	public void decode(BufferBuilder bb) {
		this.param1 = bb.getString();
		this.param2 = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.param1);
		bb.putString(this.param2);
	}
}
