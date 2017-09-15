package com.game.module.multi;

import com.game.data.SceneConfig;
import com.game.event.InitHandler;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.params.IntParam;
import com.game.params.Long2Param;
import com.game.params.scene.MoveStart;
import com.game.util.TimerService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/8/1.
 */
@Service
public class MultiService implements InitHandler {
    private final static int CMD_HOST = 4908; //

    private Map<Integer, Map<String, MultiGroup>> mulitSceneMap = new ConcurrentHashMap<>();
    //private final Map<Integer, Long> heartBeatMap = new ConcurrentHashMap<>();
    private final Cache<Integer,Long> heartBeatMap = CacheBuilder.newBuilder().expireAfterWrite(30,TimeUnit.SECONDS).build();
    @Autowired
    private TimerService timerService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SceneService sceneService;


    @Override
    public void handleInit() {
        //每秒钟检测，玩家是否掉线
        timerService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Map.Entry<Integer, Long> s : heartBeatMap.asMap().entrySet()) {
                        if (System.currentTimeMillis() - s.getValue() > 4000) {
                            ServerLogger.warn("time out remove playerId ===>" + s.getKey());
                            onExit(s.getKey());
                            heartBeatMap.invalidate(s.getKey());
                        }
                    }
                } catch (Exception e) {
                    ServerLogger.err(e, "心跳检测");
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public MultiGroup getGroup(int playerId) {
        Player player = playerService.getPlayer(playerId);
        String key = sceneService.getGroupKey(player);
        SceneConfig config = GameData.getConfig(SceneConfig.class, player.getSceneId());
        Map<String, MultiGroup> multiMap = mulitSceneMap.get(config.sceneSubType);
        if (multiMap != null) {
            MultiGroup group = multiMap.get(key);
            return group;
        }
        return null;
    }

    /**
     * 进入多人
     *
     * @param playerId
     */
    public void onEnter(int playerId) {
        Player player = playerService.getPlayer(playerId);
        String key = sceneService.getGroupKey(player);
        SceneConfig conf = GameData.getConfig(SceneConfig.class, player.getSceneId());
        Map<String, MultiGroup> multiMap = mulitSceneMap.get(conf.sceneSubType);
        if (multiMap == null) {
            multiMap = new ConcurrentHashMap<>();
            multiMap = mulitSceneMap.putIfAbsent(conf.sceneSubType, multiMap);
            if(multiMap == null) { //deal with multi thread
                multiMap = mulitSceneMap.get(conf.sceneSubType);
            }
        }
        MultiGroup group = multiMap.get(key);
        if (group == null) {
            group = new MultiGroup();
            group = multiMap.putIfAbsent(key, group);
            if(group == null) {
                group = multiMap.get(key);
            }
        }
        int oldId = group.getHostId();
        int masterId = group.addPlayer(playerId);

        if (oldId != masterId) {
            pushHost(masterId);
        }
    }

    /**
     * 主机切换
     *
     * @param playerId
     */
    private void pushHost(int playerId) {
        IntParam param = new IntParam();
        param.param = playerId;
        SessionManager.getInstance().sendMsg(CMD_HOST, param, playerId);
    }

    /**
     * 1、玩家掉线
     * 2、玩家退出
     * 3、心跳检测
     *
     * @param playerId
     */
    public void onExit(int playerId) {

        Player player = playerService.getPlayer(playerId);
        SceneConfig lastCfg = GameData.getConfig(SceneConfig.class, player.getSceneId());
        Map<String, MultiGroup> multiMap = mulitSceneMap.get(lastCfg.sceneSubType);
        if (multiMap != null) {
            String key = sceneService.getGroupKey(player);
            MultiGroup group = multiMap.get(key);
            if (group != null) {
                ServerLogger.warn("one player exit ,group key = " + key);
                int oldId = group.getHostId();
                int masterId = group.removePlayer(playerId);
                //heartBeatMap.remove(playerId);
                heartBeatMap.invalidate(playerId);
                if (oldId != masterId) {
                    pushHost(masterId);
                }
            }
        }
    }

    /**
     * 结束清理
     */
    public void clearGroup(int sceneType) {
        Map<String, MultiGroup> multiMap = mulitSceneMap.remove(sceneType);
        if (multiMap != null) {
            for (MultiGroup group : multiMap.values()) {
                if (group != null) {
                    for (int id : group.getAll()) {
//                        heartBeatMap.remove(id);
                        heartBeatMap.invalidate(id);
                    }
                }
            }
            multiMap.clear();
        }
    }


    /**
     * 主机判断心跳
     *
     * @param playerId
     * @return
     */
    public void hostHeart(int playerId) {
        heartBeatMap.put(playerId, System.currentTimeMillis());
        //TODO 先注释下
        /*Player player = playerService.getPlayer(playerId);
        SceneConfig lastCfg = GameData.getConfig(SceneConfig.class, player.getSceneId());
        Map<String, MultiGroup> multiMap = mulitSceneMap.get(lastCfg.sceneSubType);
        if (multiMap != null) {
            String key = sceneService.getGroupKey(player);
            MultiGroup group = multiMap.get(key);
            if (group != null && !group.contains(playerId)) { //如果不包含，重新加入
                group.addPlayer(playerId);
            }
        }*/
    }
}
