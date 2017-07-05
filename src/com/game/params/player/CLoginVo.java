package com.game.params.player;

import com.game.params.*;

//角色登录信息(前端发送)(工具自动生成，请勿手动修改！）
public class CLoginVo implements IProtocol {
	public int playerId;//角色id
	public int version;//版本信息(某些平台用于限制低版本登录)


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.version = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putInt(this.version);
	}
}
