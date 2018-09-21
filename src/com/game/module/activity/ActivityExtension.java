package com.game.module.activity;

import com.game.params.IntList;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class ActivityExtension {
    @Autowired
    private ActivityService activityService;
    @Autowired
    private WelfareCardService welfareCardService;

    @Command(8001)
    public Object getPlayerActivitys(int playerId, IntList param) {
        return activityService.getPlayerActivitys(playerId, param.iList);
    }

    @Command(8002)
    public Object getActivityReward(int playerId, IntParam param) {
        return activityService.getActivityAwards(playerId, param.param);
    }

    @Command(8003)
    public Object fixedActivityAwards(int playerId, IntParam param) {
        return activityService.fixedActivityAwards(playerId, param.param);
    }

    @Command(8007)
    public Object buyActivity(int playerId, IntParam param) {
        return activityService.openActivity(playerId, param.param);
    }

    @Command(8008)
    public Object getWelfareCardInfo(int playerId, Object param) {
        return welfareCardService.getWelfareCardInfo(playerId);
    }

    @Command(8009)
    public Object buyActivityTask(int playerId, IntParam param) {
        return activityService.buyActivityTask(playerId, param.param);
    }

    /**
     * 购买命运卡牌
     *
     * @param playerId 玩家id
     * @param param    活动id
     * @return
     */
    @Command(8010)
    public Object buyDestinyCard(int playerId, IntParam param) {
        return activityService.buyDestinyCard(playerId, param.param);
    }

    /**
     * 刷新命运卡牌
     * @param playerId
     * @param param
     * @return
     */
    @Command(8011)
    public Object refreshDestinyCard(int playerId, IntParam param) {
        return activityService.refreshDestinyCard(playerId, param.param);
    }
}
