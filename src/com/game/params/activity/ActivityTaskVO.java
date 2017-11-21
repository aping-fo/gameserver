package com.game.params.activity;

import com.game.params.*;

//活动任务(工具自动生成，请勿手动修改！）
public class ActivityTaskVO implements IProtocol {
	public int id;//任务id
	public int activityId;//活动id
	public int value;//当前值
	public int targetValue;//目标值
	public int state;//当前状态，1：未完成，2：已完成未领奖，3：已领奖，4：补领


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.activityId = bb.getInt();
		this.value = bb.getInt();
		this.targetValue = bb.getInt();
		this.state = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.activityId);
		bb.putInt(this.value);
		bb.putInt(this.targetValue);
		bb.putInt(this.state);
	}
}
