package com.game.params.gang;

import com.game.params.*;

//公会练功信息(工具自动生成，请勿手动修改！）
public class GTrainingVO implements IProtocol {
	public int id;//当前开放的练功房ID
	public long createTime;//本练功房开始时间
	public long startTime;//本次开始练功时间
	public int trainingTime;//已训练的时间(小时)
	public int max;//该练功房参与人数


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.createTime = bb.getLong();
		this.startTime = bb.getLong();
		this.trainingTime = bb.getInt();
		this.max = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putLong(this.createTime);
		bb.putLong(this.startTime);
		bb.putInt(this.trainingTime);
		bb.putInt(this.max);
	}
}
