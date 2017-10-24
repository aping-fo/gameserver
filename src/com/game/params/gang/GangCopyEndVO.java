package com.game.params.gang;

import com.game.params.*;

//公会副本结束信息(工具自动生成，请勿手动修改！）
public class GangCopyEndVO implements IProtocol {
	public int currentHurt;//当前伤害
	public int totalHurt;//总伤害
	public int gangContribution;//贡献
	public float progress;//进度
	public int rank;//排名


	public void decode(BufferBuilder bb) {
		this.currentHurt = bb.getInt();
		this.totalHurt = bb.getInt();
		this.gangContribution = bb.getInt();
		this.progress = bb.getFloat();
		this.rank = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.currentHurt);
		bb.putInt(this.totalHurt);
		bb.putInt(this.gangContribution);
		bb.putFloat(this.progress);
		bb.putInt(this.rank);
	}
}
