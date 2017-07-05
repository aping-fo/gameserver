package com.game.params.copy;

import com.game.params.*;

//经验副本信息(工具自动生成，请勿手动修改！）
public class ExperienceInfo implements IProtocol {
	public int challenge;//剩余挑战次数
	public int buyTime;//购买次数
	public long lastChallengeTime;//挑战CD


	public void decode(BufferBuilder bb) {
		this.challenge = bb.getInt();
		this.buyTime = bb.getInt();
		this.lastChallengeTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.challenge);
		bb.putInt(this.buyTime);
		bb.putLong(this.lastChallengeTime);
	}
}
