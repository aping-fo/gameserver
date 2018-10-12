package com.game.module.admin;

import java.util.Date;

public class ProhibitionEntity  {
    private Long serverId;

    private Integer closureType;

    private Integer closureWay;

    private String closureAccount;

    private Long closureTime;

    private Date endTime;

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Integer getClosureType() {
        return closureType;
    }

    public void setClosureType(Integer closureType) {
        this.closureType = closureType;
    }

    public Integer getClosureWay() {
        return closureWay;
    }

    public void setClosureWay(Integer closureWay) {
        this.closureWay = closureWay;
    }

    public String getClosureAccount() {
        return closureAccount;
    }

    public void setClosureAccount(String closureAccount) {
        this.closureAccount = closureAccount == null ? null : closureAccount.trim();
    }

    public Long getClosureTime() {
        return closureTime;
    }

    public void setClosureTime(Long closureTime) {
        this.closureTime = closureTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}