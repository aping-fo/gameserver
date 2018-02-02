package com.game.sdk.erating;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.sdk.erating.consts.ERatingType;
import com.game.sdk.erating.domain.GwData;
import com.game.sdk.erating.domain.GwDatas;
import com.game.sdk.erating.domain.Report;
import com.game.sdk.erating.domain.Role;
import com.game.sdk.net.HttpClient;
import com.game.util.TimerService;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2018/1/31.
 */
@Service
public class ERatingService implements InitHandler {
    @Autowired
    private TimerService timerService;

    ExecutorService executor = new ThreadPoolExecutor(2, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(12000), new DiscardPolicy());


    @Override
    public void handleInit() {
        GwDatas gwDatas = new GwDatas(ERatingType.CMD_GW_DATA_REPORT, SysConfig.gameId, SysConfig.gatewayId);
        GwData data = new GwData(ERatingType.ER_SERVER_START, 0);
        gwDatas.getGwData().add(data);
        //sendReport(gwDatas);

        timerService.scheduleAtFixedRate(new Runnable() { //每5分钟提交一次日志
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    ServerLogger.err(e, "玩家活动定时器异常");
                }
            }
        }, 120, 15, TimeUnit.MINUTES);
    }


    public void sendReport(final Report report) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient.sendPostRequest(report.toProto());
                } catch (Exception e) {
                    ServerLogger.err(e, "日志上报异常 cmd = " + report.getCommandId());
                }
            }
        });
    }

    /**
     * 上报开服日志
     */
    public void reportStart() {
        executor.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public void reportStop() {
        executor.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void reportGateWayData() {
        executor.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public static void main(String[] args) throws Exception {
        SysConfig.init();
        GwDatas gwDatas = new GwDatas(ERatingType.CMD_GW_DATA_REPORT, SysConfig.gameId, SysConfig.gatewayId);
        GwData data = new GwData(ERatingType.ER_SERVER_START, 0);
        gwDatas.getGwData().add(data);
        data = new GwData(ERatingType.ER_SERVER_START, 1);
        gwDatas.getGwData().add(data);
        HttpClient.sendPostRequest(gwDatas.toProto());
    }
}
