package com.game.params.goods;

import com.game.params.*;

//属性(工具自动生成，请勿手动修改！）
public class AttrItem implements IProtocol {
	public int type;//类型
	public int value;//值


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.value = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.value);
	}
}
