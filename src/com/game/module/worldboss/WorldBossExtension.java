package com.game.module.worldboss;

import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class WorldBossExtension {

    public static final int PLAYER_DEAD = 4906;
    public static final int BOSS_DEAD = 4905;
    @Autowired
    private WorldBossService worldBossService;

    @Command(4902)
    public Object getWorldBossHurt(int playerId, Object param) {
        return worldBossService.getWorldBossHurtList(playerId);
    }

    @Command(4901)
    public Object getWorldBossInfo(int playerId, Object param) {
        return worldBossService.getWorldBossInfo(playerId);
    }

    @Command(4903)
    public Object startChallenge(int playerId, IntParam param) {
        return worldBossService.startChallenge(playerId, param.param);
    }

    @Command(4904)
    public Object buyAttack(int playerId, IntParam param) {
        return worldBossService.buyAttack(playerId,param.param);
    }

    @Command(4906)
    public Object playerDead(int playerId, IntParam param) {
        worldBossService.onHeroDead(playerId, param.param);
        return null;
    }

    @Command(4907)
    public Object revive(int playerId, Int2Param param) {
        worldBossService.revive(playerId, param.param1,param.param2);
        return null;
    }
}
