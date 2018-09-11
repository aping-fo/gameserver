package com.game.sdk.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lucky on 2018/2/28.
 */
public interface SdkService {
    void recharge(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    default void render(HttpServletResponse resp, String message) throws Exception{
        resp.addHeader("Pragma", "no-cache");
        resp.addHeader("Accept", "*/*");
        resp.getWriter().write(message);
        resp.getWriter().flush();
    }
}
