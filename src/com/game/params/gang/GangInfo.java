package com.game.params.gang;

import com.game.params.*;

//公会信息(工具自动生成，请勿手动修改！）
public class GangInfo implements IProtocol {
	public int id;//公会id
	public String name;//公会名称
	public String owner;//会长
	public int ownerLev;//会长等级
	public int ownerVocation;//会长职业
	public int lev;//等级
	public String notice;//公告
	public int totalFight;//总战力
	public int count;//成员数量
	public int maxCount;//最大成员数量
	public int levLimit;//等级限制
	public int fightLimit;//战斗力限制
	public boolean isLevLimit;//是否等级限制
	public boolean isFightLimit;//是否战斗力限制
	public int rank;//排名
	public boolean apply;//是否有申请加入公会
	public int ownerId;//会长id
	public int ownerFightValue;//会长战斗力


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.name = bb.getString();
		this.owner = bb.getString();
		this.ownerLev = bb.getInt();
		this.ownerVocation = bb.getInt();
		this.lev = bb.getInt();
		this.notice = bb.getString();
		this.totalFight = bb.getInt();
		this.count = bb.getInt();
		this.maxCount = bb.getInt();
		this.levLimit = bb.getInt();
		this.fightLimit = bb.getInt();
		this.isLevLimit = bb.getBoolean();
		this.isFightLimit = bb.getBoolean();
		this.rank = bb.getInt();
		this.apply = bb.getBoolean();
		this.ownerId = bb.getInt();
		this.ownerFightValue = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putString(this.name);
		bb.putString(this.owner);
		bb.putInt(this.ownerLev);
		bb.putInt(this.ownerVocation);
		bb.putInt(this.lev);
		bb.putString(this.notice);
		bb.putInt(this.totalFight);
		bb.putInt(this.count);
		bb.putInt(this.maxCount);
		bb.putInt(this.levLimit);
		bb.putInt(this.fightLimit);
		bb.putBoolean(this.isLevLimit);
		bb.putBoolean(this.isFightLimit);
		bb.putInt(this.rank);
		bb.putBoolean(this.apply);
		bb.putInt(this.ownerId);
		bb.putInt(this.ownerFightValue);
	}
}
