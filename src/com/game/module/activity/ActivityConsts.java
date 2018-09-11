package com.game.module.activity;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityConsts {
    /**
     * 活动类型,暂时先跟任务类型分开
     */
    public static class ActivityType {
        public static final int T_ENERGY = 1; //体力
        public static final int T_GROW_FUND = 2; //成长基金
        public static final int T_SEVEN_DAYS = 3; //7日登录
        public static final int T_NEW_ROLE = 4; //新手礼包
        public static final int T_LEVEL_UP = 5; //冲级
        public static final int T_ONLINE_TIME = 7; //在线活动
        public static final int T_FIRST_RECHARGE = 9; //累计充值
        public static final int T_TIMED_BAG = 10; //限时礼包
        public static final int T_SPECIAL_BAG = 11; //特价礼包
        public static final int T_DAILY_RECHARGE = 15; //每日充值

        public static final int T_FACEBOOK_SHARE = 16; //facebook分享
        public static final int T_FACEBOOK_INVITE = 17; //facebook邀请
        public static final int T_GIFT_BOX = 18; //超值礼盒
        public static final int T_PET_INVESTMENT = 21; //宠物投资
        public static final int T_EQUIPMENT_INVESTMENT = 22; //装备投资
        public static final int T_CARD_INVESTMENT = 23; //卡片投资
        public static final int T_ZERO_GIFTBAG = 25; //0元礼包
        public static final int T_TWO_DAYS = 27; //2日登录
    }

    /**
     * 活动任务完成条件类型
     */
    public static class ActivityTaskCondType {
        public static final int T_ENERGY = 1; //时间区间类型
        public static final int T_GROW_FUND = 2; //成长基金
        public static final int T_SEVEN_DAYS = 3; //7日登录
        public static final int T_FIRST_RECHARGE = 4; //累计充值
        public static final int T_LEVEL_UP = 5; //冲级活动
        public static final int T_TIMED_BAG = 9; //限时礼包
        public static final int T_TIMED_ONCE = 10; //单笔充值
        public static final int T_TIMED_MONEY = 11; //只要充了钱就算
        public static final int T_DAILY_RECHARGE = 12; //每日充值
        public static final int T_FACEBOOK_SHARE = 13; //facebook分享
        public static final int T_FACEBOOK_INVITE = 14; //facebook邀请
        public static final int T_BUY_DIAMOND = 15; //facebook邀请
        public static final int T_PET_INVESTMENT = 16; //宠物投资
        public static final int T_EQUIPMENT_INVESTMENT = 17; //装备投资
        public static final int T_CARD_INVESTMENT = 18; //卡片投资
        public static final int T_DAILY_RECHARGE_DIAMONDS = 19; //每日充值钻石
        public static final int T_TIMED_MONEY_DIAMONDS = 20; //累计充值钻石
        public static final int T_FULL_SERVICE_ATTENDANCE = 21; //全服登录人数
        public static final int T_TWO_DAYS = 22; //2日登录
    }

    /**
     * 活动状态
     */
    public static class ActivityState {
        public static final int T_UN_FINISH = 1; //未完成
        public static final int T_FINISH = 2; //已完成未领奖
        public static final int T_AWARD = 3; //已领奖
        public static final int T_AGAIN_AWARD = 4; //补领
    }

    /**
     * 活动开启条件类型
     */
    public static class ActivityCondType {
        public static final int T_VIP = 1; //vip等级
        public static final int T_ITEM = 2; //道具消耗
        public static final int T_LEVEL = 3; //开启等级
    }

    /**
     * 活动任务值更新类型
     */
    public static class UpdateType {
        public static final int T_ADD = 1; //累加
        public static final int T_VALUE = 2; //最终
    }

    /**
     * 活动开启类型
     */
    public static class ActivityOpenType {
        public static final int T_AUTO = 1; //自动领取
        public static final int T_HANDLE = 2; //手动领取
    }

    /**
     * 活动任务重置类型
     */
    public static class ActivityTaskResetType {
        public static final int T_NEVERY = 0; //不重置
        public static final int T_DAILY = 1; //日重置
        public static final int T_HANDLE = 2; //手动领取
    }

    /**
     * 福利卡类型
     */
    public static class WelfareCardType {
        public static final int T_WEEKLY = 1; //周卡
        public static final int T_MONTHLY = 2; //月卡
    }

    /**
     * 活动开启时间类型
     */
    public static class ActivityDropTimeCondType {
        public static final int T_APPOINT_TIME = 1; //指定时间开启
        public static final int T_OPEN_SERVICE_TIME = 2; //开服时间开启
    }
}
