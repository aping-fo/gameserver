package com.game.module.title;

/**
 * Created by lucky on 2017/12/26.
 * 称号
 */
public class Title {
    private int id;
    private int condType; //条件类型
    private int value; //当前值
    private boolean openFlag;
    private long recvTime; //获取时间

    public long getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(long recvTime) {
        this.recvTime = recvTime;
    }

    public boolean isOpenFlag() {
        return openFlag;
    }

    public void setOpenFlag(boolean openFlag) {
        this.openFlag = openFlag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCondType() {
        return condType;
    }

    public void setCondType(int condType) {
        this.condType = condType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
