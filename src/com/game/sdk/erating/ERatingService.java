package com.game.sdk.erating;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.IntParam;
import com.game.sdk.erating.consts.ERatingType;
import com.game.sdk.erating.domain.*;
import com.game.sdk.erating.domain.base.Report;
import com.game.sdk.net.HttpClient;
import com.game.sdk.utils.XmlParser;
import com.game.util.TimerService;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import io.netty.channel.Channel;
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
    private final ExecutorService executor = new ThreadPoolExecutor(2, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(120000), new DiscardPolicy());

    private boolean flag = true;

    @Override
    public void handleInit() {
        if(flag) {
            return;
        }
        GatewayReqInfos gwDatas = new GatewayReqInfos(ERatingType.CMD_GW_DATA_REPORT);
        GatewayInfo data = new GatewayInfo(ERatingType.ER_SERVER_START, 0);
        gwDatas.getGwData().add(data);
        sendReport(gwDatas, 0);

        timerService.scheduleAtFixedRate(new Runnable() { //每15s提交一次日志
            @Override
            public void run() {
                try {
                    GatewayReqInfos gwDatas = new GatewayReqInfos(ERatingType.CMD_GW_DATA_REPORT);
                    GatewayInfo data = new GatewayInfo(ERatingType.ER_ONLINE_COUNT, playerService.getPlayers().size());
                    gwDatas.getGwData().add(data);
                    sendReport(gwDatas, 0);
                } catch (Throwable e) {
                    ServerLogger.err(e, "玩家活动定时器异常");
                }
            }
        }, 120, 45, TimeUnit.SECONDS);
    }


    private void sendReport(final Report report, int playerId) {
        if(flag) {
            return;
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!SysConfig.debug) {
                        ServerLogger.warn("send report,xml data \r\n" + report.toProto());
                        String xmlData = HttpClient.sendPostRequest(report.toProto());
                        ServerLogger.warn("receive xml data \r\n" + xmlData);
                        int[] arr = XmlParser.cmdAndResultParser(xmlData);
                        if (arr[1] != 1) { //
                            //ServerLogger.warn("receive xml data exception \r\n" + xmlData);
                        }
                        if (arr[0] == ERatingType.CMD_CREATE_ROLE_RESP) { //创建角色，获取roleId
                            PlayerData data = playerService.getPlayerData(playerId);
                            int roleId = XmlParser.xmlCmdParser(xmlData, XmlParser.XML_BODY, XmlParser.FIELD_ROLE_ID);
                            data.setRoleId(roleId);
                            Player player = playerService.getPlayer(playerId);
                            reportRoleEnter(player, roleId);
                        }
                    }
                } catch (Throwable e) {
                    try {
                        ServerLogger.err(e, "日志上报异常 xml data \r\n" + report.toProto());
                    } catch (Exception e1) {
                        ServerLogger.warn("send report,xml data \r\n" + report.getCommand_id());
                    }
                }
            }
        });
    }

    private void sendReport(final Report report, String user, Channel channel) {
        if(flag) {
            return;
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!SysConfig.debug) { //帐号认s证获取userId
                        ServerLogger.warn("send report,xml data \r\n" + report.toProto());
                        String xmlData = HttpClient.sendPostRequest(report.toProto());
                        ServerLogger.warn("receive xml data \r\n" + xmlData);
                        int resultCode = XmlParser.xmlCmdParser(xmlData, XmlParser.XML_BODY, XmlParser.FIELD_RESULT_CODE);
                        IntParam param = new IntParam();
                        int userId = 0;

                        //登录失败
                        if (resultCode != 1) {
                            param.param = -4;
                            ServerLogger.warn("resultCode => " + resultCode);
                        } else {
                            userId = XmlParser.xmlCmdParser(xmlData, XmlParser.XML_BODY, XmlParser.FIELD_USER_ID);
                            param.param = userId;
                        }
                        //用户id为0
                        if (userId == 0) {
                            param.param = -4;
                            ServerLogger.warn("userId == 0 => " + xmlData);
                        }
                        SessionManager.sendDataInner(channel, 1012, param);
                    }
                } catch (Throwable e) {
                    try {
                        IntParam param = new IntParam();
                        param.param = -4;
                        SessionManager.sendDataInner(channel, 1012, param);
                        ServerLogger.err(e, "日志上报异常 xml data => " + report.toProto());
                    } catch (Exception e1) {
                        ServerLogger.warn("send report,xml data => " + report.getCommand_id());
                    }
                }
            }
        });
    }


    //登录认证
    public void reportAuthen(String user, String token, int clientIp, int clientPort, String clientMac
            , int clientType, String sdkVersion, Channel channel) {
        UserAuthenInfo data = new UserAuthenInfo(ERatingType.CMD_JOINT_AUTHEN_EX);
        data.setUn(user);
        data.setToken(token);
        data.setUserIP(clientIp);
        data.setPort(clientPort);
        data.setMac(clientMac);
        data.setClientType(clientType);
        data.setSdkVersion(sdkVersion);
        data.setUnixTime((int) (System.currentTimeMillis() / 1000));
        data.setAdid("" + 0);
        sendReport(data, user, channel);
    }

    /**
     * 创建角色
     *
     * @param player
     */
    public void reportCreateRole(Player player) {
        RoleInfo role = new RoleInfo(ERatingType.CMD_CREATE_ROLE);
        role.setUserId(player.userId);
        role.setRoleName(player.getName());
        role.setRoleGender(player.getSex());
        role.setRoleOccupation(player.getVocation());
        role.setInitialLevel(player.getLev());
        role.setUserIp(player.clientIp);
        role.setUserPort(player.clientPort);
        sendReport(role, player.getPlayerId());
    }

    //角色登录
    public void reportRoleEnter(Player player, int roleId) {
        RoleEnterInfo data = new RoleEnterInfo(ERatingType.CMD_ROLE_ENTER_GAME_EX5);
        data.setServerId(SysConfig.serverId);
        data.setUserId(player.userId);
        data.setRoleId(roleId);
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
        sendReport(data, player.getPlayerId());
    }


    /**
     * 角色登出日志
     *
     * @param player
     */
    public void reportRoleLogout(Player player, int roleId) {
        RoleLogoutInfo data = new RoleLogoutInfo(ERatingType.CMD_USER_LOGOUT);
        data.setUserId(player.userId);
        data.setRoleId(roleId);
        data.setLogoutFlag(1);
        data.setOccupationId(player.getVocation());
        data.setRoleLevel(player.getLev());
        data.setRatingId(SysConfig.gatewayId);
        data.setMoney1(player.getDiamond());
        data.setMoney2(0);
        data.setExperience(player.getExp());
        sendReport(data, player.getPlayerId());
    }


    public void reportMoneyCost(Player player, int roleId,
                                String name,
                                int count, int price,
                                int lkDiscountPrice,
                                int subjectId, int subAmout) {
        ItemDetailInfo data = new ItemDetailInfo(ERatingType.CMD_MONEY_COST);
        data.setDetailId(System.nanoTime() / 10);
        data.setUserId(player.userId);
        data.setRoleId(roleId);
        data.setRoleGender(player.getSex());
        data.setRoleLevel(player.getLev());
        data.setRatingId(SysConfig.gatewayId);
        data.setIbCode(name);
        data.setUserIp(player.clientIp);
        data.setPackageFlag(1);
        data.setCount(count);
        data.setDiscountPrice(lkDiscountPrice);
        data.setPayTime(System.currentTimeMillis() / 1000);
        data.setPrice(price);
        SubjectInfo info = new SubjectInfo(subjectId, subAmout);
        data.getInfoList().add(info);
        sendReport(data, player.getPlayerId());
    }


    public void reportAddMoney(Player player, int roleId,
                               int subjectId, int amount, String source) {
        MoneyInfo data = new MoneyInfo(ERatingType.CMD_MONEY_ADD);
        data.setDetailId(System.nanoTime() / 10);
        data.setUserId(player.userId);
        data.setRoleId(roleId);
        data.setAddTime(System.currentTimeMillis() / 1000);
        data.setSubjectId(subjectId);
        data.setSource(source);
        data.setAmount(amount);
        sendReport(data, player.getPlayerId());
    }

    public static void main(String[] args) throws Exception {
        SysConfig.init();
        UserAuthenInfo data = new UserAuthenInfo(ERatingType.CMD_JOINT_AUTHEN_EX);
        data.setUn("shulouxifeng");
        data.setToken("2523IrM686tR90mi");
        data.setUserIP(906511);
        data.setPort(52996);
        data.setMac("");
        data.setClientType(3);
        data.setSdkVersion("");
        data.setUnixTime((int) (System.currentTimeMillis() / 1000));
        data.setAdid("" + 0);
        //HttpClient.sendPostRequest(data.toProto());

        MoneyInfo data1 = new MoneyInfo(ERatingType.CMD_MONEY_ADD);
        data1.setDetailId(System.nanoTime() / 10);
        data1.setUserId(1);
        data1.setRoleId(1);
        data1.setAddTime(System.currentTimeMillis() / 1000);
        data1.setSubjectId(1);
        data1.setSource("111");
        data1.setAmount(1);
        System.out.println(data1.toProto());
    }
}
