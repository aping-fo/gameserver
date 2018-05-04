package com.game.module.ladder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
    public final int level;
    public volatile boolean fightFlag;

    public final Map<Integer, RoomPlayer> roomPlayers;

    public final AtomicInteger loadingCount;
    public volatile boolean rewardFlag = false;
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 开始战斗时间，用于检查战斗时间是否超时
     */
    public int startFightTime;
    /**
     * 客户端开始加载资源时间
     */
    public int startLoadTime;

    public Room(int id, int score, int type,int level) {
        this.id = id;
        this.score = score;
        this.matchFlag = false;
        this.exitFlag = false;
        this.roomPlayers = new ConcurrentHashMap<>();
        this.loadingCount = new AtomicInteger(0);
        this.level = level;
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

    /**
     * 检测是否结算奖励
     *
     * @return
     */
    public boolean checkHasRward() {
        try {
            lock.lock();
            boolean ret = rewardFlag;
            rewardFlag = true;
            return ret;
        } finally {
            lock.unlock();
        }
    }
}
