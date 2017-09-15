package com.game.params.rank;

import com.game.params.*;

//排位赛排行(工具自动生成，请勿手动修改！）
public class LadderRankVO implements IProtocol {
	public String name;//玩家名称
	public int level;//段位
	public int vocation;//职业
	public int score;//积分


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.level = bb.getInt();
		this.vocation = bb.getInt();
		this.score = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.level);
		bb.putInt(this.vocation);
		bb.putInt(this.score);
	}
}
