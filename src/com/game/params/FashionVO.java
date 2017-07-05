package com.game.params;


//时装信息(工具自动生成，请勿手动修改！）
public class FashionVO implements IProtocol {
	public int id;//时装ID
	public long createTime;//获取时间
	public int period;//有效时长，单位秒


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.createTime = bb.getLong();
		this.period = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putLong(this.createTime);
		bb.putInt(this.period);
	}
}
