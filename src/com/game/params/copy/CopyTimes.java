package com.game.params.copy;

import com.game.params.BufferBuilder;
import com.game.params.IProtocol;

//副本通关次数(工具自动生成，请勿手动修改！）
public class CopyTimes implements IProtocol {
	public int id;//副本id
	public int count;//通关次数


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.count);
	}
}
