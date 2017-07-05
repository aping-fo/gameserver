package com.game.params.skill;

import com.game.params.*;

//技能卡(工具自动生成，请勿手动修改！）
public class SkillCardVo implements IProtocol {
	public int id;//自增长id
	public int cardId;//卡id
	public int exp;//经验
	public int lev;//等级


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.cardId = bb.getInt();
		this.exp = bb.getInt();
		this.lev = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.cardId);
		bb.putInt(this.exp);
		bb.putInt(this.lev);
	}
}
