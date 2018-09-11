package com.game.params;


//facebook邀请的玩家信息(工具自动生成，请勿手动修改！）
public class FacebookInvitedPlayerVO implements IProtocol {
	public int id;//玩家id
	public String name;//玩家名称
	public int lev;//等级
	public int vocation;//职业
	public int fighting;//战力


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.name = bb.getString();
		this.lev = bb.getInt();
		this.vocation = bb.getInt();
		this.fighting = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putString(this.name);
		bb.putInt(this.lev);
		bb.putInt(this.vocation);
		bb.putInt(this.fighting);
	}
}
