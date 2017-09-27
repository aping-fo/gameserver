package com.game.module.ladder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/9/13.
 */
public class Room {
    /**
     * 类型
     */
    //public final int type;
    /**
     * 蓝队
     */
    //public final Map<Integer, RoomPlayer> blueArr;
    /**
     * 红队
     */
    //public final Map<Integer, RoomPlayer> redArr;

    //*******************************上面预留
    //ID
    public final int id;
    //匹配标识
    public volatile boolean matchFlag;
    //上次匹配时间
    public long time;
    //匹配次数
    public int count;
    public int selfMatchCount;
    //取消标记
    public volatile boolean exitFlag;

    public final int score;

    public volatile boolean fightFlag;

    public final Map<Integer, RoomPlayer> roomPlayers;

    public final AtomicInteger loadingCount;

    public Room(int id, int score, int type) {
        this.id = id;
        this.score = score;
        this.matchFlag = false;
        this.exitFlag = false;
        this.roomPlayers = new ConcurrentHashMap<>();
        this.loadingCount = new AtomicInteger(0);

        //this.type = type;
        //this.blueArr = new ConcurrentHashMap<>();
        //this.redArr = new ConcurrentHashMap<>();
    }

    public RoomPlayer remove(Integer playerId) {
        return roomPlayers.remove(playerId);
    }

    public RoomPlayer getRoomPlayer(int playerId) {
        return roomPlayers.get(playerId);
    }

    public void clear() {
        roomPlayers.clear();
    }
}
