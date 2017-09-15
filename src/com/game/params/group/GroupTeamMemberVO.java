package com.game.params.group;

import com.game.params.*;

//团队队伍成员信息(工具自动生成，请勿手动修改！）
public class GroupTeamMemberVO implements IProtocol {
	public int playerId;//成员ID
	public boolean readyFlag;//是否准备


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.readyFlag = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putBoolean(this.readyFlag);
	}
}
