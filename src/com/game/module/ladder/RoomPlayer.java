package com.game.module.ladder;

/**
 * Created by lucky on 2017/9/14.
 */
public class RoomPlayer {
    private int hp;
    private int totalHp;
    private final int playerId;
    private String name;

    public RoomPlayer(int hp, int playerId) {
        this.hp = hp;
        this.totalHp = hp;
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void decreaseHp(int hp) {
        this.hp -= hp;
        if (hp > totalHp) {
            hp = totalHp;
        }
    }

    public boolean checkDeath() {
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getTotalHp() {
        return totalHp;
    }

    public void setTotalHp(int totalHp) {
        this.totalHp = totalHp;
    }
}
