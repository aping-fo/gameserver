package com.game.params.team;

import com.game.params.*;

//队员信息(工具自动生成，请勿手动修改！）
public class TeamMemberVO implements IProtocol {
	public int memberId;//队员Id
	public boolean ready;//是否已准备


	public void decode(BufferBuilder bb) {
		this.memberId = bb.getInt();
		this.ready = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.memberId);
		bb.putBoolean(this.ready);
	}
}
