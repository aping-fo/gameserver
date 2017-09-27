package com.game.params.ladder;

import com.game.params.*;

//排位赛信息(工具自动生成，请勿手动修改！）
public class LadderVO implements IProtocol {
	public int score;//积分
	public int level;//等级
	public int honorPoint;//荣誉点
	public int winTimes;//胜场
	public int continuityWinTimes;//最高连胜
	public int fightTimes;//场次


	public void decode(BufferBuilder bb) {
		this.score = bb.getInt();
		this.level = bb.getInt();
		this.honorPoint = bb.getInt();
		this.winTimes = bb.getInt();
		this.continuityWinTimes = bb.getInt();
		this.fightTimes = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.score);
		bb.putInt(this.level);
		bb.putInt(this.honorPoint);
		bb.putInt(this.winTimes);
		bb.putInt(this.continuityWinTimes);
		bb.putInt(this.fightTimes);
	}
}
