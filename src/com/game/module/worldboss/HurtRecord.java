package com.game.module.worldboss;

/**
 * Created by lucky on 2017/7/4.
 */
public class HurtRecord {
    private int playerId;
    private String name;
    private int hurt;

    public HurtRecord() {
    }

    public HurtRecord(int playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.hurt = 0;
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
