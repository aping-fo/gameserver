package com.game.module.bulletscreen;

import com.game.params.Int3Param;
import com.game.params.IntStringParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class BulletScreenExtension {
    @Autowired
    private BulletScreenService bulletscreenService;

    @Command(10201)
    public Object sendBulletScreen(int playerId, IntStringParam param) {
        return bulletscreenService.sendBulletScreen(param);
    }

    @Command(10202)
    public Object getBulletScreen(int playerId, Int3Param param) {
        return bulletscreenService.getBulletScreen(param);
    }
}
