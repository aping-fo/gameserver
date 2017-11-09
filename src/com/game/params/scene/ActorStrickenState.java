package com.game.params.scene;

import com.game.params.*;

//受击位置状态(工具自动生成，请勿手动修改！）
public class ActorStrickenState implements IProtocol {
	public int actorId;//id
	public float x;//当前位置x
	public float z;//当前位置z
	public int hp;//当前血量


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.hp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.hp);
	}
}
