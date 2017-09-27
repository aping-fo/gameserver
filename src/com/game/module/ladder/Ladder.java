package com.game.module.ladder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucky on 2017/9/13.
 * <p>
 * 排位赛对象
 */
public class Ladder {

    private int playerId; //角色ID
    private int score; //积分
    private int level; //段位
    private int times; //总场次
    private int winTimes; //胜利总次数
    private long lastTime;
    private int continuityWinTimes; //连续胜利
    private int maxContinuityWinTimes; //连续胜利
    private int fightTimes; //挑战次数
    private List<LadderRecord> records = new ArrayList<>(); //对战记录

    public List<LadderRecord> getRecords() {
        return records;
    }

    public void setRecords(List<LadderRecord> records) {
        this.records = records;
    }

    public int getMaxContinuityWinTimes() {
        return maxContinuityWinTimes;
    }

    public void setMaxContinuityWinTimes(int maxContinuityWinTimes) {
        this.maxContinuityWinTimes = maxContinuityWinTimes;
    }

    public int getFightTimes() {
        return fightTimes;
    }

    public void setFightTimes(int fightTimes) {
        this.fightTimes = fightTimes;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getWinTimes() {
        return winTimes;
    }

    public void setWinTimes(int winTimes) {
        this.winTimes = winTimes;
    }

    public int getContinuityWinTimes() {
        return continuityWinTimes;
    }

    public void setContinuityWinTimes(int continuityWinTimes) {
        this.continuityWinTimes = continuityWinTimes;
    }
}
