package com.game.params.ladder;

import com.game.params.*;

//排位赛信息(工具自动生成，请勿手动修改！）
public class LadderVO implements IProtocol {
	public int score;//积分
	public int level;//等级


	public void decode(BufferBuilder bb) {
		this.score = bb.getInt();
		this.level = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.score);
		bb.putInt(this.level);
	}
}
