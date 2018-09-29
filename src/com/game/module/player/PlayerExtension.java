package com.game.module.player;

import com.game.SysConfig;
import com.game.data.Response;
import com.game.event.DefaultLogoutHandler;
import com.game.event.LoginHandler;
import com.game.module.activity.ActivityConsts;
import com.game.module.admin.ManagerService;
import com.game.module.admin.UserManager;
import com.game.module.fashion.FashionService;
import com.game.module.gang.Gang;
import com.game.module.gang.GangService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.StringParam;
import com.game.params.player.*;
import com.game.sdk.erating.ERatingService;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;
import com.game.util.HttpRequestUtil;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.anotation.UnLogin;
import com.server.util.ServerLogger;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Extension
public class PlayerExtension {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private DefaultLogoutHandler logoutHandler;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private LoginHandler loginHandler;
    @Autowired
    private GangService gangService;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private ERatingService ratingService;
    @Autowired
    private PlayerCalculator playerCalculator;

    private static final AttributeKey<String> CHANNEL = AttributeKey.valueOf("channel");

    @UnLogin
    @Command(1001)
    public Object getRoleList(int playerId, CRegRoleListVo param, Channel channel) {
        if (SessionManager.getInstance().getOnlineCount() > SysConfig.maxCon) {
            IntParam result = new IntParam();
            SessionManager.sendDataInner(channel, 1010, result);
            channel.close();
            return null;
        }

        List<Player> roleList = playerService.getPlayersByAccName(param.userId);
        ServerLogger.info("获取角色列表");
        channel.attr(CHANNEL).set(param.userId);

        ListParam<SRoleVo> vo = new ListParam<SRoleVo>();
        vo.params = new ArrayList<SRoleVo>(roleList.size());

        for (Player player : roleList) {
            fashionService.checkRemoveTimeoutFashions(player.getPlayerId(), false);

            SRoleVo role = new SRoleVo();
            role.attack = player.getAttack();
            role.playerId = player.getPlayerId();
            role.crit = player.getCrit();
            role.defense = player.getDefense();
            role.exp = player.getExp();
            role.fashionId = player.getFashionId();
            role.fu = player.getFu();
            role.hp = player.getHp();
            role.lastLoginTime = player.getLastLoginTime() == null ? 0 : player.getLastLoginTime().getTime();
            role.level = player.getLev();
            role.name = player.getName();
            role.sex = player.getSex();
            role.symptom = player.getSymptom();
            role.title = player.getTitle();
            role.vip = player.getVip();
            role.vocation = player.getVocation();
            role.weapon = player.getWeaponId();
            if (player.getGangId() > 0) {
                Gang gang = gangService.getGang(player.getGangId());
                if (gang == null) {
                    player.setGangId(0);
                } else {
                    role.gang = gang.getName();
                }
            }
            role.head = playerService.getPlayerData(player.getPlayerId()).getCurHead();
            vo.params.add(role);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequestUtil.sendGet(SysConfig.gmServerUrl + "/admin/accountLog", "serverId=" + SysConfig.serverId + "&accountName=" + param.userId);
            }
        }).start();

        return vo;
    }

    @UnLogin
    @Command(1002)
    public Object createRole(int playerId, CRegVo param, Channel channel) {
        // 连接数太多
        RegResultVo result = new RegResultVo();
        if (SessionManager.getInstance().getOnlineCount() > SysConfig.maxCon) {
            result.code = Response.TOO_MANY_CON;
            return result;
        }
        // 关闭注册
        if (!SysConfig.reg) {
            result.code = Response.CLOSE_REG;
            return result;
        }
        // 版本检测
        if (!playerService.checkVersion(param.version)) {
            result.code = Response.LOW_VERSION;
            return result;
        }

        // 登录验证
        int auth = playerService.auth();
        if (auth != 0) {
            result.code = auth;
            return result;
        }
        // 参数错误
        if (!playerService.checkRegParam(param)) {
            result.code = Response.ERR_PARAM;
            return result;
        }
        // 同名
        if (playerService.getPlayerIdByName(param.name) > 0) {
            result.code = Response.SAME_NAME;
            return result;
        }
        // 角色数量上限
        if (playerService.getPlayersByAccName(param.accName).size() >= ConfigData.globalParam().maxRoleCount) {
            result.code = Response.TOO_MANY_ROLE;
            return result;
        }
        Player player = playerService.addNewPlayer(param.name, param.sex, param.vocation, param.accName, param.channel, param.serverId, param.serverName, param.userId, param.thirdChannel, param.thirdUserId);
        if (player == null) {
            result.code = Response.SAME_NAME;
            return result;
        }

        CLoginVo loginVo = new CLoginVo();
        loginVo.playerId = player.getPlayerId();
        loginVo.version = ConfigData.globalParam().version;// 版本
        loginVo.clientMac = param.clientMac;
        loginVo.clientType = param.clientType;
        loginVo.hardwarSn1 = param.hardwarSn1;
        loginVo.hardwarSn2 = param.hardwarSn2;
        loginVo.modelVersion = param.modelVersion;
        loginVo.ldid = param.ldid;
        loginVo.uddi = param.uddi;
        loginVo.un = param.un;

        result.serverId = String.valueOf(param.serverId);
        result.serverName = param.serverName;
        result.userName = param.accName;
        result.roleId = String.valueOf(player.getPlayerId());
        result.roleName = player.getName();
        result.createTime = String.valueOf(player.getRegTime().getTime() / 1000);
        result.roleCareer = player.getVocation();
        result.gatewayId = SysConfig.gatewayId;

        String host = channel.remoteAddress().toString();
        String[] arr = host.split(":");
        player.clientPort = Integer.parseInt(arr[1]);
        String[] hostArr = arr[0].substring(1).split("\\.");
//        player.clientIp = Integer.parseInt(hostArr[0]) * 2563 + Integer.parseInt(hostArr[1]) * 2562 + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]);
        player.clientIp = (int) (Math.pow(Integer.parseInt(hostArr[0]) * 256, 3) + Math.pow(Integer.parseInt(hostArr[1]) * 256, 2) + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]));
        //player.userId = param.userId;


        // 直接返回登录结果
        loginVo.userId = player.userId;
        PlayerVo loginResult = login(0, loginVo, channel);
        SessionManager.sendDataInner(channel, 1003, loginResult);

        //report create log
        ratingService.reportCreateRole(player);
        player.bCrateRole = true;
        return result;
    }

    @UnLogin
    @Command(1003)
    public PlayerVo login(int id, CLoginVo param, Channel channel) {
        PlayerVo result = new PlayerVo();
        if (param.playerId == 0) {

            result.code = Response.ERR_PARAM;
            return result;
        }
        // 版本检测
        if (!playerService.checkVersion(param.version)) {
            result.code = Response.LOW_VERSION;
            return result;
        }

        // 连接数太多
        if (SessionManager.getInstance().getOnlineCount() > SysConfig.maxCon) {
            result.code = Response.TOO_MANY_CON;
            return result;
        }
        UserManager ban = managerService.checkBanInfo(param.playerId);
        if (ban != null && ban.getBanLogin() > 0) {
            result.code = Response.BAN_LOGIN;
            return result;
        }
        // 登录验证
        int auth = playerService.auth();
        if (auth != 0) {
            result.code = auth;
            return result;
        }
        // 踢走旧的登录
        int playerId = param.playerId;
        Player player = playerService.getPlayer(playerId);
        String accName = channel.attr(CHANNEL).get();
        if (accName == null || !accName.equals(player.getAccName())) {
            result.code = Response.ERR_PARAM;
            return result;
        }
        //final Channel oldChannel = SessionManager.getInstance().getChannel(playerId);
        final User user = playerService.getOldAndCache(accName, playerId, channel);
        if (user != null) {
            ServerLogger.debug("duplicated login:", user.playerId);
            SessionManager.getInstance().removePlayerAttr(user.channel);
            IntParam param1 = new IntParam();
            param1.param = Response.RE_LOGIN;
            SessionManager.sendDataInner(user.channel, 1015, param1);
            user.channel.close();
            logoutHandler.logout(user.playerId);
        }

        player.setRefresh(false);
        player.setSubLine(0);
        SessionManager.getInstance().addSession(playerId, channel);
        // 第一次登录
        if (player.getLastLoginTime() == null) {
            playerService.handleFirstLogin(playerId);
            playerService.addLoginCount(playerId);//增加每日登录人数
        }
        player.setLastLoginTime(new Date());
        player.setLastLogoutTime(new Date());
        player.onlineTime = System.currentTimeMillis();

        player.setIp(CommonUtil.getIp(channel.remoteAddress()));
        // 处理登录
        playerService.handleLogin(playerId);
        //其它子系统的登录处理
        loginHandler.playerLogin(playerId);

        //刷新属性
        playerCalculator.calculate(playerId);

        result = playerService.toSLoginVo(playerId);
        if (ban != null && ban.getBanChat() > 0) {
            result.banChat = true;
        }
        PlayerData data = playerService.getPlayerData(playerId);
        result.userName = accName;
        result.serverName = data.getServerName();
        result.serverId = player.getServerId();
        player.setRefresh(true);

        player.clientType = param.clientType == 0 ? 3 : param.clientType;
        player.clientMac = param.clientMac == null ? "" : param.clientMac;
        player.hardwarSn1 = param.hardwarSn1 == null ? "" : param.hardwarSn1;
        player.hardwarSn2 = param.hardwarSn2 == null ? "" : param.hardwarSn2;
        player.uddi = param.uddi == null ? "" : param.uddi;
        player.modelVersion = param.modelVersion == null ? "" : param.modelVersion;
        player.ldid = param.ldid == null ? "" : param.ldid;
        player.adid = "1";
        player.token = param.token;
        player.userId = param.userId;

        try {
            String host = channel.remoteAddress().toString();
            String[] arr = host.split(":");
            player.clientPort = Integer.parseInt(arr[1]);
            String[] hostArr = arr[0].substring(1).split("\\.");
//            player.clientIp = Integer.parseInt(hostArr[0]) * 2563 + Integer.parseInt(hostArr[1]) * 2562 + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]);
            player.clientIp = (int) (Math.pow(Integer.parseInt(hostArr[0]) * 256, 3) + Math.pow(Integer.parseInt(hostArr[1]) * 256, 2) + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]));
        } catch (Exception e) {
            ServerLogger.err(e, "ip 解析失败");
        }

        if (!player.bCrateRole) {
            ratingService.reportRoleEnter(player, data.getRoleId());
        }
        // 设置session等级
        SessionManager.getInstance().setPlayerLev(playerId, player.getLev());
        ServerLogger.info("user login:" + playerId + " 设备mac:" + param.clientMac);
        //System.out.println("=============" + playerService.getPlayers().size());
        return result;
    }

    // 更新玩家属性
    public static final int REFRESH_MY_VO = 1004;

    // 属性更新n条属性
    public static final int UPDATE_ATTR = 1005;

    //更新货币
    public static final int UPDATE_CURRENCY = 1006;

    @Command(1007)
    public Object getOtherPlayer(int playerId, IntParam param) {
        return playerService.toSLoginVo(param.param);
    }

    @Command(1008)
    public Object openModule(int playerId, IntParam param) {
        return playerService.moduleOpen(playerId, param.param);
    }

    @Command(1013)
    public Object hitModule(int playerId, Int2Param type) {
        return playerService.hitModule(playerId, type.param1, type.param2);
    }

    @Command(1014)
    public Object getModules(int playerId, Object param) {
        return playerService.getModule(playerId);
    }

    @Command(10502)
    public Object actionModule(int playerId, IntParam param) {
        playerService.actionModule(playerId, param.param);
        return null;
    }

    @Command(10501)
    public Object getActionModule(int playerId, Object param) {
        return playerService.getActionModule(playerId);
    }

    @Command(1009)
    public Object newHandleStep(int playerId, IntParam param) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        playerData.getGuideSteps().add(param.param);
        IntParam intParam = new IntParam();
        intParam.param = param.param;
        return intParam;
    }

    @Command(1011)
    public Object selectRole(int playerId, Object param) {
        playerService.selectRole(playerId);
        return null;
    }

    @UnLogin
    @Command(1012)
    public Object userAuth(int playerId, UserAuth param, Channel channel) {
        String host = channel.remoteAddress().toString();
        String[] arr = host.split(":");
        int clientPort = Integer.parseInt(arr[1]);
        String hostIp = arr[0].substring(1).trim();
        Date date = new Date();
        if (date.before(SysConfig.openDate)) { //还没到开服时间
            ServerLogger.warn("client ip = " + hostIp);
            if (!ConfigData.accountSet.contains(hostIp)) { //非白名单
                ServerLogger.warn("client ip ---------- " + hostIp);
                IntParam param1 = new IntParam();
                param1.param = -5;
                return param1;
            }
        }
        String[] hostArr = hostIp.split("\\.");
//        int clientIp = Integer.parseInt(hostArr[0]) * 2563 + Integer.parseInt(hostArr[1]) * 2562 + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]);
        int clientIp = (int) (Math.pow(Integer.parseInt(hostArr[0]) * 256, 3) + Math.pow(Integer.parseInt(hostArr[1]) * 256, 2) + Integer.parseInt(hostArr[2]) * 256 + Integer.parseInt(hostArr[3]));
        ratingService.reportAuthen(param.un, param.token, clientIp, clientPort, param.clientMac, param.clientType, param.modelVersion, channel);
        return null;
    }

    @Command(10101)
    public Object feedback(int playerId, StringParam param) {
        IntParam result = new IntParam();
        result.param = Response.SUCCESS;
        return result;
    }

    @Command(1016)
    public Object updateName(int playerId, StringParam stringParam) {
        IntParam result = new IntParam();
        String name = stringParam.param;
        // 同名
        if (playerService.getPlayerIdByName(name) > 0) {
            result.param = Response.SAME_NAME;
            return result;
        }

        playerService.updatePlayerName(playerId, name);

        return result;
    }

    //踢人下线
    public static final int FORCE_LOGOUT = 1017;
}
