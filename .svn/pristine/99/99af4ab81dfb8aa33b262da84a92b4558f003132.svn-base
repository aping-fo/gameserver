package com.game.params.gang;

import com.game.params.*;

//公会成员信息(工具自动生成，请勿手动修改！）
public class GangMember implements IProtocol {
	public int playerId;//玩家id
	public String name;//玩家名称
	public int lev;//等级
	public int fightStrength;//战斗力
	public int donate7;//最近7天的捐献
	public int position;//职位(1会长2副会长0成员)
	public boolean online;//是否在线
	public int vip;//vip
	public int vocation;//职业
	public int taskContribution;//任务贡献值


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.lev = bb.getInt();
		this.fightStrength = bb.getInt();
		this.donate7 = bb.getInt();
		this.position = bb.getInt();
		this.online = bb.getBoolean();
		this.vip = bb.getInt();
		this.vocation = bb.getInt();
		this.taskContribution = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.lev);
		bb.putInt(this.fightStrength);
		bb.putInt(this.donate7);
		bb.putInt(this.position);
		bb.putBoolean(this.online);
		bb.putInt(this.vip);
		bb.putInt(this.vocation);
		bb.putInt(this.taskContribution);
	}
}
