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
@WebHandler(url = "/marry101/updateDatas", description = "更新自定义数据")
public class MarryDatasServlet extends SdkServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String openId = req.getParameter("openId"); //
            String datas = req.getParameter("datas"); //

            ServerLogger.info("request param = ", openId, datas);
            if (StringUtils.isEmpty(openId)
                    || StringUtils.isEmpty(datas)) {
                render(resp, "request param error");
                ServerLogger.warn("request param error = ", openId, datas);
                return;
            }

            MarryService marryService = BeanManager.getBean(MarryService.class);
            marryService.updateDatas(openId.trim(), datas.trim());

            render(resp, "Success");
        } catch (Exception e) {
            ServerLogger.err(e, "");
            resp.getWriter().write("request param error");
            resp.getWriter().flush();
        }
    }
}
