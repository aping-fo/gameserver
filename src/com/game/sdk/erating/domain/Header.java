package com.game.sdk.erating.domain;

import com.game.SysConfig;

/**
 * Created by lucky on 2018/2/1.
 * 包含协议头拼装
 */
public class Header extends Report {
    public Integer command_id;
    public Integer game_id;
    public Integer gateway_id;

    public Header(int command_id) {
        this.command_id = command_id;
        this.game_id = SysConfig.gameId;
        this.gateway_id = SysConfig.gatewayId;
    }

    public Integer getCommand_id() {
        return command_id;
    }

    public void setCommand_id(Integer command_id) {
        this.command_id = command_id;
    }

    public Integer getGame_id() {
        return game_id;
    }

    public void setGame_id(Integer game_id) {
        this.game_id = game_id;
    }

    public Integer getGateway_id() {
        return gateway_id;
    }

    public void setGateway_id(Integer gateway_id) {
        this.gateway_id = gateway_id;
    }

    public String toProto() throws Exception {
        StringBuilder sb = new StringBuilder("<agip>");
        sb.append("<header>");
        sb.append("<command_id>").append(command_id).append("</command_id>");
        sb.append("<game_id>").append(game_id).append("</game_id>");
        sb.append("<gateway_id>").append(gateway_id).append("</gateway_id>");
        sb.append("</header>");
        sb.append("<body>");
        sb.append(super.toProto());
        sb.append("</body>");
        sb.append("</agip>");
        return sb.toString();
    }
}
