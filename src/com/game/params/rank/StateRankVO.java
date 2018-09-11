package com.game.params.rank;

import com.game.params.*;

//第一排行(工具自动生成，请勿手动修改！）
public class StateRankVO implements IProtocol {
	public String name;//玩家名称
	public int vocation;//职业
	public int playerId;//玩家ID
	public int head;//时装头部
	public int fashionId;//时装衣服
	public int weapon;//时装武器
	public int level;//等级
	public String gang;//公会
	public int fightingValue;//战力
	public int vip;//vip
	public int title;//称号
	public int rankType;//排行榜类型1、战力，2、等级，3、排位赛


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.vocation = bb.getInt();
		this.playerId = bb.getInt();
		this.head = bb.getInt();
		this.fashionId = bb.getInt();
		this.weapon = bb.getInt();
		this.level = bb.getInt();
		this.gang = bb.getString();
		this.fightingValue = bb.getInt();
		this.vip = bb.getInt();
		this.title = bb.getInt();
		this.rankType = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.vocation);
		bb.putInt(this.playerId);
		bb.putInt(this.head);
		bb.putInt(this.fashionId);
		bb.putInt(this.weapon);
		bb.putInt(this.level);
		bb.putString(this.gang);
		bb.putInt(this.fightingValue);
		bb.putInt(this.vip);
		bb.putInt(this.title);
		bb.putInt(this.rankType);
	}
}
