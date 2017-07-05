package com.game.params.goods;

import com.game.params.*;

//道具(工具自动生成，请勿手动修改！）
public class CTool implements IProtocol {
	public long id;//唯一id
	public int count;//数量


	public void decode(BufferBuilder bb) {
		this.id = bb.getLong();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putLong(this.id);
		bb.putInt(this.count);
	}
}
