package com.game.params.scene;

import com.game.params.*;

//怪物死亡信息(工具自动生成，请勿手动修改！）
public class CMonster implements IProtocol {
	public int id;//唯一id
	public int reward;//是否需要奖励
	public float x;//x
	public float z;//z
	public int percent;//配置的百分比
	public int targetId;//攻击目标
	public int killerId;//被杀时的玩家id
	public float killerX;//被杀时的玩家位置x
	public float killerZ;//被杀时的玩家位置z
	public int hp;//怪物血量
	public int hurt;//怪物受到的总伤害
	public int hightHurt;//怪物受到的单次最大伤害


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.reward = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.percent = bb.getInt();
		this.targetId = bb.getInt();
		this.killerId = bb.getInt();
		this.killerX = bb.getFloat();
		this.killerZ = bb.getFloat();
		this.hp = bb.getInt();
		this.hurt = bb.getInt();
		this.hightHurt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.reward);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.percent);
		bb.putInt(this.targetId);
		bb.putInt(this.killerId);
		bb.putFloat(this.killerX);
		bb.putFloat(this.killerZ);
		bb.putInt(this.hp);
		bb.putInt(this.hurt);
		bb.putInt(this.hightHurt);
	}
}
