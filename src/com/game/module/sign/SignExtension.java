package com.game.module.sign;

import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;

import javax.annotation.Resource;

/**
 * Created by lucky on 2017/7/3.
 */
@Extension
public class SignExtension {

    @Resource
    private SignService signService;

    @Command(4801)
    public Object sign(int playerId,Object param){
        Int2Param result = signService.sign(playerId);
        return result;
    }
}
