package com.game.params.scene;

import com.game.params.*;

//技能伤害(工具自动生成，请勿手动修改！）
public class SkillHurtVO implements IProtocol {
	public int attackType;//攻击者类型 0玩家 1怪物
	public int attackId;//攻击者id
	public int targetType;//目标类型 0玩家 1怪物
	public int targetId;//受伤者id（怪物，是唯一id;玩家，是playerId)
	public int skillId;//技能id
	public float x;//释放技能时的x位置
	public float z;//释放技能时的z位置
	public int faceDir;//释放技能时的朝向
	public int hurtValue;//伤害
	public boolean isCrit;//是否是暴击
	public int subType;//自定义子类型，1：增加HP上限


	public void decode(BufferBuilder bb) {
		this.attackType = bb.getInt();
		this.attackId = bb.getInt();
		this.targetType = bb.getInt();
		this.targetId = bb.getInt();
		this.skillId = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.faceDir = bb.getInt();
		this.hurtValue = bb.getInt();
		this.isCrit = bb.getBoolean();
		this.subType = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.attackType);
		bb.putInt(this.attackId);
		bb.putInt(this.targetType);
		bb.putInt(this.targetId);
		bb.putInt(this.skillId);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.faceDir);
		bb.putInt(this.hurtValue);
		bb.putBoolean(this.isCrit);
		bb.putInt(this.subType);
	}
}
