package com.game.module.title;

import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lucky on 2017/12/26.
 */
@Extension
public class TitleExtension {
    @Autowired
    private TitleService titleService;

    @Command(9001) //装备称号
    public Object equipTitle(int playerId, IntParam param) {
        return titleService.equipTitle(playerId, param.param);
    }

    @Command(9003) //装备称号
    public Object getTitles(int playerId, Object param) {
        return titleService.getTitles(playerId);
    }

    @Command(9004) //打开称号
    public Object openTitle(int playerId, IntParam param) {
        return titleService.openTitle(playerId, param.param);
    }

    @Command(9006) //获取红点列表
    public Object getThreeTypesRed(int playerId, Object param) {
        return titleService.getThreeTypesRed(playerId);
    }

    @Command(9007) //获取红点列表
    public Object getThreeTypesRed(int playerId, Int2Param param) {
        return titleService.getSingleTypesRed(playerId, param);
    }
}
