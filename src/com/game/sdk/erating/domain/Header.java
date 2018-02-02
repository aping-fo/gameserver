package com.game.sdk.erating.domain;

/**
 * Created by lucky on 2018/2/1.
 * 包含协议头拼装
 */
public class Header extends Report {
    public final int commandId;
    public final int game_id;
    public final int gateway_id;
    private final StringBuilder sb;

    public Header(int command_id, int game_id, int gateway_id) {
        this.commandId = command_id;
        this.game_id = game_id;
        this.gateway_id = gateway_id;
        sb = new StringBuilder("<agip>");
        sb.append("<header>");
        sb.append("<command_id>").append(command_id).append("</command_id>");
        sb.append("<game_id>").append(game_id).append("</game_id>");
        sb.append("<gateway_id>").append(gateway_id).append("</gateway_id>");
        sb.append("</header>");
    }


    public int getCommandId() {
        return commandId;
    }

    public String toProto() throws Exception {
        sb.append("<body>");
        sb.append(super.toProto());
        sb.append("</body>");
        sb.append("</agip>");
        return sb.toString();
    }
}
