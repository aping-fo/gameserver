package com.game.params;


//商城购买回应(工具自动生成，请勿手动修改！）
public class BuyShopVO implements IProtocol {
	public int errCode;//错误码
	public int id;//商品ID
	public int count;//商品数量


	public void decode(BufferBuilder bb) {
		this.errCode = bb.getInt();
		this.id = bb.getInt();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.errCode);
		bb.putInt(this.id);
		bb.putInt(this.count);
	}
}
