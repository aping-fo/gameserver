package com.game.params.gang;

import com.game.params.*;

//申请信息(工具自动生成，请勿手动修改！）
public class GangApply implements IProtocol {
	public int playerId;//玩家id
	public String name;//玩家名称
	public int vocation;//职业
	public int lev;//等级
	public int fightStrength;//战斗力
	public int vip;//vip
	public long lastLogin;//最后登录时间,(如果在线则为0)


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.vocation = bb.getInt();
		this.lev = bb.getInt();
		this.fightStrength = bb.getInt();
		this.vip = bb.getInt();
		this.lastLogin = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.vocation);
		bb.putInt(this.lev);
		bb.putInt(this.fightStrength);
		bb.putInt(this.vip);
		bb.putLong(this.lastLogin);
	}
}
