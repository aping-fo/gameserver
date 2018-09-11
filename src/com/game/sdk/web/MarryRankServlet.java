package com.game.sdk.web;

import com.game.sdk.service.MarryService;
import com.game.sdk.utils.WebHandler;
import com.game.util.BeanManager;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lucky on 2018/2/28.
 */
@WebHandler(url = "/marry101/rank", description = "获取排行榜")
public class MarryRankServlet extends SdkServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int beginIndex = Integer.parseInt(req.getParameter("beginIndex")); //
            int endIndex = Integer.parseInt(req.getParameter("endIndex")); //

            ServerLogger.info("request param = ", beginIndex, endIndex);
            if (beginIndex >= endIndex
                    || endIndex <= 0
                    || beginIndex < 0) {
                resp.getWriter().write("request param error");
                resp.getWriter().flush();
                return;
            }

            MarryService marryService = BeanManager.getBean(MarryService.class);
            String json = marryService.queryMarry(beginIndex, endIndex);
            ServerLogger.info(json);
            resp.getWriter().write(json);
            resp.getWriter().flush();
        } catch (Exception e) {
            resp.getWriter().write("request param error");
            resp.getWriter().flush();

            ServerLogger.info("request param = ", req.getParameter("beginIndex"), req.getParameter("endIndex"));
        }
    }
}
