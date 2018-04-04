package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;

/**
 * Created by lucky on 2018/2/26.
 */
public class BindRespInfo extends Header {
    public BindRespInfo() {
        super(0);
    }

    private String gateway_code;
    private String gateway_password;
    private String mac;
    private Integer reconnect_flag;
    private Integer server_id;

    public String getGateway_code() {
        return gateway_code;
    }

    public void setGateway_code(String gateway_code) {
        this.gateway_code = gateway_code;
    }

    public String getGateway_password() {
        return gateway_password;
    }

    public void setGateway_password(String gateway_password) {
        this.gateway_password = gateway_password;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getReconnect_flag() {
        return reconnect_flag;
    }

    public void setReconnect_flag(Integer reconnect_flag) {
        this.reconnect_flag = reconnect_flag;
    }

    public Integer getServer_id() {
        return server_id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }
}
