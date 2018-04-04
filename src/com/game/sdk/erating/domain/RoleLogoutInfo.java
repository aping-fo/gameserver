package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;

/**
 * Created by lucky on 2018/2/5.
 * 角色登出日志
 */
public class RoleLogoutInfo extends Header {
    /**
     * 玩家帐号ID。
     */
    @NodeName(name = "user_id")
    private int userId;
    /**
     * 玩家的角色ID。
     */
    @NodeName(name = "role_id")
    private int roleId;
    /**
     * 登出标志。
     * <p>
     * 1   – 角色正常登出；
     * <p>
     * 2   – 报告角色在线状态。
     * <p>
     * 用于更新角色的级别等信息，以及计算防沉迷时间；
     * <p>
     * 3   – 异常登出。
     * <p>
     * 此处的异常指游戏相应接口服务无法获取角色级别等数据的情况
     * <p>
     * 而非掉线等对玩家而言的异常。
     */
    @NodeName(name = "logout_flag")
    private int logoutFlag;
    /**
     * 职业
     */
    @NodeName(name = "role_occupation")
    private int occupationId;
    /**
     * 当前角色的级别。
     */
    @NodeName(name = "role_level")
    private int roleLevel;
    /**
     * 网关ID。
     */
    @NodeName(name = "rating_id")
    private int ratingId;
    /**
     * 金钱1，非绑定金币。包括存款和身上携带的。
     */
    @NodeName(name = "money1")
    private int money1;
    /**
     * 金钱2，绑定金币。
     */
    @NodeName(name = "money2")
    private int money2;
    /**
     * 当前经验值
     */
    @NodeName(name = "experience")
    private int experience;

    public RoleLogoutInfo(int command_id) {
        super(command_id);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getLogoutFlag() {
        return logoutFlag;
    }

    public void setLogoutFlag(int logoutFlag) {
        this.logoutFlag = logoutFlag;
    }

    public int getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(int occupationId) {
        this.occupationId = occupationId;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public int getRatingId() {
        return ratingId;
    }

    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public int getMoney1() {
        return money1;
    }

    public void setMoney1(int money1) {
        this.money1 = money1;
    }

    public int getMoney2() {
        return money2;
    }

    public void setMoney2(int money2) {
        this.money2 = money2;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }
}
