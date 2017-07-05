package com.game.params.arena;

import com.game.params.*;

//竞技场玩家对象(工具自动生成，请勿手动修改！）
public class ArenaVO implements IProtocol {
	public int rank;//排名
	public int uniqueId;//在竞技场中的唯一ID
	public int challenge;//剩余挑战次数
	public int buyTime;//购买次数
	public int record;//战绩，大于0表示连胜，小于0表示连败


	public void decode(BufferBuilder bb) {
		this.rank = bb.getInt();
		this.uniqueId = bb.getInt();
		this.challenge = bb.getInt();
		this.buyTime = bb.getInt();
		this.record = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.rank);
		bb.putInt(this.uniqueId);
		bb.putInt(this.challenge);
		bb.putInt(this.buyTime);
		bb.putInt(this.record);
	}
}
