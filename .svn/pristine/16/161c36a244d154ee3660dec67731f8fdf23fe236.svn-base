package com.game.params.mail;

import com.game.params.*;

//邮件奖励(工具自动生成，请勿手动修改！）
public class SMailReward implements IProtocol {
	public int code;//错误码
	public String rewards;//附件，格式 id:数量;id:数量


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.rewards = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putString(this.rewards);
	}
}
