package com.game.module.multi;

import com.game.params.LongParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class MultiExtension {

    @Autowired
    private MultiService multiService;

    @Command(4909)
    public Object getWorldBossHurt(int playerId, LongParam param) {
        multiService.hostHeart(playerId);
        return null;
    }

}
