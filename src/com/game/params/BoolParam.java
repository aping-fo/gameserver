package com.game.params;


//包含1个bool类型的vo(工具自动生成，请勿手动修改！）
public class BoolParam implements IProtocol {
	public boolean param;//int参数


	public void decode(BufferBuilder bb) {
		this.param = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putBoolean(this.param);
	}
}
