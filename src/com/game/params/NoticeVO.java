package com.game.params;


//公告(工具自动生成，请勿手动修改！）
public class NoticeVO implements IProtocol {
	public String notice;//消息
	public int channel;//频道


	public void decode(BufferBuilder bb) {
		this.notice = bb.getString();
		this.channel = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.notice);
		bb.putInt(this.channel);
	}
}
