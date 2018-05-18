package com.game.sdk.service;

import com.game.data.ChargeConfig;
import com.game.module.vip.VipService;
import com.game.sdk.erating.consts.ERatingType;
import com.game.sdk.erating.domain.CommonRespInfo;
import com.game.sdk.erating.domain.RechargeInfo;
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
        CommonRespInfo result = new CommonRespInfo(ERatingType.CMD_CHARGE_RESP);
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

            if (cmd == 0) {
                result.setCommand_id(ERatingType.CMD_BIND_RESP);
                result.setResultCode(ERatingType.ErrorCode.E_ERROR);
                ServerLogger.warn("resp xml data =>" + result.toProto());
                resp.getWriter().write(result.toProto());
                return;
            }

            ServerLogger.warn("command id =>" + cmd);
            if (cmd == ERatingType.CMD_BIND) {
                result.setCommand_id(ERatingType.CMD_BIND_RESP);
                result.setResultCode(ERatingType.ErrorCode.S_SUCCESS);
                ServerLogger.warn("resp xml data =>" + result.toProto());
                resp.getWriter().write(result.toProto());
                return;
            }

            ServerLogger.warn("callback param = " + content);
            RechargeInfo data = new RechargeInfo();
            XmlParser.xmlParser(content, data);
            if (orders.contains(data.getDetail_id())) {
                result.setResultCode(ERatingType.ErrorCode.E_CHARGE_DUPLICATE);
                resp.getWriter().write(result.toProto());
                ServerLogger.warn("order duplicate,order sn = " + data.getDetail_id());
                return;
            }

            ServerLogger.warn("order info = " + data.toString());
            ServerLogger.warn("Attach code =>" + data.getAttach_code());
            String[] arr = data.getAttach_code().split("_");
            int playerId = Integer.parseInt(arr[0]); //角色ID
            int id = Integer.parseInt(arr[1]); //充值配置ID
            int cpId = Integer.parseInt(arr[2]);
            String paymentType = arr[3];
            String currentType = arr[4];
            ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, id);
            int amount = data.getAmount() / 10;
            if (charge.rmb != amount) {
                result.setResultCode(ERatingType.ErrorCode.S_SUCCESS);
                ServerLogger.warn("error,resp xml data =>" + result.toProto());
                resp.getWriter().write(result.toProto());
                return;
            }
            //金额判断
            vipService.addCharge(playerId, id, cpId, paymentType, currentType, data.getDetail_id() + "");
            result.setResultCode(1);
            orders.add(String.valueOf(data.getDetail_id()));
            ServerLogger.warn("resp xml data =>" + result.toProto());
            result.setResultCode(ERatingType.ErrorCode.S_SUCCESS);
            resp.getWriter().write(result.toProto());
            resp.getWriter().flush();
        } catch (Exception e) {
            result.setResultCode(ERatingType.ErrorCode.E_PARAMETER_ERROR);
            ServerLogger.warn("resp xml data =>" + result.toProto());
            resp.getWriter().write(result.toProto());
            ServerLogger.err(e, "充值异常");
        }
    }
}
