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
     * 当前血量，当前少于等于0，表示死亡
     */
    private int hp;

    private int curHp;
    /**
     * BOSS 索引,难度等级
     */
    private int index;

    private int level;

    /**
     * 副本id
     */
    private int copyId;

    public int getCopyId() {
        return copyId;
    }

    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    public int getCurHp() {
        return curHp;
    }

    public void setCurHp(int curHp) {
        this.curHp = curHp;
    }

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static WorldBoss newInstance(int bossId, int index, int hp, int copyId) {
        WorldBoss worldBoss = new WorldBoss();
        worldBoss.curHp = hp;
        worldBoss.hp = hp;
        worldBoss.id = bossId;
        worldBoss.index = index;
        worldBoss.copyId = copyId;
        return worldBoss;
    }
}
