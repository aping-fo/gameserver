package com.game.params;

import java.util.List;

//包含1个List类型的vo(工具自动生成，请勿手动修改！）
public class ListParam<T extends IProtocol> implements IProtocol {
	public int code;//错误码
	public List<T> params;//list数据


	public void decode(BufferBuilder bb) {
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putProtocolVoList(this.params);
	}
}
