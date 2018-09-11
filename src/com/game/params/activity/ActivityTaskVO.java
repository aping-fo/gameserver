package com.game.params.activity;

import com.game.params.*;

//活动任务(工具自动生成，请勿手动修改！）
public class ActivityTaskVO implements IProtocol {
	public int id;//任务id
	public int activityId;//活动id
	public float value;//当前值
	public float targetValue;//目标值
	public int state;//当前状态，1：未完成，2：已完成未领奖，3：已领奖，4：补领
	public int remainingTime;//剩余时间
	public int finishNum;//完成次数


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.activityId = bb.getInt();
		this.value = bb.getFloat();
		this.targetValue = bb.getFloat();
		this.state = bb.getInt();
		this.remainingTime = bb.getInt();
		this.finishNum = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.activityId);
		bb.putFloat(this.value);
		bb.putFloat(this.targetValue);
		bb.putInt(this.state);
		bb.putInt(this.remainingTime);
		bb.putInt(this.finishNum);
	}
}
