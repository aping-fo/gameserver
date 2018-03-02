package com.game.sdk.service;

import com.game.data.ChargeConfig;
import com.game.module.vip.VipService;
import com.game.sdk.erating.consts.ERatingType;
import com.game.sdk.erating.domain.CommonRespData;
import com.game.sdk.erating.domain.RechargeData;
import com.game.sdk.utils.XmlParser;
import com.game.util.ConfigData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lucky on 2018/2/27.
 * 蓝港回调
 */
@Service("lg")
public class LgService implements SdkService {
    @Autowired
    private VipService vipService;
    private Set<String> orders = new HashSet<>();

    public void recharge(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.addHeader("Pragma", "no-cache");
        resp.addHeader("Accept", "*/*");
        CommonRespData result = new CommonRespData(ERatingType.CMD_CHARGE_RESP);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"))) {
            StringBuilder stringBuilder = new StringBuilder();
            String ln;
            while ((ln = reader.readLine()) != null) {
                stringBuilder.append(ln);
                stringBuilder.append("\r\n");
            }
            String content = stringBuilder.toString();
            ServerLogger.warn("\r\n" + content);
            int cmd = XmlParser.xmlCmdParser(content, XmlParser.XML_HEAD, XmlParser.FIELD_CMD);

            ServerLogger.warn("command id =>" + cmd);
            if (cmd == ERatingType.CMD_BIND) {
                result.setResultCode(ERatingType.CMD_BIND_RESP);
                result.setResultCode(ERatingType.ErrorCode.S_SUCCESS);
                ServerLogger.warn("resp xml data =>" + result.toProto());
                resp.getWriter().write(result.toProto());
                return;
            }

            RechargeData data = new RechargeData();
            XmlParser.xmlParser(content, data);
            if (orders.contains(data.getDetail_id())) {
                result.setResultCode(ERatingType.ErrorCode.E_CHARGE_DUPLICATE);
                resp.getWriter().write(result.toProto());
                ServerLogger.warn("order duplicate,order sn = " + data.getDetail_id());
                return;
            }

            String[] arr = data.getAttach_code().split("_");
            int playerId = Integer.parseInt(arr[0]);
            int id = Integer.parseInt(arr[1]);
            int count = Integer.parseInt(arr[2]);
            ServerLogger.warn("Attach code =>" + data.getAttach_code());
            ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, id);
            if (charge.rmb != data.getAmount()) {
                result.setResultCode(ERatingType.ErrorCode.E_PARAMETER_ERROR);
                ServerLogger.warn("resp xml data =>" + result.toProto());
                resp.getWriter().write(result.toProto());
                return;
            }
            //金额判断
            vipService.addCharge(playerId, id, count);
            result.setResultCode(1);
            orders.add(String.valueOf(data.getDetail_id()));
            ServerLogger.warn("resp xml data =>" + result.toProto());
            result.setResultCode(ERatingType.ErrorCode.S_SUCCESS);
            resp.getWriter().write(result.toProto());
        } catch (Exception e) {
            result.setResultCode(ERatingType.ErrorCode.E_PARAMETER_ERROR);
            ServerLogger.warn("resp xml data =>" + result.toProto());
            resp.getWriter().write(result.toProto());
            ServerLogger.err(e, "充值异常");
        }
    }
}
