package com.game.sdk.web;

import com.game.sdk.service.SdkService;
import com.game.util.BeanManager;
import com.server.util.ServerLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lucky on 2018/2/28.
 */
public class SdkServlet extends HttpServlet {

    protected void doRecharge(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String[] arr = url.split("/");
        String bean = arr[1]; //获取平台关键字
        SdkService sdkService = BeanManager.getBean(bean);
        try {
            sdkService.recharge(req, resp);
        } catch (Throwable e) {
            ServerLogger.err(e, "充值异常，" + url);
        }
    }
}
