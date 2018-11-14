package com.game.module.admin;

import com.game.SysConfig;
import com.game.data.ErrCode;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.chat.ChatExtension;
import com.game.module.friend.FriendService;
import com.game.module.gang.GangService;
import com.game.module.giftbag.ActivationCode;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.merge.MergeService;
import com.game.module.player.*;
import com.game.module.vip.VipService;
import com.game.params.IntParam;
import com.game.params.StringParam;
import com.game.params.player.PlayerVo;
import com.game.util.*;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import com.sun.net.httpserver.Authenticator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService implements InitHandler {

    public static final String RETURN_PARAM_ERROR = "param_error";// 参数错误
    public static final String RETURN_FAILED = "failed";// 发送消息失败，服务器异常
    public static final String RETURN_SUCCESS = "success";// 成功
    public static final String PLAYERID_NOT_EXISTENCE = "playerid_not_existence";// 玩家id不存在
    public static final String GOOD_NOT_EXISTENCE = "good_not_existence";// 物品不存在

    public static final String OK = "ok";
    public static final String REPEAT = "repeat";
    public static final String BAN = "ban";
    public static final String FAIL = "fail";

    public static final int BAN_CHAT = 1;//禁言
    public static final int BAN_LOGIN = 2;//封号
    public static final int BAN_IP = 3;//禁ip
    public static final int BAN_IMEI = 4;//禁IMEI
    public static final int KICK_LINE = 5;//踢下线

    public static final int BAN_PROHIBITION = 1;//封禁
    public static final int BAN_UNSEAL = 2;//解封

    @Autowired
    private PlayerService playerService;
    @Autowired
    private ManagerDao managerDao;
    @Autowired
    private MailService mailService;
    @Autowired
    private VipService vipsService;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private MergeService mergeService;

    private Map<String, ProhibitionEntity> bans;

    @Override
    public void handleInit() {
        bans = new ConcurrentHashMap<>();
        List<ProhibitionEntity> prohibitionEntityList = Context.getLoggerService().allProhibition();
        if (prohibitionEntityList != null && !prohibitionEntityList.isEmpty()) {
            for (ProhibitionEntity o : prohibitionEntityList) {
                bans.put(o.getClosureAccount(), o);
            }
        }
    }

    // //////////服务器转发/////////////////////////////////////////////////////////////

    /**
     * 后台发过来的http格式：action=aaa&p1=11&p2=11 （具体跟后台系统的同事约定参数名） 返回 0 失败，返回1 成功
     * 根据不同的action，处理后返回结果String 一个action，写一个函数处理返回结果
     */
    public String handle(Map<String, String> params) throws SecurityException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        String action = params.get("act");
        if ("xx".equals(action))
            return "xx";
        Method method;
        try {
            method = this.getClass().getMethod(
                    String.format("%s_%s", "handle", action), Map.class);
            Object result = method.invoke(this, params);
            if (result != null)
                return (String) result;
        } catch (Exception e) {
            ServerLogger.err(e, "handle manager err!");
            return "command format err!";
        }
        return "command format err!";
    }

    /**
     * 充值
     */
    public String handle_pay(Map<String, String> params) {
        int playerId = Integer.parseInt(params.get("userid"));
        int chargeCount = Integer.parseInt(params.get("charge"));
        int id = Integer.parseInt(params.get("type"));
//		String orderId = params.get("order");
        //重新计算chargeId
        if (id == 0) {
            ServerLogger.warn("Err Charge id:", playerId, id, chargeCount);
            return RETURN_FAILED;
        }
        ServerLogger.info("后台充值 玩家id:" + playerId + " rechargeId:" + id + " 充值金额:" + chargeCount);
        //记录日志
        vipsService.addCharge(playerId, id, 1, "test", SysConfig.currency, System.currentTimeMillis() + "", SysConfig.serverId);
        return RETURN_SUCCESS;
    }

    //加称号
    public String handle_add_title(Map<String, String> params) {
        //int playerId = Integer.valueOf(params.get("playerId"));
        //int titleId = Integer.valueOf(params.get("title"));
        //titleService.addTitle(playerId, titleId);
        return RETURN_SUCCESS;
    }

    /**
     * 发送邮件
     */
    public String handle_send_mail(Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        String rewards = params.get("rewards");
        String userIds = params.get("ids");
        String minLev = params.get("min_lev");
        String maxLev = params.get("max_lev");
        String vocation = params.get("vocation");

        //检查标题内容
        if (title == null || content == null) {
            return RETURN_PARAM_ERROR;
        }
        if (rewards != null) {
            Map<Integer, Integer> reward = StringUtil.str2map(rewards, ";", ":");
            for (int goodsId : reward.keySet()) {
                if (ConfigData.getConfig(GoodsConfig.class, goodsId) == null) {
                    return RETURN_PARAM_ERROR;
                }
            }
        //检查奖励id
        }

        //拼sql
        StringBuffer sql = new StringBuffer("SELECT playerId FROM player WHERE 1=1 ");
        if (userIds != null && !userIds.trim().equals("") && !userIds.equals("null")) {
            sql.append(" and playerId in (").append(userIds).append(") ");
        }
        if (minLev != null && !minLev.trim().equals("") && !minLev.equals("null")) {
            sql.append(" and lev>=").append(minLev);
        }
        if (maxLev != null && !maxLev.trim().equals("") && !maxLev.equals("null")) {
            sql.append(" and lev<=").append(maxLev);
        }
        if (vocation != null && !vocation.trim().equals("") && !vocation.equals("0")) {
            sql.append(" and vocation=").append(vocation);
        }
        List<Map<String, Object>> list = Context.getLoggerService().getDb().queryForList(sql.toString());
        if (list.isEmpty()) {
            return RETURN_PARAM_ERROR;
        }
        List<Integer> playerIds = new ArrayList<Integer>(list.size());
        for (Map<String, Object> result : list) {
            int playerId = Integer.parseInt(String.valueOf(result.get("playerId")));
            playerIds.add(playerId);
        }

        //发邮件
        sendMail(title, content, rewards, playerIds);
        return RETURN_SUCCESS;
    }

    //封禁/解封
    public String handle_ban(Map<String, String> params) {
        int ban = Integer.parseInt(params.get("ban"));
        int type = Integer.parseInt(params.get("type"));
        Long hour = Long.parseLong(params.get("hour"));
        String closureAccount = params.get("closureAccount");

        ProhibitionEntity prohibition = new ProhibitionEntity();
        prohibition.setClosureType(ban);
        prohibition.setClosureWay(type);
        prohibition.setClosureAccount(closureAccount);
        prohibition.setClosureTime(hour);
        Date time = new Date();
        time.setTime(time.getTime() + TimeUtil.ONE_SECOND * hour);
        prohibition.setEndTime(time);

        ProhibitionEntity p = bans.get(closureAccount);
        if (p == null) {
            if (ban == BAN_PROHIBITION) {
                bans.put(closureAccount, prohibition);
            }
        } else {
            if (ban == BAN_PROHIBITION) {
                bans.put(closureAccount, prohibition);
            } else if (ban == BAN_UNSEAL) {
                bans.remove(closureAccount);
            }
        }

        return RETURN_SUCCESS;
    }

    //查询人物信息
    public String handle_getInfo(Map<String, String> params) {
        String name = params.get("name");
        Integer playerId = null;
        if (name != null) {
            playerId = playerDao.selectIdByName(name);
            if (playerId == null) {
                Player p = null;
                try {
                    p = playerService.getPlayer(Integer.parseInt(name));
                } catch (Exception ex) {

                }
                if (p == null) {
                    return RETURN_PARAM_ERROR;
                }
                playerId = p.getPlayerId();
            }
        }
        if (playerId == null) {
            return RETURN_PARAM_ERROR;
        }
        PlayerVo vo = playerService.toSLoginVo(playerId);
        return JsonUtils.object2String(vo);
    }

    //发送系统消息
    public String handle_sendSysMsg(Map<String, String> params) {
        StringParam param = new StringParam();
        String msg = params.get("msg");
        int loopTimes = Integer.parseInt(params.get("loopTimes"));//次数
        int gapSecond = Integer.parseInt(params.get("gapSecond"));//间隔秒,
        final CronTask cronTask = new CronTask(loopTimes, msg);
        ScheduledFuture<?> future = Context.getTimerService().scheduleWithFixedDelay(cronTask, 0, gapSecond, TimeUnit.SECONDS);
        cronTask.setFuture(future);
        return RETURN_SUCCESS;
    }

    //更新激活码
    public String handle_update_ActivationCode(Map<String, String> params) {
        String name = params.get("name");
        if (StringUtils.isBlank(name)) {
            String s = HttpRequestUtil.sendGet(SysConfig.gmServerUrl + "/admin/activationCode", "serverId=" + SysConfig.serverId);
            ActivationCode[] activationCodes = JsonUtils.string2Array(s, ActivationCode.class);
            if (activationCodes == null || activationCodes.length <= 0) {
                ServerLogger.warn("激活码不存在");
                return RETURN_FAILED;
            }
            Map<String, ActivationCode> giftBagMapTmp = new HashMap<>();
            for (int i = 0; i < activationCodes.length; i++) {
                giftBagMapTmp.put(activationCodes[i].getName(), activationCodes[i]);
            }
            ConfigData.giftBagMap = giftBagMapTmp;
        } else {
            if (ConfigData.giftBagMap.containsKey(name)) {
                ConfigData.giftBagMap.remove(name);
            }
        }

        return RETURN_SUCCESS;
    }

    //物品回收
    public String handle_goods_recovery(Map<String, String> params) {
        int playerId = Integer.parseInt(params.get("playerId"));
        String goods = params.get("goods");
        Map<Integer, Integer> reward = StringUtil.str2map(goods, ";", ":");
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return PLAYERID_NOT_EXISTENCE;
        }
        //扣除材料
        int code = goodsService.decConsume(playerId, reward, LogConsume.RecoveryGoods);
        if (code != Response.SUCCESS) {
            return GOOD_NOT_EXISTENCE;
        }
        return player.getName();
    }

    //查询人物信息
    public String handle_getInfoByPlayerId(Map<String, String> params) {
        PlayerVo vo = playerService.toSLoginVo(Integer.valueOf(params.get("playerId")));
        return JsonUtils.object2String(vo);
    }

    //踢人下线
    public String handle_kickPlayer(Map<String, String> params) {
        IntParam param = new IntParam();
        param.param = Response.SUCCESS;
        SessionManager.getInstance().sendMsg(PlayerExtension.FORCE_LOGOUT, param, Integer.valueOf(params.get("id")));
        return RETURN_SUCCESS;
    }

    //合服
    public String handle_mergeServer(Map<String, String> params) {
        String ip = params.get("fromIp");
        String serverId = params.get("fromServer");
        return mergeService.mergeServer(ip, serverId) + "";
    }

    //批量发系统邮件接口
    private void sendMail(String title, String content,
                          String rewards, List<Integer> ids) {
        //发送者-系统
        String sysSender = ConfigData.getConfig(ErrCode.class, Response.SYS).tips;
        final List<Object[]> params = new ArrayList<Object[]>();
        for (int playerId : ids) {
            params.add(new Object[]{0, sysSender, playerId, title, content, rewards, rewards != null && !rewards.isEmpty() ? 1 : 0, LogConsume.GM.actionId});
        }
        mailService.sendBatchMail(params);
    }

    //批量发送邮件
    public void sendMail(String title, String content, int[][] rewards, List<Integer> ids) {
        StringBuilder attach = new StringBuilder();
        if (rewards != null) {
            for (int i = 0; i < rewards.length; i++) {
                attach.append(rewards[i][0]).append(":");
                attach.append(rewards[i][1]);
                if (i != rewards.length - 1) {
                    attach.append(";");
                }
            }
        }
        sendMail(title, content, attach.toString(), ids);
    }

    //封禁检测
    public Boolean checkBan(String closureAccount, int type) {
        if (bans == null || bans.isEmpty()) {
            return false;
        }

        ProhibitionEntity prohibition = bans.get(closureAccount);
        if (prohibition == null) {
            return false;
        }

        if (prohibition.getClosureWay() != type) {
            return false;
        }

        //封禁结束
        if (prohibition.getEndTime().getTime() < new Date().getTime()) {
            bans.remove(closureAccount);
            return false;
        }

        return true;
    }

    //=====================================================

    /**
     * 帐号信息
     *
     * @param params
     * @return
     */
    private String account(Map<String, String> params) {
        return "";
    }

    /**
     * 角色信息
     *
     * @param params
     * @return
     */
    private String role(Map<String, String> params) {
        return "";
    }

    /**
     * 玩家提问
     *
     * @param params
     * @return
     */
    private String question(Map<String, String> params) {
        return "";
    }

    /**
     * 公告发布
     *
     * @param params
     * @return
     */
    private String notice(Map<String, String> params) {
        return "";
    }


    static class CronTask implements Runnable {
        private ScheduledFuture<?> future;
        private int count;
        private final int maxCount;
        private final StringParam param;

        public CronTask(int maxCount, String msg) {
            this.maxCount = maxCount;
            this.count = 0;
            param = new StringParam();
            param.param = msg;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }


        @Override
        public void run() {
            SessionManager.getInstance().sendMsgToAll(ChatExtension.SYS_NOTICE, param);
            count += 1;
            if (count >= maxCount) {
                if (future != null) {
                    future.cancel(true);
                }
            }
        }
    }
}
