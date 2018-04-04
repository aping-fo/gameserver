package com.game.sdk.erating.domain;

import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2018/2/1.
 */

public class GatewayReqInfos extends Header {
    @NodeName(name = "data_info_list", object = true)
    private final List<GatewayInfo> gwData;

    public GatewayReqInfos(int command_id) {
        super(command_id);
        gwData = Lists.newArrayList();
    }

    public List<GatewayInfo> getGwData() {
        return gwData;
    }
}
