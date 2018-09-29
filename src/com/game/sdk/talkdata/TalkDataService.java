package com.game.sdk.talkdata;

import com.game.SysConfig;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.util.JsonUtils;
import com.google.common.collect.Lists;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created by lucky on 2018/4/18.
 */
@Service
public class TalkDataService {

    @Autowired
    private PlayerService playerService;

    public String talkGameRecharge(int playerId, String orderId, float currencyAmount, double virtualCurrencyAmount, String currentType,int serverId,int itemId) {
        Player player = playerService.getPlayer(playerId);
        RechargeInfo info = new RechargeInfo();
        info.setMsgID(System.nanoTime() + "");
        info.setStatus("success");
        String os = player.clientType == 1 ? "ios" : "android";
        info.setOS(os);
        info.setAccountID(playerId + "");
        info.setOrderID(serverId + "-" + orderId);
        info.setCurrencyAmount(currencyAmount);
        info.setCurrencyType(currentType);
        info.setVirtualCurrencyAmount(virtualCurrencyAmount);
        info.setChargeTime(System.currentTimeMillis() / 1000);
        info.setGameServer(serverId + "");
        info.setLevel(player.getLev());
        if(SysConfig.currency.equals("CNY")){
            info.setPartner("Ingcle");
        }else{
            info.setPartner("GooglePlay--Singapore Malaysia");
        }
        info.setGameVersion("1.0");
        info.setMission("recharge");
        byte[] dataByte = gzip(JsonUtils.object2String(Lists.newArrayList(info)));
        String result = "";
        try {
            ServerLogger.warn("talkingdata report:rechargeId="+itemId+" 金额="+currencyAmount);
            HttpClient client = new HttpClient("api.talkinggame.com", "80", "/api/charge/CCD1697D3DE34B499BC53EBE92844B1E");
            result = client.doPost(dataByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        TalkDataService talkDataService = new TalkDataService();
        talkDataService.talkGameRecharge(1, "1232", 100, 100, "CNY",SysConfig.serverId,1);
    }

    /**
     * 將字符串压缩为 gzip 流 * @param content
     *
     * @return
     */
    private static byte[] gzip(String content) {
        ByteArrayOutputStream baos = null;
        GZIPOutputStream out = null;
        byte[] ret = null;
        try {
            baos = new ByteArrayOutputStream();
            out = new GZIPOutputStream(baos);
            out.write(content.getBytes());
            out.close();
            baos.close();
            ret = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
}
