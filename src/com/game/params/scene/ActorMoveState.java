package com.game.params.scene;

import com.game.params.*;

//位置状态(工具自动生成，请勿手动修改！）
public class ActorMoveState implements IProtocol {
	public int actorId;//id
	public int posX;//位置x
	public int posZ;//位置z
	public int faceDir;//朝向


	public void decode(BufferBuilder bb) {
		this.actorId = bb.getInt();
		this.posX = bb.getInt();
		this.posZ = bb.getInt();
		this.faceDir = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.actorId);
		bb.putInt(this.posX);
		bb.putInt(this.posZ);
		bb.putInt(this.faceDir);
	}
}
