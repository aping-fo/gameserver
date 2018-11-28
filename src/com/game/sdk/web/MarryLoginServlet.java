package com.game.sdk.web;

import com.game.sdk.service.MarryService;
import com.game.sdk.utils.WebHandler;
import com.game.util.BeanManager;
import com.game.util.JsonUtils;
import com.google.common.collect.Maps;
import com.server.util.ServerLogger;
import javafx.beans.binding.ObjectExpression;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebHandler(url = "/marry101/login", description = "登陆")
public class MarryLoginServlet extends SdkServlet {
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
            String invitor = req.getParameter("invitor"); //
            String unlockToolId = req.getParameter("unlockToolId");

            ServerLogger.info("request param = ", openId, nickName, avatarUrl);
            if (StringUtils.isEmpty(openId)) {
                render(resp, "request param error");
                return;
            }

            MarryService marryService = BeanManager.getBean(MarryService.class);
            marryService.login(openId, nickName, avatarUrl, invitor, unlockToolId);

            int score = marryService.queryScore(openId);

            Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
            map.put("result", "Success");
            map.put("score", score);

            String json = JsonUtils.map2String(map);

            render(resp, json);
        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
