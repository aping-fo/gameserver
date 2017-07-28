package com.game.params.worldboss;

import com.game.params.*;

//世界boss进度(工具自动生成，请勿手动修改！）
public class WorldBossVO implements IProtocol {
	public int startTime;//开始时间
	public int killCount;//已击杀boss的数量
	public int bossKilledTime;//最终boss的击杀时间
	public String playerName;//击杀最终boss的玩家名字


	public void decode(BufferBuilder bb) {
		this.startTime = bb.getInt();
		this.killCount = bb.getInt();
		this.bossKilledTime = bb.getInt();
		this.playerName = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.startTime);
		bb.putInt(this.killCount);
		bb.putInt(this.bossKilledTime);
		bb.putString(this.playerName);
	}
}
