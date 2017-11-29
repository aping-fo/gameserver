package com.game.module.sct;

import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.TrainVo;
import com.server.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillCardTrainService {
    private static final int CMD_GET_TRAIN = 1;

    @Autowired
    private PlayerService playerService;

    public TrainVo getTrainInfo(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        return data.getTrain().toProto();
    }

    public void updateCopyTimes(int playerId, int characterId, int consumeCount, int copyId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        Integer count = playerData.getTrain().getGroupTimes().get(characterId);
        if (count == null) {
            count = 0;
        }
        count += consumeCount;
        playerData.getTrain().getGroupTimes().put(characterId, count);
        if (copyId != 0) {
            playerData.getTrain().getChallengeIds().add(copyId);
        }
        getTrainInfo(playerId);

        SessionManager.getInstance().sendMsg(CMD_GET_TRAIN, null, playerId);
    }

    public void dailyRest(int playerId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        playerData.getTrain().dailyRest();
        getTrainInfo(playerId);
        SessionManager.getInstance().sendMsg(CMD_GET_TRAIN, null, playerId);
    }
}
