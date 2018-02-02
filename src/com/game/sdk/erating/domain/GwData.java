package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;

/**
 * Created by lucky on 2018/2/1.
 */
@NodeName(name = "data_info")
public class GwData extends Report {
    @NodeName(name = "data_type")
    public final int dataType;
    @NodeName(name = "data_value")
    public final int dataValue;

    public GwData(int dataType, int dataValue) {
        this.dataType = dataType;
        this.dataValue = dataValue;
    }
}
