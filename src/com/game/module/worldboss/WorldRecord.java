package com.game.module.worldboss;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * 也表示该boss死亡
     */
    private Map<Integer, Integer> killMap = new ConcurrentHashMap<>();

    private Map<Integer, WorldBoss> worldBossMap = new ConcurrentHashMap<>();
    //玩家死亡时间，CD用
    private Map<Integer, Long> playerDeadTime = new ConcurrentHashMap<>();
    //攻击购买次数
    private Map<Integer, Integer> buyTimes = new ConcurrentHashMap<>();
    private long startTime; //开始时间
    private int day;
    //发奖标记
    private int rewardFlag;
    //击杀最后一个BOSS 玩家
    private int lastKillPlayerId;
    //最后一个boss杀死时间
    private long lastKillTime;
    //最后BOSS ID
    private int lastBossId;

    @JsonIgnore
    private AtomicBoolean bUpdate = new AtomicBoolean(false);

    public AtomicBoolean getbUpdate() {
        return bUpdate;
    }

    public void setbUpdate(boolean bUpdate) {
        this.bUpdate.compareAndSet(this.bUpdate.get(),bUpdate);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastKillTime() {
        return lastKillTime;
    }

    public void setLastKillTime(long lastKillTime) {
        this.lastKillTime = lastKillTime;
    }

    public Map<Integer, Long> getPlayerDeadTime() {
        return playerDeadTime;
    }

    public void setPlayerDeadTime(Map<Integer, Long> playerDeadTime) {
        this.playerDeadTime = playerDeadTime;
    }

    public Map<Integer, Integer> getBuyTimes() {
        return buyTimes;
    }

    public void setBuyTimes(Map<Integer, Integer> buyTimes) {
        this.buyTimes = buyTimes;
    }

    public int getLastBossId() {
        return lastBossId;
    }

    public void setLastBossId(int lastBossId) {
        this.lastBossId = lastBossId;
    }

    public int getLastKillPlayerId() {
        return lastKillPlayerId;
    }

    public void setLastKillPlayerId(int lastKillPlayerId) {
        this.lastKillPlayerId = lastKillPlayerId;
    }

    public Map<Integer, WorldBoss> getWorldBossMap() {
        return worldBossMap;
    }

    public void setWorldBossMap(Map<Integer, WorldBoss> worldBossMap) {
        this.worldBossMap = worldBossMap;
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

    public boolean checkAllDead() {
        for (WorldBoss worldBoss : worldBossMap.values()) {
            if (worldBoss.getCurHp() > 0) {
                return false;
            }
        }
        return true;
    }
}
