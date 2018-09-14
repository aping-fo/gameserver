package com.game.sdk.web;

import com.game.sdk.service.MarryService;
import com.game.sdk.service.SdkService;
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
@WebHandler(url = "/marry101/update", description = "更新排行榜")
public class MarryServlet extends SdkServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String openId = req.getParameter("openId"); //
            String nickName = req.getParameter("nickName"); //
            String avatarUrl = req.getParameter("avatarUrl"); //
            int score = Integer.parseInt(req.getParameter("score")); //

            ServerLogger.info("request param = ", openId, nickName, avatarUrl, req.getParameter("score"));
            if (StringUtils.isEmpty(openId)
                    || StringUtils.isEmpty(nickName)
                    || StringUtils.isEmpty(avatarUrl)
                    || score <= 0) {
                resp.getWriter().write("request param error");
                resp.getWriter().flush();
                ServerLogger.warn("request param error = ", openId, nickName, avatarUrl, req.getParameter("score"));
                return;
            }

            MarryService marryService = BeanManager.getBean(MarryService.class);
            marryService.saveOrUpdateScore(openId.trim(), nickName, avatarUrl, score);

        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
