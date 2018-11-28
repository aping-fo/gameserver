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
        public static final int T_FULL_SERVICE_ATTENDANCE = 26; //全服登录人数
        public static final int T_TWO_DAYS = 27; //2日登录
        public static final int T_TOUR = 30; //巡礼活动
        public static final int T_INTEGRAL = 31; //积分活动
        public static final int T_ADVENTURE_BOX = 32; //奇遇宝箱
        public static final int T_CARD= 34; //卡牌
        public static final int T_CALL= 35; //累计召唤
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
        public static final int T_GUILD_DONATION = 30; //公会捐献
        public static final int T_RESOURCE_PURCHASE = 31; //资源副本购买
        public static final int T_NUMBER_FRIENDS = 32; //好友数量
        public static final int T_DAILY_ACTIVITY = 33; //日常任务
        public static final int T_COOPERATIVE_TASK = 34; //合作任务
        public static final int T_STORE_PURCHASE = 35; //商店购买
        public static final int T_GUILD_COPY = 36; //公会副本
        public static final int T_DIFFICULT_COPY_PURCHASE = 37; //困难副本购买
        public static final int T_ARENA_VICTORY = 38; //竞技场胜利
        public static final int T_GUILD_TECHNOLOGY = 39; //公会科技
        public static final int T_ARENA_CHALLENGE = 40; //竞技场挑战
        public static final int T_CAMP_PRESTIGE = 41; //阵营声望
        public static final int T_GUILD_BANQUET = 42; //公会宴会
        public static final int T_CARD_SUPPLY = 43; //召唤卡片补给
        public static final int T_EQUIPMENT_SUPPLY = 44; //召唤装备补给
        public static final int T_VIP_SUPPLY = 45; //召唤VIP补给
        public static final int T_TRAVERSING = 46; //时空仪组队
        public static final int T_GUILD_TASK = 47; //公会任务
        public static final int T_BUYING_ALCHEMY = 48; //购买炼金
        public static final int T_BUYING_STRENGTH = 49; //购买体力
        public static final int T_QUALIFYING_STAGE = 50; //排位赛段位
        public static final int T_GUILD_GRADE = 51; //公会等级
        public static final int T_PRIVILEGE_LEVEL = 52; //特权等级
        public static final int T_COMBAT_EFFECTIVENESS = 53; //战力
        public static final int T_INTEGRAL = 70; //积分
        public static final int T_CUMULATIVE_CONSUMPTION_DIAMONDS = 71; //累计消耗钻石
        public static final int T_CALL_REWARD = 81; //累计召喚

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
        public static final int T_BUY_CARD = 4; //购买卡牌
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

    /**
     * 领奖类型
     */
    public static class AutoReward{
        public static final int T_AUTO = 1; //自动领取
        public static final int T_HANDLE = 0; //手动领取
    }
}
