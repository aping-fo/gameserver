package com.game.params.ladder;

import com.game.params.*;

//排位赛成员信息(工具自动生成，请勿手动修改！）
public class LadderMemberVO implements IProtocol {
	public String name;//名称
	public int vocation;//职业
	public int level;//等级
	public int ladderLevel;//排位等级
	public int team;//队伍
	public int playerId;//玩家ID


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.vocation = bb.getInt();
		this.level = bb.getInt();
		this.ladderLevel = bb.getInt();
		this.team = bb.getInt();
		this.playerId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.vocation);
		bb.putInt(this.level);
		bb.putInt(this.ladderLevel);
		bb.putInt(this.team);
		bb.putInt(this.playerId);
	}
}
