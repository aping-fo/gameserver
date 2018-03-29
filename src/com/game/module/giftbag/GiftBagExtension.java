package com.game.module.giftbag;

import com.game.params.StringParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class GiftBagExtension {
    @Autowired
    private GiftBagService giftBagService;

    @Command(10301)//激活码验证
    public Object sendCDK(int playerId, StringParam param) {
        return giftBagService.getGiftBag(playerId,param);
    }
}
