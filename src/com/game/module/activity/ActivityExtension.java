package com.game.module.activity;

import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class ActivityExtension {
    @Autowired
    private ActivityService activityService;

    @Command(8001)
    public Object getActivityList(int playerId, Object param) {
        return activityService.getOpenActivitys(playerId);
    }

    @Command(8002)
    public Object getActivityReward(int playerId, IntParam param) {
        return activityService.getActivityAwards(playerId,param.param);
    }

    @Command(8003)
    public Object getActivityRewardAgain(int playerId, IntParam param) {
        return activityService.getAwardAgain(playerId,param.param);
    }

    @Command(8007)
    public Object buyActivity(int playerId, IntParam param) {
        return activityService.openActivity(playerId,param.param);
    }
}
