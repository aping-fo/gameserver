package com.game.params.chat;

import com.game.params.*;

//聊天信息(工具自动生成，请勿手动修改！）
public class ChatVo implements IProtocol {
	public int channel;//频道(1世界频道2私聊)
	public String sender;//发送者
	public int senderId;//发送者Id
	public String content;//内容
	public String recordUrl;//语音url
	public int receiveId;//私聊对方Id
	public int senderLev;//发送者等级
	public int senderVip;//发送者vip
	public int senderVocation;//发送者职业
	public String senderGang;//发送者仙盟
	public int fight;//战斗力
	public long time;//发送时间


	public void decode(BufferBuilder bb) {
		this.channel = bb.getInt();
		this.sender = bb.getString();
		this.senderId = bb.getInt();
		this.content = bb.getString();
		this.recordUrl = bb.getString();
		this.receiveId = bb.getInt();
		this.senderLev = bb.getInt();
		this.senderVip = bb.getInt();
		this.senderVocation = bb.getInt();
		this.senderGang = bb.getString();
		this.fight = bb.getInt();
		this.time = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.channel);
		bb.putString(this.sender);
		bb.putInt(this.senderId);
		bb.putString(this.content);
		bb.putString(this.recordUrl);
		bb.putInt(this.receiveId);
		bb.putInt(this.senderLev);
		bb.putInt(this.senderVip);
		bb.putInt(this.senderVocation);
		bb.putString(this.senderGang);
		bb.putInt(this.fight);
		bb.putLong(this.time);
	}
}
