package com.game.module.activity;

import com.game.data.ActivityCfg;
import com.game.params.Reward;
import com.game.params.activity.ActivityTaskVO;
import com.game.util.ConfigData;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityTask {
    private int id;
    private boolean rewardFlag;
    private int activityId;
    private int state;
    private ActivityTaskCdt cond;
    private long timedBag;//限时礼包时间
    private int finishNum;//完成次数或者购买次数
    private int param0;//额外参数
    private List<Reward> rewards;

    public ActivityTask() {
    }

    public ActivityTask(int id, int activityId, float targetValue, int condType) {
        this.id = id;
        this.activityId = activityId;
        this.state = ActivityConsts.ActivityState.T_UN_FINISH;
        this.finishNum = 0;
        this.param0 = 0;
        cond = new ActivityTaskCdt(targetValue, condType);
        timedBag = System.currentTimeMillis();
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public int getParam0() {
        return param0;
    }

    public void setParam0(int param0) {
        this.param0 = param0;
    }

    public long getTimedBag() {
        return timedBag;
    }

    public void setTimedBag(long timedBag) {
        this.timedBag = timedBag;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public boolean isRewardFlag() {
        return rewardFlag;
    }

    public void setRewardFlag(boolean rewardFlag) {
        this.rewardFlag = rewardFlag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void cleanup() {
        rewardFlag = false;
        state = ActivityConsts.ActivityState.T_UN_FINISH;
        cond.cleanup();
    }

    public ActivityTaskCdt getCond() {
        return cond;
    }

    public void setCond(ActivityTaskCdt cond) {
        this.cond = cond;
    }

    public ActivityTaskVO toProto() {
        ActivityTaskVO vo = new ActivityTaskVO();
        vo.activityId = activityId;
        vo.id = id;
        vo.targetValue = cond.getTargetValue();
        vo.value = cond.getValue();
        vo.state = state;
        vo.finishNum = finishNum;
        vo.param0 = param0;

        vo.cardRewards = Lists.newArrayList();
        if (rewards != null && !rewards.isEmpty()) {
            vo.cardRewards = rewards;
        }

        //剩余时间
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, activityId);
        if (config != null && config.ActivityType == ActivityConsts.ActivityType.T_TIMED_BAG) {
            long l = config.Param0 * 60 - (System.currentTimeMillis() - timedBag) / 1000;//剩余时间（秒）
            if (l < 0) {
                l = 0;
            }
            vo.remainingTime = (int) l;
        }
        return vo;
    }
}
