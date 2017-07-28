package com.game.params;


//获得物品的通知消息(工具自动生成，请勿手动修改！）
public class GainGoodNotify implements IProtocol {
	public int code;//错误码
	public int id;//物品id
	public int count;//数量


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.id = bb.getInt();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.id);
		bb.putInt(this.count);
	}
}
