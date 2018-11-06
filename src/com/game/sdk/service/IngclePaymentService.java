package com.game.sdk.service;

import com.game.SysConfig;
import com.game.data.ChargeConfig;
import com.game.module.vip.VipService;
import com.game.sdk.utils.EncoderHandler;
import com.game.util.ConfigData;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Created by lucky on 2018/2/27.
 * 蓝港回调
 */
@Service("ingcle")
public class IngclePaymentService implements SdkService {
    @Autowired
    private VipService vipService;

    private final static String IOS_PAYMENT_TYPE = "IOS";

    public void recharge(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String agent = req.getParameter("agent"); //渠道代号
            String cpOrderId = req.getParameter("cp_order_id"); //cp游戏订单号
            String gameId = req.getParameter("game_id"); //平台游戏id
            String orderId = req.getParameter("order_id"); //平台订单号
            String orderStatus = req.getParameter("order_status"); //订单状态 2为支付成功，其他值统一为失败处理
            String payTime = req.getParameter("pay_time"); //支付时间
            String playerIdStr = req.getParameter("player_id"); //玩家平台id
            String productId = req.getParameter("product_id"); //商品id
            String productName = req.getParameter("product_name"); //商品名
            String productPriceStr = req.getParameter("product_price"); //商品金额
            String sign = req.getParameter("sign"); //签名
            String ext = req.getParameter("ext"); //透传参数

            StringBuilder sb = new StringBuilder();
            sb.append("agent=").append(agent).append("&")
                    .append("cp_order_id=").append(cpOrderId).append("&")
                    .append("game_id=").append(gameId).append("&")
                    .append("order_id=").append(orderId).append("&")
                    .append("order_status=").append(orderStatus).append("&")
                    .append("pay_time=").append(payTime).append("&")
                    .append("player_id=").append(playerIdStr).append("&")
                    .append("product_id=").append(productId).append("&")
                    .append("product_name=").append(URLEncoder.encode(productName, "UTF-8")).append("&")
                    .append("product_price=").append(productPriceStr).append("&")
                    .append("game_key=").append(SysConfig.gameKey);
            ServerLogger.warn(sb.toString() + "&sign=" + sign + "&ext=" + ext);

            float productPrice = Float.parseFloat(productPriceStr);
            int success = Integer.parseInt(orderStatus);
            if (productPrice <= 0 || StringUtils.isBlank(orderId) ||
                    StringUtils.isBlank(orderStatus) ||
                    StringUtils.isBlank(ext) ||
                    StringUtils.isBlank(sign) ||
                    StringUtils.isBlank(cpOrderId) ||
                    success != 2) {
                ServerLogger.warn("request param error");
                render(resp, "request param error");
                return;
            }

            String mySign = EncoderHandler.md5(sb.toString());
            if (!mySign.equals(sign)) {
                ServerLogger.warn("sign error", mySign);
                render(resp, "sign error.");
                return;
            }

            String[] arr = ext.split("_");
            int playerId = Integer.parseInt(arr[0]); //充值配置ID
            ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, Integer.parseInt(productId));
            if (charge.rmb != productPrice) {
                ServerLogger.warn("充值金额错误==>", "配置金额 = " + charge.rmb, ",充值金额 = " + productPrice);
                return;
            }
            //金额判断
            vipService.addCharge(playerId, Integer.parseInt(productId), Long.parseLong(cpOrderId), agent, SysConfig.currency, orderId, SysConfig.serverId);
            render(resp, "SUCCESS");
        } catch (Exception e) {
            render(resp, "error");
            ServerLogger.err(e, "充值异常");
        }
    }

    //IOS支付
    public void rechargeIOS(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String appId = req.getParameter("app_id"); //游戏ID
            String cpOrderId = req.getParameter("cp_order_id"); //游戏传入的外部订单号。服务器会根据这个订单号生成对应的平台订单号，请保证每笔订单传入的订单号的唯一性
            String memId = req.getParameter("mem_id"); //玩家ID
            String orderId = req.getParameter("order_id"); //平台订单号
            String orderStatus = req.getParameter("order_status"); //平台订单状态,2为平台订单支付成功，非2是支付异常
            String payTime = req.getParameter("pay_time"); //时间戳
            String productId = req.getParameter("product_id"); //商品ID
            String productName = req.getParameter("product_name"); //商品名
            String productPriceStr = req.getParameter("product_price"); //商品价格
            String sign = req.getParameter("sign"); //签名
            String ext = req.getParameter("ext"); //透传信息

            if (SysConfig.gameIdIOS != Integer.valueOf(appId)) {
                render(resp, "FAILURE");
                ServerLogger.warn("gameId无效,gameId=" + appId);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("app_id=").append(appId)
                    .append("&cp_order_id=").append(cpOrderId)
                    .append("&mem_id=").append(memId)
                    .append("&order_id=").append(orderId)
                    .append("&order_status=").append(orderStatus)
                    .append("&pay_time=").append(payTime)
                    .append("&product_id=").append(productId)
                    .append("&product_name=").append(URLEncoder.encode(productName, "UTF-8"))
                    .append("&product_price=").append(productPriceStr)
                    .append("&app_key=").append(SysConfig.gameKeyIOS);

            ServerLogger.warn(sb.toString() + "&sign=" + sign + "&ext=" + ext);

            float productPrice = Float.parseFloat(productPriceStr);
            int success = Integer.parseInt(orderStatus);
            if (productPrice <= 0 || StringUtils.isBlank(orderId) ||
                    StringUtils.isBlank(orderStatus) ||
                    StringUtils.isBlank(ext) ||
                    StringUtils.isBlank(sign) ||
                    StringUtils.isBlank(cpOrderId) ||
                    success != 2) {
                ServerLogger.warn("request param error");
                render(resp, "request param error");
                return;
            }

            String mySign = EncoderHandler.md5(sb.toString());
            if (!mySign.equals(sign)) {
                ServerLogger.warn("sign error", mySign);
                render(resp, "sign error.");
                return;
            }

            String[] arr = ext.split("_");
            int playerId = Integer.parseInt(arr[0]); //充值配置ID
//            ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, Integer.parseInt(arr[1]));
//            if (charge.us != productPrice) {
//                ServerLogger.warn("充值金额错误==>", "配置金额 = " + charge.us, ",充值金额 = " + productPrice);
//                return;
//            }
            //金额判断
            vipService.addCharge(playerId, Integer.parseInt(arr[1]), Long.parseLong(arr[2]), IOS_PAYMENT_TYPE, SysConfig.currency, orderId, SysConfig.serverId);
            render(resp, "SUCCESS");
        } catch (Exception e) {
            render(resp, "FAILURE");
            ServerLogger.err(e, "充值异常");
        }
    }
}
