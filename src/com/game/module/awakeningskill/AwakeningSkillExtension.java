package com.game.module.awakeningskill;

import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class AwakeningSkillExtension {
    @Autowired
    private AwakeningSkillService awakeningSkillService;

    @Command(10401)
    public Object GetAwakeningSkill(int playerId, Object param) {
        return awakeningSkillService.GetAwakeningSkill(playerId);
    }

    @Command(10402)
    public Object UpAwakeningSkill(int playerId, IntParam param) {
        return awakeningSkillService.UpAwakeningSkill(playerId, param);
    }
}
