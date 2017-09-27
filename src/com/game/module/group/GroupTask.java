package com.game.module.group;

import com.game.params.group.GroupTaskVO;

/**
 * Created by lucky on 2017/9/14.
 */
public class GroupTask {
    private int type;
    private int count;
    private int target;
    private int value;
    private int param;
    public GroupTask(int type, int count,int target,int param) {
        this.type = type;
        this.count = count;
        this.target = target;
        this.param = param;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public GroupTaskVO toProto() {
        GroupTaskVO vo = new GroupTaskVO();
        vo.count = value;
        vo.id = type;
        vo.copyId = target;
        return vo;
    }
}
