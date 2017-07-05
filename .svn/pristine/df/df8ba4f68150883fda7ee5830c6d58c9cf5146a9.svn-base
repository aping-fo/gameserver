package com.game.params;

import java.util.List;

//多个Long(工具自动生成，请勿手动修改！）
public class LongList implements IProtocol {
	public List<Long> lList;//多个long


	public void decode(BufferBuilder bb) {
		this.lList = bb.getLongList();
	}

	public void encode(BufferBuilder bb) {
		bb.putLongList(this.lList);
	}
}
