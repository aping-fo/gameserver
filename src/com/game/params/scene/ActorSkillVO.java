package com.game.params.scene;

import com.game.params.*;

//技能状态(工具自动生成，请勿手动修改！）
public class ActorSkillVO implements IProtocol {
	public int actorId;//id
	public int skillId;//技能id
	public float x;//当前位置x
	public float z;//当前位置z
	public byte faceDir;//朝向
	public byte skillState;//技能状态


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.skillId = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.faceDir = bb.getByte();
		this.skillState = bb.getByte();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.skillId);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putByte(this.faceDir);
		bb.putByte(this.skillState);
	}
}
