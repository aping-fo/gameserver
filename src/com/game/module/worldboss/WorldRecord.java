package com.game.module.worldboss;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lucky on 2017/7/4.
 * 世界boss战场信息
 */
public class WorldRecord {
    private int id;
    /**
     * 玩家id - 伤害
     */
    private Map<Integer, HurtRecord> hurtMap = new ConcurrentHashMap<>();
    /**
     * 最后一击,bossId -- 玩家ID
     */
    private Map<Integer,Integer> killMap = new ConcurrentHashMap<>();

    private Map<Integer,Set<Integer>> killBossMap = new ConcurrentHashMap<>();
    private int day;


    //发奖标记
    private int rewardFlag;

    public Map<Integer, Set<Integer>> getKillBossMap() {
        return killBossMap;
    }

    public void setKillBossMap(Map<Integer, Set<Integer>> killBossMap) {
        this.killBossMap = killBossMap;
    }

    public int getRewardFlag() {
        return rewardFlag;
    }

    public void setRewardFlag(int rewardFlag) {
        this.rewardFlag = rewardFlag;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Integer, HurtRecord> getHurtMap() {
        return hurtMap;
    }

    public void setHurtMap(Map<Integer, HurtRecord> hurtMap) {
        this.hurtMap = hurtMap;
    }

    public Map<Integer, Integer> getKillMap() {
        return killMap;
    }

    public void setKillMap(Map<Integer, Integer> killMap) {
        this.killMap = killMap;
    }
}
