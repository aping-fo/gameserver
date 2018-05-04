package com.game.module.activity;

import com.game.data.ActivityCfg;
import com.game.params.activity.ActivityTaskVO;
import com.game.util.ConfigData;

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

    public ActivityTask() {
    }

    public ActivityTask(int id, int activityId, int targetValue, int condType) {
        this.id = id;
        this.activityId = activityId;
        this.state = ActivityConsts.ActivityState.T_UN_FINISH;
        cond = new ActivityTaskCdt(targetValue, condType);
        timedBag = System.currentTimeMillis();
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

        //剩余时间
        ActivityCfg config = ConfigData.getConfig(ActivityCfg.class, activityId);
        if(config!=null&&config.ActivityType==ActivityConsts.ActivityType.T_TIMED_BAG){
            long l = config.Param0 * 60 - (System.currentTimeMillis() - timedBag) / 1000;//剩余时间（秒）
            if(l<0){
                l=0;
            }
            vo.remainingTime= (int) l;
        }
        return vo;
    }
}
