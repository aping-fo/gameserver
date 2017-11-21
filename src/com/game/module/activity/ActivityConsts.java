package com.game.module.activity;

/**
 * Created by lucky on 2017/11/17.
 */
public class ActivityConsts {

    public static class TaskType {

    }

    public static class ActivityType {
        public static final int T_SIGN = 1; //签到
        public static final int T_ENERGY = 2; //体力
        public static final int T_LEV_UP = 3; //升级
        public static final int T_GROW_UP = 4; //成长
    }

    public static class ActivityState {
        public static final int T_UN_FINISH = 1; //未完成
        public static final int T_FINISH = 2; //已完成未领奖
        public static final int T_AWARD = 3; //已领奖
        public static final int T_AGAIN_AWARD = 4; //补领
    }

    public static class ActivityCondType {
        public static final int T_VIP = 1; //vip等级
        public static final int T_ITEM = 2; //道具消耗
    }

    public static class ActivityTaskCondType {
        public static final int T_TIME = 1; //时间区间类型
        public static final int T_LEVEL = 2; //等级类型
    }

    public static class UpdateType {
        public static final int T_ADD = 1; //累加
        public static final int T_VALUE = 2; //最终
    }

    public static class ActivityOpenType {
        public static final int T_AUTO = 1; //自动领取
        public static final int T_HANDLE = 2; //手动领取
    }
}
