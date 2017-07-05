package com.game.params;

import java.util.List;

//多个int(工具自动生成，请勿手动修改！）
public class IntList implements IProtocol {
	public int code;//错误码
	public List<Integer> iList;//多个int


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.iList = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putIntList(this.iList);
	}
}
