package com.game.module.player;

import io.netty.channel.Channel;

/**
 * Created by lucky on 2018/1/17.
 */
public class User {
    public final int playerId;
    public final Channel channel;

    public User(int playerId, Channel channel) {
        this.playerId = playerId;
        this.channel = channel;
    }
}
