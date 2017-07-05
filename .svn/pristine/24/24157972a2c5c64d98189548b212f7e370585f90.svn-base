package com.game.params;


//包含1个String类型的vo(工具自动生成，请勿手动修改！）
public class StringParam implements IProtocol {
	public String param;//String参数


	public void decode(BufferBuilder bb) {
		this.param = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.param);
	}
}
