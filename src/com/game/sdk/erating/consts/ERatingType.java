package com.game.sdk.erating.consts;

/**
 * Created by lucky on 2018/2/1.
 */
public class ERatingType {
    public static final int ER_ONLINE_COUNT = 1;
    public static final int ER_DIAMOND_COUNT = 4;
    public static final int ER_DIAMOND_TOTAL = 5;
    public static final int ER_SERVER_START = 6;
    public static final int ER_SERVER_STOP = 7;
    public static final int ER_LOTTERY = 8;


    public static final int CMD_BIND = 10000001;//网关连接
    public static final int CMD_BIND_RESP = 20000001;//网关连接回应

    public static final int CMD_GW_DATA_REPORT = 10002003;//网关数据上报

    public static final int CMD_CREATE_ROLE = 10003102;//创建角色
    public static final int CMD_CREATE_ROLE_RESP = 20003102;//创建角色回应

    public static final int CMD_ROLE_ENTER_GAME_EX5 = 10003119;//角色进入游戏

    public static final int CMD_JOINT_AUTHEN_EX = 10003802; //用户登录认证

    public static final int CMD_USER_LOGOUT = 10003303; //角色登出游戏

    public static final int CMD_CHARGE = 10003413; //充值
    public static final int CMD_CHARGE_RESP = 20003413; //充值回应

    public static final int CMD_MONEY_COST = 10003717; //money消耗
    public static final int CMD_MONEY_ADD = 10003412; //money产出



    public static class ErrorCode {
        public static final int S_SUCCESS = 1; //表示操作成功；
        public static final int E_ERROR = 0; //表示操作失败
        /**
         * 表示输入的参数非法；
         * 例如，参数的取值越界、传了包含非法字符的字符串等；
         */
        public static final int E_PARAMETER_ERROR = -100;
        public static final int E_SYS_DATABASE_ERROR = -500; //数据库操作失败；
        public static final int E_ACCOUNT_NOT_FOUND = -1201; //玩家帐号不存在；
        public static final int E_CHARGE_DUPLICATE = -1472; //同一流水号重复充值；
        public static final int E_USER_ERROR_CHCAGE_DETAIL_ID = -1490; //充值流水号不存在；

    }
}
