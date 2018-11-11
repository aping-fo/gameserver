package com.game.sdk.web;

import com.game.sdk.utils.WebHandler;
import com.game.util.JsonUtils;
import com.google.common.collect.Maps;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebHandler(url = "/marry101/notice", description = "公告")
public class MarryNoticeServlet extends SdkServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String openId = req.getParameter("openId"); //

            if (StringUtils.isEmpty(openId)) {
                render(resp, "request param error");
                return;
            }

            Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
            map.put("content", "暂无公告");
            map.put("time", "2018.9.10");

            String json = JsonUtils.map2String(map);

            render(resp, json);
        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
