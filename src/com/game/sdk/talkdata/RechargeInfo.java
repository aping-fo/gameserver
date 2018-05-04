package com.game.sdk.talkdata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lucky on 2018/4/18.
 */
public class RechargeInfo {
    private String msgID;
    private String status;
    private String OS;
    private String accountID;
    private String orderID;
    private double currencyAmount;
    private String currencyType;
    private double virtualCurrencyAmount;
    private long chargeTime;
    private String iapID;
    private String gameServer;
    private String gameVersion;
    private int level;
    private String mission;
    private String partner;
    private String paymentType;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty(value = "OS")
    public String getOS() {
        return OS;
    }

    public void setOS(String OS) {
        this.OS = OS;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public double getCurrencyAmount() {
        return currencyAmount;
    }

    public void setCurrencyAmount(double currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public double getVirtualCurrencyAmount() {
        return virtualCurrencyAmount;
    }

    public void setVirtualCurrencyAmount(double virtualCurrencyAmount) {
        this.virtualCurrencyAmount = virtualCurrencyAmount;
    }

    public long getChargeTime() {
        return chargeTime;
    }

    public void setChargeTime(long chargeTime) {
        this.chargeTime = chargeTime;
    }

    public String getIapID() {
        return iapID;
    }

    public void setIapID(String iapID) {
        this.iapID = iapID;
    }

    public String getGameServer() {
        return gameServer;
    }

    public void setGameServer(String gameServer) {
        this.gameServer = gameServer;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}
