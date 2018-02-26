package com.game.module.serial;

/**
 * Created by lucky on 2018/1/27.
 */
public class PlayerView {
    private int ladderMaxRank;
    private int worldBossMaxRank;
    private int fightMaxRank;
    private int gangMaxRank;
    private int gangMaxTech;
    private int gangMaxLevel;
    private int achievementMaxRank;
    //公会副本挑战层数
    private int guildLayer;

    public int getGuildLayer() {
        return guildLayer;
    }

    public void setGuildLayer(int guildLayer) {
        this.guildLayer = guildLayer;
    }

    public int getAchievementMaxRank() {
        return achievementMaxRank;
    }

    public void setAchievementMaxRank(int achievementMaxRank) {
        this.achievementMaxRank = achievementMaxRank;
    }

    public int getLadderMaxRank() {
        return ladderMaxRank;
    }

    public void setLadderMaxRank(int ladderMaxRank) {
        this.ladderMaxRank = ladderMaxRank;
    }

    public int getWorldBossMaxRank() {
        return worldBossMaxRank;
    }

    public void setWorldBossMaxRank(int worldBossMaxRank) {
        this.worldBossMaxRank = worldBossMaxRank;
    }

    public int getFightMaxRank() {
        return fightMaxRank;
    }

    public void setFightMaxRank(int fightMaxRank) {
        this.fightMaxRank = fightMaxRank;
    }

    public int getGangMaxRank() {
        return gangMaxRank;
    }

    public void setGangMaxRank(int gangMaxRank) {
        this.gangMaxRank = gangMaxRank;
    }

    public int getGangMaxTech() {
        return gangMaxTech;
    }

    public void setGangMaxTech(int gangMaxTech) {
        this.gangMaxTech = gangMaxTech;
    }

    public int getGangMaxLevel() {
        return gangMaxLevel;
    }

    public void setGangMaxLevel(int gangMaxLevel) {
        this.gangMaxLevel = gangMaxLevel;
    }
}
