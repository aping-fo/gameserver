package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2018/2/1.
 */

public class GwDatas extends Header {
    @NodeName(name = "data_info_list", object = true)
    private final List<GwData> gwData;

    public GwDatas(int command_id, int game_id, int gateway_id) {
        super(command_id, game_id, gateway_id);
        gwData = Lists.newArrayList();
    }

    public List<GwData> getGwData() {
        return gwData;
    }
}
