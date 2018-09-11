package com.game.sdk.ingcle;

import com.game.SysConfig;
import com.game.params.IntParam;
import com.game.sdk.net.HttpClient;
import com.game.sdk.utils.EncoderHandler;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2018/9/6.
 */
@Service
public class IngcleService {
    private final ExecutorService executor = new ThreadPoolExecutor(2, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(120000), new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 登录验证
     *
     * @param playerId
     * @param token
     * @return
     */
    public void loginVerify(int playerId, String token,Channel channel) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = Maps.newHashMapWithExpectedSize(5);
                params.put("channel_id", "1");
                params.put("game_id", "1");
                params.put("player_id", "" + playerId);
                params.put("player_token", token);


                StringBuilder sb = new StringBuilder();
                sb.append("channel_id=").append(SysConfig.channel)
                        .append("game_id").append(SysConfig.gameId)
                        .append("player_id").append(playerId)
                        .append("game_key").append(SysConfig.gameKey)
                        .append("player_token").append(token);
                String sign = EncoderHandler.md5(sb.toString());
                params.put("sign", sign);
                try {
                    String json = HttpClient.sendPostRequest(SysConfig.ingcleLogin, params);
                    ServerLogger.info(json);
                    IntParam param = new IntParam();

                    SessionManager.sendDataInner(channel, 1012, param);

                } catch (Exception e) {
                    ServerLogger.err(e, "login fail");
                }
            }
        });
    }

    public static void main(String[] args) {
        Map<String, String> params = Maps.newHashMapWithExpectedSize(5);
        params.put("channel_id", "1");
        params.put("game_id", "1");
        params.put("player_id", "" + 1);
        params.put("player_token", "1");


        StringBuilder sb = new StringBuilder();
        sb.append("channel_id=").append(1123)
                .append("game_id").append(260)
                .append("player_id").append(1)
                .append("game_key").append("261391694a2e825eaa100d2d797429e3")
                .append("player_token").append(1);
        String sign = EncoderHandler.md5(sb.toString());
        params.put("sign", sign);
        try {
            String json = HttpClient.sendPostRequest("http://sdkapi.ingcle.cn/cp/cp/check", params);
            System.out.println(json);


        } catch (Exception e) {
            ServerLogger.err(e, "login fail");
        }
    }
}
