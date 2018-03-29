package com.game.module.log.domain;

import java.util.Date;

/**
 * Created by lucky on 2018/3/12.
 */
@DB(name = "server")
public class Server extends AbstractDb {
    @DB(name = "server_id")
    private int id;
    @DB(name = "server_name")
    private String name;
    @DB(name = "channel_id")
    private int channelId;
    @DB(name = "channel_name")
    private String channelName;
    @DB(name = "open_date")
    private Date openData;
    @DB(name = "server_type")
    private int type;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Date getOpenData() {
        return openData;
    }

    public void setOpenData(Date openData) {
        this.openData = openData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
