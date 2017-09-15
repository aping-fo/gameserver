package com.game.params.group;

import com.game.params.*;

//团队副本信息(工具自动生成，请勿手动修改！）
public class SelfGroupCopyVO implements IProtocol {
	public int times;//团队副本次数


	public void decode(BufferBuilder bb) {
		this.times = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.times);
	}
}
