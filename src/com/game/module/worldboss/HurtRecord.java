package com.game.module.worldboss;

/**
 * Created by lucky on 2017/7/4.
 */
public class HurtRecord {
    private int playerId;
    private String name;
    private int hurt;
    private int rank;

    private int curHurt;
    private int curBossId;

    public HurtRecord() {
    }

    public HurtRecord(int playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.hurt = 0;
    }

    public int getCurHurt() {
        return curHurt;
    }

    public void setCurHurt(int curHurt) {
        this.curHurt = curHurt;
    }

    public int getCurBossId() {
        return curBossId;
    }

    public void setCurBossId(int curBossId) {
        this.curBossId = curBossId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getHurt() {
        return hurt;
    }

    public void setHurt(int hurt) {
        this.hurt = hurt;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
