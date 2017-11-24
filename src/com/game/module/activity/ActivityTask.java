package com.game.module.activity;

import com.game.params.activity.ActivityTaskVO;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityTask {
    private int id;
    private int resetType;
    private boolean rewardFlag;
    private int activityId;
    private int state;
    private ActivityTaskCdt cond;

    public ActivityTask() {
    }

    public ActivityTask(int id, int resetType, int activityId, int targetCount, int condType) {
        this.id = id;
        this.resetType = resetType;
        this.activityId = activityId;
        this.state = ActivityConsts.ActivityState.T_UN_FINISH;
        cond = new ActivityTaskCdt(targetCount, condType);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getResetType() {
        return resetType;
    }

    public void setResetType(int resetType) {
        this.resetType = resetType;
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
        return vo;
    }
}
