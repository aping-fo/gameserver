package com.game.params;


//穿，换时装(工具自动生成，请勿手动修改！）
public class TakeFashionVO implements IProtocol {
	public int type;//时装类型
	public int fashionId;//时装ID
	public int errCode;//操作码


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.fashionId = bb.getInt();
		this.errCode = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putInt(this.fashionId);
		bb.putInt(this.errCode);
	}
}
