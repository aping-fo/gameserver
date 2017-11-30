package com.game.module.sct;

import com.game.data.SkillCardTrainCfg;
import com.game.data.TrialFieldCfg;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.TrainVo;
import com.game.util.ConfigData;
import com.google.common.collect.Lists;
import com.server.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SkillCardTrainService {
    private static final int CMD_GET_TRAIN = 1;

    @Autowired
    private PlayerService playerService;

    public TrainVo getTrainInfo(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        Train train = data.getTrain();
        TrainVo vo = new TrainVo();
        vo.ids = Lists.newArrayList();
        vo.rewards = Lists.newArrayList();
        vo.ids.addAll(train.getChallengeIds());
        for (Map.Entry<Integer, Integer> s : ConfigData.trainCount.entrySet()) {
            Int2Param param = new Int2Param();
            param.param1 = s.getKey();

            Integer count = train.getGroupTimes().get(s.getKey());
            if (count == null) {
                count = 0;
                train.getGroupTimes().put(s.getKey(), count);
            }
            param.param2 = s.getValue() - count;
            vo.rewards.add(param);
        }
        return vo;
    }

    public void updateCopyTimes(int playerId, int consumeCount, int copyId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        TrialFieldCfg cfg = ConfigData.trainCopy.get(copyId);
        int count = playerData.getTrain().getGroupTimes().get(cfg.type);
        count += consumeCount;
        playerData.getTrain().getGroupTimes().put(cfg.type, count);
        playerData.getTrain().getChallengeIds().add(copyId);
        TrainVo vo = getTrainInfo(playerId);
        SessionManager.getInstance().sendMsg(CMD_GET_TRAIN, vo, playerId);
    }

    public void dailyRest(int playerId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        playerData.getTrain().dailyRest();
        TrainVo vo = getTrainInfo(playerId);
        SessionManager.getInstance().sendMsg(CMD_GET_TRAIN, vo, playerId);
    }
}
