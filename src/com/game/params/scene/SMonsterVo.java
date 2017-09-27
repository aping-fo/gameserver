package com.game.params.scene;

import com.game.params.*;

//怪物信息(工具自动生成，请勿手动修改！）
public class SMonsterVo implements IProtocol {
	public int id;//唯一id
	public int monsterId;//怪物id
	public int hp;//总血量
	public int curHp;//当前血量
	public int wave;//第几波，从0开始
	public int attack;//攻击
	public int defense;//防守
	public int crit;//暴击
	public int symptom;//症状
	public int fu;//符能


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.monsterId = bb.getInt();
		this.hp = bb.getInt();
		this.curHp = bb.getInt();
		this.wave = bb.getInt();
		this.attack = bb.getInt();
		this.defense = bb.getInt();
		this.crit = bb.getInt();
		this.symptom = bb.getInt();
		this.fu = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.monsterId);
		bb.putInt(this.hp);
		bb.putInt(this.curHp);
		bb.putInt(this.wave);
		bb.putInt(this.attack);
		bb.putInt(this.defense);
		bb.putInt(this.crit);
		bb.putInt(this.symptom);
		bb.putInt(this.fu);
	}
}
