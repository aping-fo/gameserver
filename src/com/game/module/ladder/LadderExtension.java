package com.game.module.ladder;

import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lucky on 2017/9/13.
 */
@Extension
public class LadderExtension {
    @Autowired
    private LadderService ladderService;

    @Command(6001)
    public Object getLadderInfo(int playerId, Object param) {
        return ladderService.getLadderInfo(playerId);
    }

    @Command(6002)
    public Object startMatching(int playerId, Object param) {
        return ladderService.startMatching(playerId);
    }

    @Command(6003)
    public Object cancelMatching(int playerId, Object param) {
        return ladderService.cancelMatching(playerId);
    }
}
