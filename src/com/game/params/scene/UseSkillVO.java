package com.game.params.scene;

import com.game.params.*;

//使用技能(工具自动生成，请勿手动修改！）
public class UseSkillVO implements IProtocol {
	public int type;//攻击者类型 0玩家 1怪物
	public int attackId;//攻击者id
	public int skillId;//技能id
	public float x;//x
	public float z;//z
	public int faceDir;//释放技能时的朝向


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.attackId = bb.getInt();
		this.skillId = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.faceDir = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.attackId);
		bb.putInt(this.skillId);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.faceDir);
	}
}
