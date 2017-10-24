package com.game.params.team;

import com.game.params.*;

//队员信息(工具自动生成，请勿手动修改！）
public class TeamMemberVO implements IProtocol {
	public int memberId;//队员Id
	public boolean ready;//是否已准备
	public int fight;//战力
	public int lev;//等级


	public void decode(BufferBuilder bb) {
		this.memberId = bb.getInt();
		this.ready = bb.getBoolean();
		this.fight = bb.getInt();
		this.lev = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.memberId);
		bb.putBoolean(this.ready);
		bb.putInt(this.fight);
		bb.putInt(this.lev);
	}
}
