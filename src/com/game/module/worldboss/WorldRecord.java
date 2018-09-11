package com.game.module.worldboss;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ConcurrentHashMap<Integer, HurtRecord> hurtMap = new ConcurrentHashMap<>();
    /**
     * 最后一击,bossId -- 玩家ID
     * 也表示该boss死亡
     */
    private ConcurrentHashMap<Integer, Integer> killMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, WorldBoss> worldBossMap = new ConcurrentHashMap<>();
    //玩家死亡时间，CD用
    private ConcurrentHashMap<Integer, Long> playerDeadTime = new ConcurrentHashMap<>();
    //攻击购买次数
    private ConcurrentHashMap<Integer, Integer> buyTimes = new ConcurrentHashMap<>();
    private long startTime; //开始时间
    private int day;
    //本次活动开始时间
    private int openHour;
    //本次活动结束时间
    private int endHour;
    //击杀最后一个BOSS 玩家
    private int lastKillPlayerId;
    //最后一个boss杀死时间
    private long lastKillTime;
    //最后BOSS ID
    private int lastBossId;
    //10分钟广播标记
    private boolean bTenMin;
    //5分钟
    private boolean bFiveMin;
    //开始
    private boolean bStart;
    //是否领奖
    private boolean bAward;

    private List<HurtRecord> top10 = new ArrayList<>();
    private Map<Integer, HurtRecord> rankMap = new ConcurrentHashMap<>();

    @JsonIgnore
    private AtomicBoolean bUpdate = new AtomicBoolean(false);

    public List<HurtRecord> getTop10() {
        return top10;
    }

    public Map<Integer, HurtRecord> getRankMap() {
        return rankMap;
    }

    public void setRankMap(Map<Integer, HurtRecord> rankMap) {
        this.rankMap = rankMap;
    }

    public void setTop10(List<HurtRecord> top10) {
        this.top10 = top10;
    }

    public boolean isbAward() {
        return bAward;
    }

    public void setbAward(boolean bAward) {
        this.bAward = bAward;
    }

    public boolean isbTenMin() {
        return bTenMin;
    }

    public void setbTenMin(boolean bTenMin) {
        this.bTenMin = bTenMin;
        setbUpdate(true);
    }

    public boolean isbFiveMin() {
        return bFiveMin;
    }

    public void setbFiveMin(boolean bFiveMin) {
        this.bFiveMin = bFiveMin;
        setbUpdate(true);
    }

    public int getTotalHp() {
        int hp = 0;
        for (WorldBoss wb : worldBossMap.values()) {
            hp += wb.getHp();
        }
        return hp;
    }

    public boolean isbStart() {
        return bStart;
    }

    public void setbStart(boolean bStart) {
        this.bStart = bStart;
    }

    public int getOpenHour() {
        return openHour;
    }

    public void setOpenHour(int openHour) {
        this.openHour = openHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public AtomicBoolean getbUpdate() {
        return bUpdate;
    }

    public void setbUpdate(boolean bUpdate) {
        this.bUpdate.compareAndSet(this.bUpdate.get(), bUpdate);
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

    public void setPlayerDeadTime(ConcurrentHashMap<Integer, Long> playerDeadTime) {
        this.playerDeadTime = playerDeadTime;
    }

    public Map<Integer, Integer> getBuyTimes() {
        return buyTimes;
    }

    public void setBuyTimes(ConcurrentHashMap<Integer, Integer> buyTimes) {
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

    public void setWorldBossMap(ConcurrentHashMap<Integer, WorldBoss> worldBossMap) {
        this.worldBossMap = worldBossMap;
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

    public void setHurtMap(ConcurrentHashMap<Integer, HurtRecord> hurtMap) {
        this.hurtMap = hurtMap;
    }

    public Map<Integer, Integer> getKillMap() {
        return killMap;
    }

    public void setKillMap(ConcurrentHashMap<Integer, Integer> killMap) {
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
