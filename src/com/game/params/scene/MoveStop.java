package com.game.params.scene;

import com.game.params.*;

//停止移动(工具自动生成，请勿手动修改！）
public class MoveStop implements IProtocol {
	public int playerId;//玩家id
	public float x;//客户端当前位置x
	public float z;//客户端当前位置z


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
	}
}
