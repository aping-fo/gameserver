package com.game.module.pet;

/**
 * Created by lucky on 2018/1/15.
 */
public class PetActivityData {
    /**
     * 该类型活动总次数
     */
    private int totalCount;
    /**
     * 当前等级
     */
    private int level;
    /**
     * 正在进行的活动个数
     */
    private int doingCount;

    /**
     * 最大次数
     */
    private int maxCount;

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDoingCount() {
        return doingCount;
    }

    public void setDoingCount(int doingCount) {
        this.doingCount = doingCount;
    }
}
