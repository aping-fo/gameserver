package com.game.params.scene;

import com.game.params.*;

//切换状态(工具自动生成，请勿手动修改！）
public class ChangeFSMState implements IProtocol {
	public int actorId;//id
	public String eventName;//状态事件名
	public long endTime;//状态有效时间


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.eventName = bb.getString();
		this.endTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putString(this.eventName);
		bb.putLong(this.endTime);
	}
}
