package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;

/**
 * Created by lucky on 2018/2/5.
 * 增值道具消费
 */

public class MoneyData extends Header {
    @NodeName(name = "detail_id")
    private long detailId;
    @NodeName(name = "user_id")
    private int userId;
    @NodeName(name = "role_id")
    private int roleId;
    @NodeName(name = "subject_id")
    private int subjectId;
    @NodeName(name = "amount")
    private int amount;
    @NodeName(name = "add_time")
    private long addTime;
    @NodeName(name = "source")
    private String source;


    public MoneyData(int command_id) {
        super(command_id);
    }

    public long getDetailId() {
        return detailId;
    }

    public void setDetailId(long detailId) {
        this.detailId = detailId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getAmount() {
        return amount;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
