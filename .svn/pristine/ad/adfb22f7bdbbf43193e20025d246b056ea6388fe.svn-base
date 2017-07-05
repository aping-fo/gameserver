package com.game.params;


//无尽漩涡信息(工具自动生成，请勿手动修改！）
public class EndlessInfo implements IProtocol {
	public int currLayer;//当前层次
	public int maxLayer;//历史最高层次
	public int challenge;//剩余挑战次数
	public int refresh;//可重置次数
	public long clearTime;//扫荡时间(ms),0表示没有进行扫荡


	public void decode(BufferBuilder bb) {
		this.currLayer = bb.getInt();
		this.maxLayer = bb.getInt();
		this.challenge = bb.getInt();
		this.refresh = bb.getInt();
		this.clearTime = bb.getLong();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.currLayer);
		bb.putInt(this.maxLayer);
		bb.putInt(this.challenge);
		bb.putInt(this.refresh);
		bb.putLong(this.clearTime);
	}
}
