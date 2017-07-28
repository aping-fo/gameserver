package com.game.params.task;

import com.game.params.*;

//任务数据(工具自动生成，请勿手动修改！）
public class SJointTaskVo implements IProtocol {
	public int id;//任务id
	public int playerId;//玩家id
	public String name;//玩家名称
	public int lev;//等级
	public int vocation;//职业
	public boolean online;//是否在线


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.lev = bb.getInt();
		this.vocation = bb.getInt();
		this.online = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.lev);
		bb.putInt(this.vocation);
		bb.putBoolean(this.online);
	}
}
