package com.game.module.activity;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityTaskCdt {
    private int value;
    private int targetValue;
    private int condType;

    public ActivityTaskCdt(int targetValue, int condType) {
        this.value = 0;
        this.targetValue = targetValue;
        this.condType = condType;
    }

    public ActivityTaskCdt() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
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
