package com.game.params.arena;

import com.game.params.*;

//竞技场玩家对象(工具自动生成，请勿手动修改！）
public class ArenaPlayerVO implements IProtocol {
	public int rank;//排名
	public int uniqueId;//在竞技场中的唯一ID
	public String name;//玩家名称
	public int level;//等级
	public int fightingValue;//战力
	public int vocation;//职业
	public int fashionId;//时装ID
	public int weapon;//武器
	public int head;//头部
	public int id;//在竞技场中玩家ID


	public void decode(BufferBuilder bb) {
		this.rank = bb.getInt();
		this.uniqueId = bb.getInt();
		this.name = bb.getString();
		this.level = bb.getInt();
		this.fightingValue = bb.getInt();
		this.vocation = bb.getInt();
		this.fashionId = bb.getInt();
		this.weapon = bb.getInt();
		this.head = bb.getInt();
		this.id = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.rank);
		bb.putInt(this.uniqueId);
		bb.putString(this.name);
		bb.putInt(this.level);
		bb.putInt(this.fightingValue);
		bb.putInt(this.vocation);
		bb.putInt(this.fashionId);
		bb.putInt(this.weapon);
		bb.putInt(this.head);
		bb.putInt(this.id);
	}
}
