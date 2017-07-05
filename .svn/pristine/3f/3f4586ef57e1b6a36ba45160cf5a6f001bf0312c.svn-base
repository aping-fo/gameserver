package com.game.params;


//奖励(工具自动生成，请勿手动修改！）
public class Reward implements IProtocol {
	public int id;//物品id
	public int count;//数量


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.count);
	}
}
