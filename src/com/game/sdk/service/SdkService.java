package com.game.sdk.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lucky on 2018/2/28.
 */
public interface SdkService {
    public void recharge(HttpServletRequest req,HttpServletResponse resp) throws Exception;
}
