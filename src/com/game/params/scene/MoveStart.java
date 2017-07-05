package com.game.params.scene;

import com.game.params.*;

//开始移动(工具自动生成，请勿手动修改！）
public class MoveStart implements IProtocol {
	public int playerId;//玩家id
	public byte hMoveDir;//水平移动方向
	public byte vMoveDir;//垂直移动方向
	public float x;//当前位置x
	public float z;//当前位置z
	public boolean isSkillMoving;//是否是技能触发的位移


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.hMoveDir = bb.getByte();
		this.vMoveDir = bb.getByte();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.isSkillMoving = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putByte(this.hMoveDir);
		bb.putByte(this.vMoveDir);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putBoolean(this.isSkillMoving);
	}
}
