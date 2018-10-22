package com.game.sdk.web;

import com.game.sdk.service.MarryService;
import com.game.sdk.utils.WebHandler;
import com.game.util.BeanManager;
import com.game.util.StringUtil;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lucky on 2018/2/28.
 */
@WebHandler(url = "/marry101/datasQuery", description = "获取自定义数据")
public class MarryDatasQueryServlet extends SdkServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String openId = req.getParameter("openId"); //

            ServerLogger.info("request param = ", openId);
            if (StringUtils.isEmpty(openId)) {
                render(resp, "request param error");
                return;
            }

            MarryService marryService = BeanManager.getBean(MarryService.class);
            String json = marryService.queryDatas(openId);
            ServerLogger.info(json);
            render(resp, json);
        } catch (Exception e) {
            resp.getWriter().write("request param error");
            resp.getWriter().flush();

            ServerLogger.info("request param = ", req.getParameter("beginIndex"), req.getParameter("endIndex"));
        }
    }
}
