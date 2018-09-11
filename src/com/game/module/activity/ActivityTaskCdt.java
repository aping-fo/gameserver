package com.game.module.activity;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityTaskCdt {
    private float value;
    private float targetValue;
    private int condType;

    public ActivityTaskCdt(float targetValue, int condType) {
        this.value = 0;
        this.targetValue = targetValue;
        this.condType = condType;
    }

    public ActivityTaskCdt() {
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(float targetValue) {
        this.targetValue = targetValue;
    }

    public boolean checkComplete() {
        if (condType == ActivityConsts.ActivityTaskCondType.T_TIMED_BAG) {
            return value == targetValue;
        }
        return value >= targetValue;
    }

    public int getCondType() {
        return condType;
    }

    public void setCondType(int condType) {
        this.condType = condType;
    }

    public void cleanup() {
        value = 0;
    }
}
