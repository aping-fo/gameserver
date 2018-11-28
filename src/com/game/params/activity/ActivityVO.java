package com.game.params.activity;

import com.game.params.*;

//活动内容(工具自动生成，请勿手动修改！）
public class ActivityVO implements IProtocol {
	public int id;//活动id
	public String beginTime;//活动开始时间
	public String endTime;//活动结束时间


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.beginTime = bb.getString();
		this.endTime = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putString(this.beginTime);
		bb.putString(this.endTime);
	}
}
