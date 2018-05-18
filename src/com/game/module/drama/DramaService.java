package com.game.module.drama;

import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.IntParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DramaService {
    @Autowired
    private PlayerService playerService;

    public IntParam getDramaOrder(int playerId) {
        IntParam param = new IntParam();
        PlayerData player = playerService.getPlayerData(playerId);

        param.param = player.getDramaOrder();

        return param;
    }

    public void saveDramaOrder(int playerId, IntParam param) {
        PlayerData player = playerService.getPlayerData(playerId);

        player.setDramaOrder(param.param);
    }
}
