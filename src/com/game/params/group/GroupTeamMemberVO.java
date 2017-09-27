package com.game.params.group;

import com.game.params.*;

//团队队伍成员信息(工具自动生成，请勿手动修改！）
public class GroupTeamMemberVO implements IProtocol {
	public int playerId;//成员ID
	public int vocation;//职业
	public String name;//名字
	public int fight;//战力
	public int lev;//等级
	public boolean readyFlag;//是否准备


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.vocation = bb.getInt();
		this.name = bb.getString();
		this.fight = bb.getInt();
		this.lev = bb.getInt();
		this.readyFlag = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putInt(this.vocation);
		bb.putString(this.name);
		bb.putInt(this.fight);
		bb.putInt(this.lev);
		bb.putBoolean(this.readyFlag);
	}
}
