package com.game.sdk.erating.domain;


import com.game.sdk.erating.domain.base.Header;
import com.game.sdk.erating.domain.base.NodeName;

/**
 * Created by lucky on 2018/2/26.
 */
public class CommonRespInfo extends Header {
    public CommonRespInfo(int command_id) {
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
