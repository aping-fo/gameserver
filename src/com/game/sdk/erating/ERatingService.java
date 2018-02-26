package com.game.sdk.erating;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.sdk.erating.consts.ERatingType;
import com.game.sdk.erating.domain.*;
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
    @Autowired
    private PlayerService playerService;

    ExecutorService executor = new ThreadPoolExecutor(2, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(12000), new DiscardPolicy());


    @Override
    public void handleInit() {
        GwDatas gwDatas = new GwDatas(ERatingType.CMD_GW_DATA_REPORT);
        GwData data = new GwData(ERatingType.ER_SERVER_START, 0);
        gwDatas.getGwData().add(data);
        sendReport(gwDatas);

        timerService.scheduleAtFixedRate(new Runnable() { //每5分钟提交一次日志
            @Override
            public void run() {
                try {
                    GwDatas gwDatas = new GwDatas(ERatingType.CMD_GW_DATA_REPORT);
                    GwData data = new GwData(ERatingType.ER_ONLINE_COUNT, playerService.getPlayers().size());
                    gwDatas.getGwData().add(data);
                    sendReport(gwDatas);
                } catch (Exception e) {
                    ServerLogger.err(e, "玩家活动定时器异常");
                }
            }
        }, 120, 15, TimeUnit.MINUTES);
    }


    private void sendReport(final Report report) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //HttpClient.sendPostRequest(report.toProto());
                } catch (Exception e) {
                    ServerLogger.err(e, "日志上报异常 cmd = " + report.getCommandId());
                }
            }
        });
    }

    /**
     * 创建角色
     *
     * @param player
     */
    public void reportCreateRole(Player player) {
        RoleData role = new RoleData(ERatingType.CMD_CREATE_ROLE);
        role.setUserId(player.getPlayerId());
        role.setRoleName(player.getName());
        role.setRoleGender(player.getSex());
        role.setRoleOccupation(player.getVocation());
        role.setInitialLevel(player.getLev());

        sendReport(role);
    }

    public void reportRoleEnter(Player player) {
        RoleEnterData data = new RoleEnterData(ERatingType.CMD_ROLE_ENTER_GAME_EX5);
        data.setServerId(SysConfig.serverId);
        data.setUserId(player.getPlayerId());
        data.setRoleId(player.getPlayerId());
        data.setLevel(player.getLev());
        data.setGender(player.getSex());
        data.setOccupationId(player.getVocation());
        data.setClientIp(player.clientIp);
        data.setClientType(player.clientType);
        data.setClientPort(player.clientPort);
        data.setClientMac(player.clientMac);
        data.setHardwareSn1(player.hardwarSn1);
        data.setHardwareSn2(player.hardwarSn2);
        data.setUddi(player.uddi);
        data.setLdid(player.ldid);
        data.setModelVersion(player.modelVersion);

        sendReport(data);
        reportAuthen(player);
    }

    public void reportAuthen(Player player) {
        UserAuthenData data = new UserAuthenData(ERatingType.CMD_JOINT_AUTHEN_EX);
        data.setUn(player.openId);
        data.setToken(player.token);
        data.setUserIP(player.clientIp);
        data.setPort(player.clientPort);
        data.setMac(player.clientMac);
        data.setClientType(player.clientType);
        data.setSdkVersion(player.modelVersion);
        data.setUnixTime((int)(System.currentTimeMillis() / 1000));
        data.setCpId(SysConfig.gameId);
        data.setAdid(player.adid);
        data.setUid(SysConfig.gameId+"");
        sendReport(data);
    }

    /**
     * 角色登出日志
     *
     * @param player
     */
    public void reportRoleLogout(Player player) {
        RoleLogoutData data = new RoleLogoutData(ERatingType.CMD_USER_LOGOUT);
        data.setUserId(player.getPlayerId());
        data.setRoleId(player.getPlayerId());
        data.setLogoutFlag(1);
        data.setOccupationId(player.getVocation());
        data.setRoleLevel(player.getLev());
        data.setRatingId(ERatingType.CMD_USER_LOGOUT);
        data.setMoney1(player.getDiamond());
        data.setMoney2(0);
        data.setExperience(player.getExp());
        sendReport(data);
    }

    public static void main(String[] args) throws Exception {
        SysConfig.init();
        GwDatas gwDatas = new GwDatas(ERatingType.CMD_GW_DATA_REPORT);
        GwData data = new GwData(ERatingType.ER_SERVER_START, 0);
        gwDatas.getGwData().add(data);
        data = new GwData(ERatingType.ER_SERVER_START, 1);
        gwDatas.getGwData().add(data);
        HttpClient.sendPostRequest(gwDatas.toProto());
    }
}
