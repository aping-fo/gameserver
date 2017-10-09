package com.game.module.ladder;

/**
 * Created by lucky on 2017/9/14.
 */
public class RoomPlayer {
    private int hp;
    private int teamId;
    private final int playerId;

    public RoomPlayer(int hp, int playerId) {
        this.hp = hp;
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void decreaseHp(int hp) {
        this.hp -= hp;
    }

    public boolean checkDeath() {
        return hp <= 0;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getHp() {
        return hp;
    }
}
