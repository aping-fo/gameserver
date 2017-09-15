package com.game.module.ladder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    //public final List<Integer> blueArr;
    /**
     * 红队
     */
    //public final List<Integer> redArr;

    //*******************************上面预留
    //ID
    public final int id;
    //匹配标识
    public boolean matchFlag;
    //上次匹配时间
    public long time;
    //匹配次数
    public int count;
    //取消标记
    public volatile boolean exitFlag;

    public final int score;

    public volatile boolean fightFlag;

    public final Map<Integer, RoomPlayer> roomPlayers;

    public Room(int id, int score, int type) {
        this.id = id;
        this.score = score;
        this.matchFlag = false;
        this.exitFlag = false;
        this.roomPlayers = new ConcurrentHashMap<>();

        //this.type = type;
        //blueArr = new CopyOnWriteArrayList<>();
        //redArr = new CopyOnWriteArrayList<>();
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
