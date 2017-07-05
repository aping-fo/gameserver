package com.game.module.worldboss;

/**
 * Created by lucky on 2017/7/4.
 * 世界BOSS
 */
public class WorldBoss {
    /**
     * 配置ID
     */
    private int id;
    /**
     * 当前血量
     */
    private int hp;

    /**
     * BOSS难度等级
     */
    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
