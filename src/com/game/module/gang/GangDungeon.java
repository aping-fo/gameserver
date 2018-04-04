package com.game.module.gang;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lucky on 2017/10/10.
 */
public class GangDungeon {
    /**
     * 第几层
     */
    private int layer;
    /**
     * 怪物列表
     */
    private Map<Integer, Monster> monsterMap = new HashMap<>();
    /**
     * 奖励列表
     */
    private List<Integer> awardStep = new ArrayList<>();
    @JsonIgnore
    private ReentrantLock lock = new ReentrantLock();

    //挑战标记
    @JsonIgnore
    public int fighter = 0;

    /**
     * 0：未开启，1：已开启，2：已通关
     */
    private int hasOpen = 1;
    @JsonIgnore
    public volatile boolean hasOver; //战斗是否结束并结算
    /**
     * 公会BOSS 伤害
     */
    private Map<Integer, GangHurt> hurtMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock() {
        return lock;
    }

    /**
     * 公会BOSS 伤害排名
     */
    @JsonIgnore
    public TreeMap<GangHurt, GangHurt> hurtRankMap = new TreeMap<>(new Comparator<GangHurt>() {
        @Override
        public int compare(GangHurt o1, GangHurt o2) {
            if (o1.getPlayerId() == o2.getPlayerId()) {
                return 0;
            }
            if (o1.getHurt() == o2.getHurt()) {
                return o1.getPlayerId() - o2.getPlayerId();
            }
            return o2.getHurt() - o1.getHurt();
        }
    });

    public boolean checkAndSet(int playerId) {
        try {
            lock.lock();
            if (this.fighter != 0) {
                return false;
            }
            this.fighter = playerId;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean clearFighter(int playerId) {
        try {
            lock.lock();
            if (fighter != playerId) {
                return false;
            }
            this.fighter = 0;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkAndAdd(int step) {
        try {
            lock.lock();
            if (awardStep.contains(step)) {
                return false;
            }
            awardStep.add(step);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public List<Integer> getAwardStep() {
        return awardStep;
    }

    public void setAwardStep(List<Integer> awardStep) {
        this.awardStep = awardStep;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public Map<Integer, GangHurt> getHurtMap() {
        return hurtMap;
    }

    public void setHurtMap(Map<Integer, GangHurt> hurtMap) {
        this.hurtMap = hurtMap;
    }

    public Map<Integer, Monster> getMonsterMap() {
        return monsterMap;
    }

    public void setMonsterMap(Map<Integer, Monster> monsterMap) {
        this.monsterMap = monsterMap;
    }

    public int getHasOpen() {
        return hasOpen;
    }

    public void setHasOpen(int hasOpen) {
        this.hasOpen = hasOpen;
    }

    public float getProgress() {
        float totalHp = 0;
        float currentHp = 0;
        for (Monster vo : monsterMap.values()) {
            totalHp += vo.getHp();
            currentHp += vo.getCurrentHp();
        }
        if (totalHp == 0) {
            return 0;
        } else {
            return (totalHp - currentHp) / totalHp * 100;
        }
    }

    public boolean checkDeath() {
        for (Monster vo : monsterMap.values()) {
            if (vo.getCurrentHp() > 0) {
                return false;
            }
        }
        return true;
    }


    public int getTotalHp() {
        int totalHp = 0;
        for (Monster vo : monsterMap.values()) {
            totalHp += vo.getHp();
        }
        return totalHp;
    }

    public void reset() {
        layer = 1;
        hasOpen = 1;
        hasOver = false;
        monsterMap.clear();
        awardStep.clear();
    }
}