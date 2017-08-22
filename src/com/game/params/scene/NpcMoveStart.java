package com.game.params.scene;

import com.game.params.*;

//npc开始移动(工具自动生成，请勿手动修改！）
public class NpcMoveStart implements IProtocol {
	public int playerId;//玩家id
	public byte hMoveDir;//水平移动方向
	public byte vMoveDir;//垂直移动方向
	public float x;//当前位置x
	public float z;//当前位置z
	public int moveType;//0巡逻 1跟踪
	public int traceDirection;//跟踪方向
	public float hMovementUnit;//x移动速度
	public float vMovementUnit;//y移动速度


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.hMoveDir = bb.getByte();
		this.vMoveDir = bb.getByte();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.moveType = bb.getInt();
		this.traceDirection = bb.getInt();
		this.hMovementUnit = bb.getFloat();
		this.vMovementUnit = bb.getFloat();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putByte(this.hMoveDir);
		bb.putByte(this.vMoveDir);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.moveType);
		bb.putInt(this.traceDirection);
		bb.putFloat(this.hMovementUnit);
		bb.putFloat(this.vMovementUnit);
	}
}
