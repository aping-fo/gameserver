package com.game.module.activity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Created by lucky on 2017/11/23.
 */
public class WelfareCard {
    /**
     * 月卡开始时间
     */
    private long monthlyTime;
    /***周卡开始时间*/
    private long weeklyTime;

    /**
     * 月卡剩余天数
     */
    private int monthlyDays;
    /**
     * 周卡剩余天数
     */
    private int weeklyDays;

    public long getMonthlyTime() {
        return monthlyTime;
    }

    public void setMonthlyTime(long monthlyTime) {
        this.monthlyTime = monthlyTime;
    }

    public long getWeeklyTime() {
        return weeklyTime;
    }

    public void setWeeklyTime(long weeklyTime) {
        this.weeklyTime = weeklyTime;
    }

    public int getMonthlyDays() {
        return monthlyDays;
    }

    public void setMonthlyDays(int monthlyDays) {
        this.monthlyDays = monthlyDays;
    }

    public int getWeeklyDays() {
        return weeklyDays;
    }

    public void setWeeklyDays(int weeklyDays) {
        this.weeklyDays = weeklyDays;
    }

    /**
     * 获取剩余月卡天数
     *
     * @return
     */
    public int getRemainMonthlyDays() {
        return minusTodayToOtherTime(monthlyDays, monthlyTime);
    }

    private int minusTodayToOtherTime(int day, long time) {
        if (time == 0) {
            return 0;
        }
        Instant instant = Instant.ofEpochMilli(time);
        LocalDateTime beginDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime nowDate = LocalDateTime.now();
        int passDay = (int) nowDate.until(beginDateTime, ChronoUnit.DAYS);
        int remain = day - passDay > 0 ? day - passDay : 0;
        return remain;
    }

    /**
     * 获取剩余月卡天数
     *
     * @return
     */
    public int getRemainWeeklyDays() {
        return minusTodayToOtherTime(weeklyDays, weeklyTime);
    }


}
