package com.game.params.ladder;

import com.game.params.*;

//战报(工具自动生成，请勿手动修改！）
public class LadderRecordVO implements IProtocol {
	public String name;//对手名称
	public int type;//战报类型
	public int result;//战斗结果1胜利，0失败
	public int score;//积分变化
	public int rankId;//变化后段位Id


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.type = bb.getInt();
		this.result = bb.getInt();
		this.score = bb.getInt();
		this.rankId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.type);
		bb.putInt(this.result);
		bb.putInt(this.score);
		bb.putInt(this.rankId);
	}
}
