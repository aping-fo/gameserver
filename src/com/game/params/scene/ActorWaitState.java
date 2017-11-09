package com.game.params.scene;

import com.game.params.*;

//待机位置状态(工具自动生成，请勿手动修改！）
public class ActorWaitState implements IProtocol {
	public int actorId;//id
	public int posX;//位置x
	public int posZ;//位置z
	public byte faceDir;//朝向
	public int hp;//当前血量


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.posX = bb.getInt();
		this.posZ = bb.getInt();
		this.faceDir = bb.getByte();
		this.hp = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.posX);
		bb.putInt(this.posZ);
		bb.putByte(this.faceDir);
		bb.putInt(this.hp);
	}
}
