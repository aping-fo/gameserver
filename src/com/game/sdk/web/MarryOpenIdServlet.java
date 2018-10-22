package com.game.sdk.web;

import com.game.sdk.net.HttpClient;
import com.game.sdk.utils.WebHandler;
import com.server.util.ServerLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lucky on 2018/2/28.
 */
@WebHandler(url = "/marry101/get_open_id", description = "获取openID")
public class MarryOpenIdServlet extends SdkServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String js_code = req.getParameter("code"); //

            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wx7004cb8d1a5b3df5&secret=80ab0be6003cec743ea964dedfad101c&grant_type=authorization_code&js_code=" + js_code;
            String json = HttpClient.sendGetRequest(url);
            ServerLogger.info(json);

            render(resp, json);
        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
