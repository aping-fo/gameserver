package com.game.params.gang;

import com.game.params.*;

//公会副本信息(工具自动生成，请勿手动修改！）
public class GangCopyVO implements IProtocol {
	public int errCode;//错误码
	public int layer;//当前第几层
	public int remainTimes;//剩余挑战次数
	public float progress;//进度
	public int hasOpen;//是否开启 0：未开启，1开启
	public int playerId;//战斗玩家ID


	public void decode(BufferBuilder bb) {
		this.errCode = bb.getInt();
		this.layer = bb.getInt();
		this.remainTimes = bb.getInt();
		this.progress = bb.getFloat();
		this.hasOpen = bb.getInt();
		this.playerId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.errCode);
		bb.putInt(this.layer);
		bb.putInt(this.remainTimes);
		bb.putFloat(this.progress);
		bb.putInt(this.hasOpen);
		bb.putInt(this.playerId);
	}
}
