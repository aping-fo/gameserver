package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;

/**
 * Created by lucky on 2018/2/5.
 * 登录认证日志
 * <p>
 * <p>
 * 保存各参数的以‘\0’结束的字符串，最大长度限制长(含结束符)为1000Byte。
 * <p>
 * 根据需要可以选择性的包含下述字段（顺序不限）。
 * <p>
 * 在进行手游对接时，以下字段在未进行手游说明的情况下，无需传送，若有手游标识，则表示有值的情况下必须传送：
 * <p>
 * 保存各参数的以‘\0’结束的字符串，最大长度限制长(含结束符)为1000Byte。
 * <p>
 * 根据需要可以选择性的包含下述字段（顺序不限）。
 * <p>
 * 在进行手游对接时，以下字段在未进行手游说明的情况下，无需传送，若有手游标识，则表示有值的情况下必须传送：
 * <p>
 * 1.UN：合作运营方用来标识其用户的唯一ID，一般情况下为第三方的用户ID；（手游）
 * <p>
 * 2.Token：认证加密串；（手游）
 * <p>
 * 3.UserIP4：用户登录的IPv4地址,ip需使用整数形式。
 * <p>
 * 如点分形式的ip地址127.0.0.1需要传入的整数形式为127*2563+0*2562+0*256+1=2130706433；（手游）
 * <p>
 * 4.Port：用户登录的端口；（手游）
 * <p>
 * 5.MAC，客户端主机MAC地址。ios7.0及以上传IDFA:格式为大写带横杠，
 * <p>
 * 例如：421389E5-4AAB-40B0-903F-836529768748  其它情况传MAC:格式为小写不带冒号，例如：02eaff21aa20；（手游）
 * <p>
 * 6.ClientType，客户端类型:1 ios;2 android;3 wp;4其它 （手游）
 * <p>
 * 7.SdkVersion，版本号，为空的情况下，默认使用最初的版本（手游）
 * <p>
 * 8.UnixTime，时间戳（手游）
 * <p>
 * 9.CP_ID，渠道编号（手游）
 * <p>
 * 10.Pad，扩展字段，用于临时数据传输（手游）
 * <p>
 * 11.ADID，广告ID（手游）
 * <p>
 * 12.UID：合作运营方帐号ID；
 * <p>
 * 13.AdultState：成人标志。
 * <p>
 * 14.Password（PW），帐号密码（加密后的密码，采用合作运营方passport系统自己的加密规则）；
 * <p>
 * 15.PasswordType（PWType），密码类型；
 * <p>
 * 1 - 表示一级登录密码。
 * <p>
 * 3 - 表示密保卡密码。
 * <p>
 * 4 - 表示动态密码。
 * <p>
 * 5 - 表示声讯密码（电话锁）。
 * <p>
 * 注意：无论密码类型如何，PW字段总为玩家的登录密码。
 * <p>
 * 16.IDCode，玩家身份证
 */
public class UserAuthenInfo extends Header {
    @NodeName(name = "UN")
    private String un;
    @NodeName(name = "Token")
    private String token;
    @NodeName(name = "UserIP4")
    private int userIP;
    @NodeName(name = "Port")
    private int port;
    @NodeName(name = "MAC")
    private String mac;
    @NodeName(name = "ClientType")
    private int clientType;
    @NodeName(name = "SdkVersion")
    private String sdkVersion;
    @NodeName(name = "UnixTime")
    private int unixTime;
    @NodeName(name = "ADID")
    private String adid;

    public UserAuthenInfo(int command_id) {
        super(command_id);
    }

    public String getUn() {
        return un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserIP() {
        return userIP;
    }

    public void setUserIP(int userIP) {
        this.userIP = userIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public int getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(int unixTime) {
        this.unixTime = unixTime;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }
}
