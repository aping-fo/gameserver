package com.game.module.fame;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.game.data.ShopCfg;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.FameListVO;
import com.game.params.IntParam;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.FameConfig;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.player.Upgrade;
import com.game.params.FameVo;
import com.game.params.ListParam;
import com.game.util.ConfigData;
import com.server.SessionManager;

/**
 * 声望系统
 */
@Service
public class FameService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivityService activityService;

    // 获取声望数据
    public FameListVO getInfo(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        FameListVO info = new FameListVO();
//        info.camp = data.getActivityCamp();
        info.fames = new ArrayList<>(data.getFames().size());
        for (Entry<Integer, Upgrade> fame : data.getFames().entrySet()) {
            FameVo vo = new FameVo();
            vo.camp = fame.getKey();
            vo.lev = fame.getValue().getLev();
            vo.totalExp = fame.getValue().getCurExp(); //总声望
            vo.exp = fame.getValue().getExp(); //当前声望
            info.fames.add(vo);
        }
        return info;
    }

    // 刷新数据
    public void refresh(int playerId) {
        SessionManager.getInstance().sendMsg(FameExtension.GET_INFO, getInfo(playerId), playerId);
    }

    // 添加声望
    public void addFame(int playerId, int camp, int fame) {
        if (fame <= 0) {
            return;
        }
        PlayerData data = playerService.getPlayerData(playerId);
        /*if (data.getActivityCamp() != 0) { //代表
            camp = data.getActivityCamp();
        }*/
        Upgrade fameData = data.getFames().get(camp);
        if (fameData == null) {
            fameData = new Upgrade();
            fameData.setLev(1);
            data.getFames().put(camp, fameData);
        }
        fameData.setExp(fameData.getExp() + fame);
        fameData.setCurExp(fameData.getCurExp() + fame);

        taskService.doTask(playerId, Task.TYPE_FAME, fameData.getExp());
        while (true) {
            FameConfig cfg = ConfigData.getConfig(FameConfig.class, camp * 1000 + fameData.getLev());
            if (cfg.exp > fameData.getExp()) {
                break;
            }
            int nextId = camp * 1000 + fameData.getLev() + 1;
            FameConfig nextCfg = ConfigData.getConfig(FameConfig.class, nextId);
            if (nextCfg == null) {
                break;
            }
            fameData.setLev(fameData.getLev() + 1);
            fameData.setExp(fameData.getExp() - cfg.exp);
        }

        //声望活动
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData != null) {
            if (activityService.checkIsOpen(playerData, ActivityConsts.ActivityTaskCondType.T_CAMP_PRESTIGE)) {
                Map<Integer, Integer> typeNumberMap = getTypeNumberMap(playerId, camp);
                if (typeNumberMap != null && !typeNumberMap.isEmpty()) {
                    activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_CAMP_PRESTIGE, true, typeNumberMap, true);
                }
            }
        } else {
            ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
        }

        refresh(playerId);
    }

    /**
     * 激活代表阵营
     *
     * @param playerId
     * @param camp
     * @return
     */
//    public IntParam activityAcmp(int playerId, int camp) {
//        PlayerData data = playerService.getPlayerData(playerId);
//        data.setActivityCamp(camp);
//        IntParam param = new IntParam();
//        param.param = camp;
//        return param;
//    }

    /**
     * 检测是否开放购买
     *
     * @param playerId
     * @param camp
     * @return
     */
    public boolean checkFameShopBuy(int playerId, int camp, int lv) {
        PlayerData data = playerService.getPlayerData(playerId);
        if (!data.getFames().containsKey(camp)) {
            return true;
        }
        Upgrade fameData = data.getFames().get(camp);
        return fameData.getLev() >= lv;
    }

    /**
     * 阵营声望
     *
     * @param playerId 玩家id
     * @return 阵营和声望
     */
    public Map<Integer, Integer> getTypeNumberMap(int playerId, int type) {
        Map<Integer, Integer> map = new ConcurrentHashMap<>();
        PlayerData data = playerService.getPlayerData(playerId);
        if (data == null) {
            ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
            return map;
        }
        Upgrade upgrade = data.getFames().get(type);
        if (upgrade == null) {
            ServerLogger.info("声望数据错误，阵营类型=" + type);
            return map;
        }

        int exp = upgrade.getExp();
        if (upgrade.getLev() > 1) {
            for (int i = 1; i < upgrade.getLev(); i++) {
                FameConfig cfg = ConfigData.getConfig(FameConfig.class, type * 1000 + i);
                exp += cfg.exp;
            }
        }

        map.put(type, exp);

        return map;
    }
}
