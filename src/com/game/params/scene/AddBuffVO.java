package com.game.params.scene;

import com.game.params.*;

//添加buff(工具自动生成，请勿手动修改！）
public class AddBuffVO implements IProtocol {
	public int actorId;//角色 id(包括玩家和怪物)
	public int giverId;//给与者 id
	public int buffId;//buff id
	public int buffType;//buff 类型
	public long endTime;//buff 结束时间


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.giverId = bb.getInt();
		this.buffId = bb.getInt();
		this.buffType = bb.getInt();
		this.endTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.giverId);
		bb.putInt(this.buffId);
		bb.putInt(this.buffType);
		bb.putLong(this.endTime);
	}
}
