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
            map.put("content", "游戏冲榜活动：\n" +
                    "\n" +
                    "活动时间：11月19日~11月30日晚十点\n" +
                    "\n" +
                    "活动规则：世界排行榜第1名获得1010元现金，第2~5名各获得101元现金奖励，前100名获得10.1元现金奖励。\n" +
                    "\n" +
                    "获奖者请添加微信号：dpc201706，领取现金红包。");
            map.put("time", "2018.11.19");

            String json = JsonUtils.map2String(map);

            render(resp, json);
        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
