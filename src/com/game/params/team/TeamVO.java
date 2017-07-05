package com.game.params.team;

import com.game.params.*;

//队伍信息(工具自动生成，请勿手动修改！）
public class TeamVO implements IProtocol {
	public int id;//队伍id
	public String name;//名称
	public int copyId;//副本id
	public int member;//队伍人数
	public int leaderVocation;//队长职业
	public int leaderLev;//队长等级


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.name = bb.getString();
		this.copyId = bb.getInt();
		this.member = bb.getInt();
		this.leaderVocation = bb.getInt();
		this.leaderLev = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putString(this.name);
		bb.putInt(this.copyId);
		bb.putInt(this.member);
		bb.putInt(this.leaderVocation);
		bb.putInt(this.leaderLev);
	}
}
