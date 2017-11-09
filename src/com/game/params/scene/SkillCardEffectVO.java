package com.game.params.scene;

import com.game.params.*;

//技能卡效果状态(工具自动生成，请勿手动修改！）
public class SkillCardEffectVO implements IProtocol {
	public int actorId;//id
	public int attackerId;//id
	public int skillCardId;//技能卡id
	public float punishTime;//触发时间


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.attackerId = bb.getInt();
		this.skillCardId = bb.getInt();
		this.punishTime = bb.getFloat();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.attackerId);
		bb.putInt(this.skillCardId);
		bb.putFloat(this.punishTime);
	}
}
