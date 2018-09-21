package com.game.params.activity;

import java.util.List;
import java.util.ArrayList;
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
	public int param0;//额外参数
	public List<Int2Param> rewards;//额外参数
	public List<Reward> cardRewards;//奖励


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.activityId = bb.getInt();
		this.value = bb.getFloat();
		this.targetValue = bb.getFloat();
		this.state = bb.getInt();
		this.remainingTime = bb.getInt();
		this.finishNum = bb.getInt();
		this.param0 = bb.getInt();
		
        if (bb.getNullFlag())
            this.rewards = null;
        else {
            int length = bb.getInt();
            this.rewards = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.rewards.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.rewards.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.cardRewards = null;
        else {
            int length = bb.getInt();
            this.cardRewards = new ArrayList<Reward>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.cardRewards.add(null);
                }
                else
                {
                    Reward instance = new Reward();
                    instance.decode(bb);
                    this.cardRewards.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.activityId);
		bb.putFloat(this.value);
		bb.putFloat(this.targetValue);
		bb.putInt(this.state);
		bb.putInt(this.remainingTime);
		bb.putInt(this.finishNum);
		bb.putInt(this.param0);
		bb.putProtocolVoList(this.rewards);
		bb.putProtocolVoList(this.cardRewards);
	}
}
