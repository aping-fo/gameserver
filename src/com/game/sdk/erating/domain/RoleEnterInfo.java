package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;

/**
 * Created by lucky on 2018/2/5.
 * 角色登录
 */
public class RoleEnterInfo extends Header {
    /**
     * 服务器标识，一般情况下与消息中header的网关ID一致。
     */
    @NodeName(name = "server_id")
    private int serverId;
    /**
     * 帐号ID。
     */
    @NodeName(name = "user_id")
    private int userId;
    /**
     * 角色ID。
     */
    @NodeName(name = "role_id")
    private int roleId;
    /**
     * 角色当前级别。
     */
    @NodeName(name = "level")
    private int level;
    /**
     * 角色性别（0-未定，1-男，2-女）。
     */
    @NodeName(name = "gender")
    private int gender;
    /**
     * 角色职业（或门派）。
     */
    @NodeName(name = "occupation_id")
    private int occupationId;
    /**
     * 角色所在军团、工会等。
     */
    @NodeName(name = "corps_id")
    private int corpsId;
    /**
     * 角色所在国家、阵营等。
     */
    @NodeName(name = "community_id")
    private int communityId;
    /**
     * 客户端端口。
     */
    @NodeName(name = "client_port")
    private int clientPort;
    /**
     * 客户端IP地址。如点分形式的ip地址127.0.0.1需要传入的整数形式为127*2563+0*2562+0*256+1=2130706433。
     */
    @NodeName(name = "client_ip")
    private int clientIp;
    /**
     * 客户端类型，手游：1 ios；2安卓；3 WP；4其它
     */
    @NodeName(name = "client_type")
    private int clientType;
    /**
     * 客户端主机MAC地址。ios7.0及以上传IDFA:格式为大写带横杠，例如：421389E5-4AAB-40B0-903F-836529768748其它情况传MAC:格式为小写不带冒号，例如：02eaff21aa20
     */
    @NodeName(name = "client_mac")
    private String clientMac;
    /**
     * 客户端主机硬件序列号，如含有英文字母请做大写转换。如：CPU序列号、BIOS序列号等硬件的原始或处理后的序列号。如不使用或无法获取时请不要设置。
     */
    @NodeName(name = "hardware_sn1")
    private String hardwareSn1;
    /**
     * 客户端主机硬件序列号，如含有英文字母请做大写转换。如：CPU序列号、BIOS序列号等硬件的原始或处理后的序列号。如不使用或无法获取时请不要设置。
     */
    @NodeName(name = "hardware_sn2")
    private String hardwareSn2;
    /**
     * 记录移动设备的唯一识别码,如含有英文字母请做大写转换,如不使用或无法获取时请不要设置。
     */
    @NodeName(name = "uddi")
    private String uddi;
    /**
     * 设备型号相关信息(未获取到则传空):按照设备类型###网络环境
     * ###操作系统版本号组合的形式进行传输(###为分割符号),并且字母全部小写,
     * 例如:iphone5s###wifi###ios-7.0.1ipad mini###3G###ios-6.5.1samsung galaxy n7100###wifi###android-4.2.2htc d816w###2g###android-4.0.1
     */
    @NodeName(name = "model_version")
    private String modelVersion;
    /**
     * 蓝港sdk所计算的客户端所在设备的唯一编号,未接入蓝港sdk则传空
     */
    @NodeName(name = "ldid")
    private String ldid;

    public RoleEnterInfo(int command_id) {
        super(command_id);
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(int occupationId) {
        this.occupationId = occupationId;
    }

    public int getCorpsId() {
        return corpsId;
    }

    public void setCorpsId(int corpsId) {
        this.corpsId = corpsId;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getClientIp() {
        return clientIp;
    }

    public void setClientIp(int clientIp) {
        this.clientIp = clientIp;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public String getClientMac() {
        return clientMac;
    }

    public void setClientMac(String clientMac) {
        this.clientMac = clientMac;
    }

    public String getHardwareSn1() {
        return hardwareSn1;
    }

    public void setHardwareSn1(String hardwareSn1) {
        this.hardwareSn1 = hardwareSn1;
    }

    public String getHardwareSn2() {
        return hardwareSn2;
    }

    public void setHardwareSn2(String hardwareSn2) {
        this.hardwareSn2 = hardwareSn2;
    }

    public String getUddi() {
        return uddi;
    }

    public void setUddi(String uddi) {
        this.uddi = uddi;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getLdid() {
        return ldid;
    }

    public void setLdid(String ldid) {
        this.ldid = ldid;
    }
}
