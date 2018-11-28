package com.game.params.scene;

import com.game.params.*;

//使用技能(工具自动生成，请勿手动修改！）
public class StartSkillVO implements IProtocol {
	public int skillId;//技能id
	public int skillCD;//技能cd
	public long startTime;//技能释放时间


	public void decode(BufferBuilder bb) {
		this.skillId = bb.getInt();
		this.skillCD = bb.getInt();
		this.startTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.skillId);
		bb.putInt(this.skillCD);
		bb.putLong(this.startTime);
	}
}
