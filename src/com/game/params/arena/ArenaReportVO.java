package com.game.params.arena;

import com.game.params.*;

//竞技场战报对象(工具自动生成，请勿手动修改！）
public class ArenaReportVO implements IProtocol {
	public int id;//挑战类型ID
	public String name;//对手名字
	public int rank;//挑战后的排名


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.name = bb.getString();
		this.rank = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putString(this.name);
		bb.putInt(this.rank);
	}
}
