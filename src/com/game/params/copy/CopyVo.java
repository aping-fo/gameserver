package com.game.params.copy;

import com.game.params.*;

//单个副本信息(工具自动生成，请勿手动修改！）
public class CopyVo implements IProtocol {
	public int copyId;//副本id
	public short state;//状态(0未通关,其他的是星级)
	public short count;//通关次数
	public short reset;//重置次数
	public short buyTimes;//购买次数


	public void decode(BufferBuilder bb) {
		this.copyId = bb.getInt();
		this.state = bb.getShort();
		this.count = bb.getShort();
		this.reset = bb.getShort();
		this.buyTimes = bb.getShort();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.copyId);
		bb.putShort(this.state);
		bb.putShort(this.count);
		bb.putShort(this.reset);
		bb.putShort(this.buyTimes);
	}
}
