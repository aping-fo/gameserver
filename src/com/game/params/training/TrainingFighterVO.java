package com.game.params.training;

import com.game.params.*;

//英雄试炼对战玩家信息(工具自动生成，请勿手动修改！）
public class TrainingFighterVO implements IProtocol {
	public int code;//错误码
	public int index;//当前关卡索引,从0开始
	public int attack;//威力
	public int defense;//坚韧
	public int crit;//精准
	public int symptom;//症状
	public int fu;//符能
	public int hp;//血量
	public String name;//玩家名称
	public int level;//等级
	public int vocation;//职业


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.index = bb.getInt();
		this.attack = bb.getInt();
		this.defense = bb.getInt();
		this.crit = bb.getInt();
		this.symptom = bb.getInt();
		this.fu = bb.getInt();
		this.hp = bb.getInt();
		this.name = bb.getString();
		this.level = bb.getInt();
		this.vocation = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.index);
		bb.putInt(this.attack);
		bb.putInt(this.defense);
		bb.putInt(this.crit);
		bb.putInt(this.symptom);
		bb.putInt(this.fu);
		bb.putInt(this.hp);
		bb.putString(this.name);
		bb.putInt(this.level);
		bb.putInt(this.vocation);
	}
}
