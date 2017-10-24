package com.game.module.gang;

/**
 * Created by lucky on 2017/10/10.
 */
public class GangHurt {
    private int playerId;
    private String name;
    private int hurt;
    private int vip;
    private int level;
    private int vocation;// 职业

    public GangHurt() {
    }

    public GangHurt(int playerId, String name,int vocation) {
        this.playerId = playerId;
        this.name = name;
        this.hurt = 0;
        this.vocation = vocation;
    }

    public int getVocation() {
        return vocation;
    }

    public void setVocation(int vocation) {
        this.vocation = vocation;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public int getHurt() {
        return hurt;
    }

    public void setHurt(int hurt) {
        this.hurt = hurt;
    }
}
