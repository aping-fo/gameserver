package com.game.module.drama;

import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class DramaExtension {
    @Autowired
    DramaService dramaService;

    @Command(10601)
    public Object getDramaOrder(int playerId, Object object) { return dramaService.getDramaOrder(playerId); }

    @Command(10602)
    public Object saveDramaOrder(int playerId, IntParam param) {
        dramaService.saveDramaOrder(playerId, param);

        return null;
    }
}
