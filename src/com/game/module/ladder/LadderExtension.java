package com.game.module.ladder;

import com.game.params.IntParam;
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

    @Command(6008)
    public Object loadingOver(int playerId, Object param) {
        ladderService.loadingOver(playerId);
        return null;
    }

    @Command(6009)
    public Object getRecords(int playerId, Object param) {
        return ladderService.getRecords(playerId);
    }

    @Command(6011)
    public Object aiResult(int playerId, IntParam param) {
        ladderService.aiResult(playerId, param.param);
        return null;
    }
}
