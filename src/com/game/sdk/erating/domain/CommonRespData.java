package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;

/**
 * Created by lucky on 2018/2/26.
 */
public class CommonRespData extends Header {
    public CommonRespData(int command_id) {
        super(command_id);
    }

    @NodeName(name = "result_code")
    private int resultCode;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
