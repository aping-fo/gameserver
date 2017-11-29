package com.game.module.sct;

import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class SkillCardTrainExtension {

    @Autowired
    private SkillCardTrainService skillCardTrainService;

    @Command(1927)
    public Object getRewards(int playerId, Object param) {
        return skillCardTrainService.getTrainInfo(playerId);
    }
}
