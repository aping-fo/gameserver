package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;

/**
 * Created by lucky on 2018/2/5.
 * 角色日志
 */
public class RoleInfo extends Header {
    /**
     * 帐号ID。
     */
    @NodeName(name = "user_id")
    private int userId;
    /**
     * 角色名。
     */
    @NodeName(name = "role_name")
    private String roleName;
    @NodeName(name = "role_gender")
    private int roleGender;
    @NodeName(name = "role_occupation")
    private int roleOccupation;
    @NodeName(name = "initial_level")
    private int initialLevel;
    @NodeName(name = "user_ip")
    private int userIp;
    @NodeName(name = "user_port")
    private int userPort;
    @NodeName(name = "community_id")
    private int communityId;

    public RoleInfo(int command_id) {
        super(command_id);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleGender() {
        return roleGender;
    }

    public void setRoleGender(int roleGender) {
        this.roleGender = roleGender;
    }

    public int getRoleOccupation() {
        return roleOccupation;
    }

    public void setRoleOccupation(int roleOccupation) {
        this.roleOccupation = roleOccupation;
    }

    public int getInitialLevel() {
        return initialLevel;
    }

    public void setInitialLevel(int initialLevel) {
        this.initialLevel = initialLevel;
    }

    public int getUserIp() {
        return userIp;
    }

    public void setUserIp(int userIp) {
        this.userIp = userIp;
    }

    public int getUserPort() {
        return userPort;
    }

    public void setUserPort(int userPort) {
        this.userPort = userPort;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }
}
