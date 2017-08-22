package com.game.module.multi;

import com.server.util.ServerLogger;
import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lucky on 2017/8/1.
 */
public class MultiGroup {
    /**玩家列表*/
    private Set<Integer> players = new ConcurrentSet<>();
    private int hostId = 0; //当前主机玩家ID
    private ReentrantLock lock = new ReentrantLock();

    public Set<Integer> getAll() {
        return players;
    }

    public int getHostId() {
        return hostId;
    }

    public boolean contains(int playerId) {
        return players.contains(playerId);
    }

    public int addPlayer(int playerId) {
        try {
            lock.lock();
            if (!players.contains(playerId)) {
                players.add(playerId);
            }
            ServerLogger.warn("add new player playerId=>" + playerId + "  current group hostId = " + hostId);
            if (hostId == 0) {
                hostId = playerId;
                ServerLogger.warn("first hostId====>" + hostId);
            }
            return hostId;
        } finally {
            lock.unlock();
        }
    }

    public int removePlayer(int playerId) {
        try {
            lock.lock();
            players.remove(playerId);
            if (hostId == playerId) {
                if (players.isEmpty()) {
                    hostId = 0;
                    ServerLogger.warn("remove old host =>"+playerId+" ,change hostId ===>" + hostId);
                } else {
                    hostId = players.iterator().next();
                    ServerLogger.warn("remove old host =>"+playerId+" ,change hostId ===>" + hostId);
                }
            }
            return hostId;
        } finally {
            lock.unlock();
        }
    }
}
