package com.game.params.mail;

import com.game.params.*;

//邮件信息(工具自动生成，请勿手动修改！）
public class MailVo implements IProtocol {
	public long id;//唯一id
	public int senderId;//发件人id
	public String senderName;//发送者姓名
	public int receiverId;//收件人id
	public String receiverName;//收件人姓名
	public String title;//标题
	public String content;//内容
	public String rewards;//奖励(id:count;id:count)
	public boolean hasReward;//是否有附件
	public int state;//状态0未读1已读
	public long sendTime;//发送时间


	public void decode(BufferBuilder bb) {
		this.id = bb.getLong();
		this.senderId = bb.getInt();
		this.senderName = bb.getString();
		this.receiverId = bb.getInt();
		this.receiverName = bb.getString();
		this.title = bb.getString();
		this.content = bb.getString();
		this.rewards = bb.getString();
		this.hasReward = bb.getBoolean();
		this.state = bb.getInt();
		this.sendTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putLong(this.id);
		bb.putInt(this.senderId);
		bb.putString(this.senderName);
		bb.putInt(this.receiverId);
		bb.putString(this.receiverName);
		bb.putString(this.title);
		bb.putString(this.content);
		bb.putString(this.rewards);
		bb.putBoolean(this.hasReward);
		bb.putInt(this.state);
		bb.putLong(this.sendTime);
	}
}
