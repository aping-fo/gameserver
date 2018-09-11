package com.game.params;


//充值回应(工具自动生成，请勿手动修改！）
public class RechargeRespVO implements IProtocol {
	public float amount;//真实的金额,
	public int totalAmout;//实际添加的金额
	public String orderId;//订单号类型
	public String paymentType;//支付类型
	public String currentType;//货币类型
	public int rechargeCfgId;//充值配置表id


	public void decode(BufferBuilder bb) {
		this.amount = bb.getFloat();
		this.totalAmout = bb.getInt();
		this.orderId = bb.getString();
		this.paymentType = bb.getString();
		this.currentType = bb.getString();
		this.rechargeCfgId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putFloat(this.amount);
		bb.putInt(this.totalAmout);
		bb.putString(this.orderId);
		bb.putString(this.paymentType);
		bb.putString(this.currentType);
		bb.putInt(this.rechargeCfgId);
	}
}
