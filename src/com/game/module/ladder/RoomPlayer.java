package com.game.module.ladder;

/**
 * Created by lucky on 2017/9/14.
 */
public class RoomPlayer {
    private int hp;

    public RoomPlayer(int hp) {
        this.hp = hp;
    }

    public void decreaseHp(int hp) {
        this.hp -= hp;
    }

    public boolean checkDeath() {
        return hp <= 0;
    }
}
