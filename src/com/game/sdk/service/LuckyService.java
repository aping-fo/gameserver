package com.game.sdk.service;

import com.game.SysConfig;
import com.game.data.ChargeConfig;
import com.game.module.vip.VipService;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.google.common.collect.Maps;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lucky on 2018/2/27.
 * 乐起SDK充值回调
 */
@Service("lucky")
public class LuckyService implements SdkService {
    @Autowired
    private VipService vipService;
    private Set<String> orders = new HashSet<>();

    public void recharge(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String orderSn = req.getParameter("order_sn"); //游久订单号
            String uid = req.getParameter("uid"); //平台用户ID
            String status = req.getParameter("status"); //订单状态
            String amount = req.getParameter("amount"); //实际到付金额
            String currency = req.getParameter("currency"); //货币代码
            String targetamount = req.getParameter("targetamount"); //货币代码
            String product_id = req.getParameter("product_id"); //商品ID
            String platment = req.getParameter("platment"); //充值平台
            String game_area = req.getParameter("game_area"); //游戏分区或游戏服编号，实际就是客户端传入的 serverId
            String apply_time = req.getParameter("apply_time"); //支付时间，类似2014-05-12 12:01:01（北京时间）
            String exts = req.getParameter("exts"); //扩展信息

            StringBuilder sb = new StringBuilder();
            sb.append("order_sn = ").append(orderSn).append(",")
                    .append("uid = ").append(uid).append(",")
                    .append("status = ").append(status).append(",")
                    .append("amount = ").append(amount).append(",")
                    .append("currency = ").append(currency).append(",")
                    .append("targetamount = ").append(targetamount).append(",")
                    .append("product_id = ").append(product_id).append(",")
                    .append("platment = ").append(platment).append(",")
                    .append("game_area = ").append(game_area).append(",")
                    .append("apply_time = ").append(apply_time).append(",")
                    .append("exts = ").append(exts);
            ServerLogger.warn(sb.toString());

            if (orders.contains(orderSn)) {
                render(resp, "error");
                ServerLogger.warn("改订单已处理");
            }

            float rmb = Float.parseFloat(amount);
            String[] arr = exts.split("_");
            int playerId = Integer.parseInt(arr[0]); //角色ID
            int id = Integer.parseInt(arr[1]); //充值配置ID
            int cpId = Integer.parseInt(arr[2]);
            ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, id);
            if (charge.rmb != rmb) {
                ServerLogger.warn("充值金额错误==>", "配置金额 = " + charge.rmb, ",充值金额 = " + rmb);
                return;
            }
            //金额判断
            vipService.addCharge(playerId, id, cpId, platment, currency, orderSn, SysConfig.serverId);

            Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(2);
            resultParams.put("code", 0);
            resultParams.put("message", "success");
            render(resp, JsonUtils.map2String(resultParams));
            //处理成功，移除
            orders.add(orderSn);
        } catch (Exception e) {
            render(resp, "error");
            ServerLogger.err(e, "充值异常");
        }
    }

    public static void main(String[] args) {
        Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(2);
        resultParams.put("code", 0);
        resultParams.put("message", "success");
        System.out.println(JsonUtils.map2String(resultParams));
    }
}
