package com.game.module.vip;

import com.game.SysConfig;
import com.game.data.ChargeConfig;
import com.game.data.Response;
import com.game.data.VIPConfig;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.activity.WelfareCardService;
import com.game.module.attach.charge.ChargeActivityLogic;
import com.game.module.daily.DailyService;
import com.game.module.gang.GangService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.shop.ShopService;
import com.game.params.*;
import com.game.sdk.talkdata.TalkDataService;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.game.util.RandomUtil;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VipService {

    public static final int TYPE_MONTH = 1;// 月卡
    public static final int TYPE_NEW = 5;// 新手礼包
    public static final int TYPE_WEEKLY = 6;// 周卡
    public static final int TYPE_SPEC = 2;// 特殊的
    public static final int TYPE_TIMED = 7;// 限时礼包
    public static final int TYPE_SPECIAL = 9;// 特价礼包
    //public static final int TYPE_ONCE = 10;// 单笔充值
    @SuppressWarnings("unused")
    private static final int TYPE_COMMON = 3;// 普通的
    public static final int TYPE_FUND = 4;// 基金

    private static final int MONTH_CARD_ID = 11;// 月卡充值id
    private static final int FUND_ID = 41;// 基金id
    public static final int KEY = 0xef;

    public static final String SIMULATION__RECHARGE = "test";

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private DailyService dailyService;
    @Autowired
    private TalkDataService talkDataService;
    @Autowired
    private ChargeActivityLogic chargeActivityLogic;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private WelfareCardService welfareCardService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private GangService gangService;

    // 获取vip奖励
    public int getVipReward(int playerId, int vipLev) {
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);

        // 等级不够
        if (player.getVip() < vipLev) {
            return Response.NO_VIP;
        }
        VIPConfig cfg = ConfigData.getConfig(VIPConfig.class, vipLev);
        if (cfg.rewards == null) {
            return Response.ERR_PARAM;
        }
        // 已经领取
        if (data.getVipReward().get(vipLev) != null) {
            return Response.HAS_TAKE_REWARD;
        }
        // 钻石不足
        if (!playerService.decDiamond(playerId, cfg.price, LogConsume.VIP_BAG_COST, player.getVip())) {
            return Response.NO_DIAMOND;
        }
        // 背包已满
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        for (int i = 0; i < cfg.rewards.length; i++) {
            int[] item = cfg.rewards[i];
            rewards.add(new GoodsEntry(item[0], item[1]));
        }
        if (!goodsService.checkCanAddToBag(playerId, rewards)) {
            return Response.BAG_FULL;
        }

        data.getVipReward().put(vipLev, 1);
        goodsService.addRewards(playerId, rewards, LogConsume.VIP_BAG);
        playerService.update(player);
        return Response.SUCCESS;
    }

    // vip每日福利
    public int getVipDailyReward(int playerId) {
        // 今天领取过没有
        if (dailyService.getCount(playerId, DailyService.VIP_DAILY_REWARD) >= 1) {
            return Response.ERR_PARAM;
        }
        // vip等级
        Player player = playerService.getPlayer(playerId);
        if (player.getVip() == 0) {
            return Response.ERR_PARAM;
        }
        // 加物品
        VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        for (int i = 0; i < vip.dailyRewards.length; i++) {
            int[] item = vip.dailyRewards[i];
            rewards.add(new GoodsEntry(item[0], item[1]));
        }
        if (!goodsService.checkCanAddToBag(playerId, rewards)) {
            return Response.BAG_FULL;
        }
        dailyService.alterCount(playerId, DailyService.VIP_DAILY_REWARD, 1);
        goodsService.addRewards(playerId, rewards, LogConsume.VIP_DAILY_REWARD);
        return Response.SUCCESS;
    }

    // 领取月卡福利
    public int getMonthCardReward(int playerId) {
        // 是否到期了
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getMonthCard() == 0) {
            return Response.ERR_PARAM;
        }
        // 今日有无领过
        if (dailyService.getCount(playerId, DailyService.VIP_MONTH_CARD) >= 1) {
            return Response.ERR_PARAM;
        }
        // 更新次数
        dailyService.alterCount(playerId, DailyService.VIP_MONTH_CARD, 1);
        // 加钻石
        ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, MONTH_CARD_ID);
        //playerService.addDiamond(playerId, charge.weekMonthCard[0], LogConsume.VIP_MONTH_CARD);
        //额外奖励
        /*if(ConfigData.globalParam().monthCardRewards!=null){
            goodsService.addRewards(playerId, ConfigData.globalParam().monthCardRewards, LogConsume.VIP_MONTH_CARD);
		}*/
        return Response.SUCCESS;
    }

    // 领取基金
    public int takeFund(int playerId, int lev) {
        // 是否激活了
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getFundActive() == 0) {
            return Response.ERR_PARAM;
        }
        // 是否已经领取了
        if (data.getFunds().contains(lev)) {
            return Response.ERR_PARAM;
        }
        Integer diamond = ConfigData.getConfig(ChargeConfig.class, FUND_ID).funds.get(lev);
        if (diamond == null) {
            return Response.ERR_PARAM;
        }
        // 设置已经领取
        data.getFunds().add(lev);
        // 加钻石
        playerService.addDiamond(playerId, diamond, LogConsume.VIP_FUN);
        // 更新每日数据
        dailyService.refreshDailyVo(playerId);
        return Response.SUCCESS;
    }

    // 充值
    public void addCharge(int playerId, int id, long cpId, String paymentType, String currentType, String orderId, int serverId) {
        ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, id);
        if (charge == null) {
            return;
        }
        int type = charge.type;
        PlayerData data = playerService.getPlayerData(playerId);
        if (data == null) {
            return;
        }
        if (type == TYPE_SPEC) {
            if (data.getCharges().contains(id)) {
                ServerLogger.warn("Err charge id:" + id, playerId);
            } else {
                data.getCharges().add(id);
            }
        } else if (type == TYPE_FUND) {
            data.setFundActive(1);
        }

        if (!SysConfig.gm && !paymentType.equals(SIMULATION__RECHARGE)) {
            if (!data.getCpIdSet().contains(cpId)) { //不包含此订单
                ServerLogger.warn("Err charge cpId:" + cpId, playerId);
                return;
            }

            if (data.getDealCpIdSet().contains(cpId)) { //已经处理过的订单
                ServerLogger.warn("order has done ,id:" + cpId, playerId);
                return;
            }
        }

        if (SysConfig.report && !paymentType.equals(SIMULATION__RECHARGE)) {
            Context.getThreadService().execute(new Runnable() {
                @Override
                public void run() {
                    float f = 0f;
                    if (currentType.equals("CNY")) {
                        f = charge.rmb;
                    } else {
                        f = charge.us;
                    }
                    talkDataService.talkGameRecharge(playerId, orderId, f, charge.total + charge.add, currentType, serverId, id);
                }
            });
        }

        playerService.addVipExp(playerId, charge.total);
        playerService.addDiamond(playerId, charge.total, LogConsume.CHARGE);
        playerService.addDiamond(playerId, charge.add, LogConsume.CHARGE_ADD);
        chargeActivityLogic.updateCharge(playerId, charge.total);
        // 每日数据更新
        dailyService.refreshDailyVo(playerId);
        // 通知前端
        RechargeRespVO result = new RechargeRespVO();
        float f = 0f;
        if (currentType.equals("CNY")) {
            f = charge.rmb;
        } else {
            f = charge.us;
        }
        result.amount = f;
        result.totalAmout = charge.total + charge.add;
        result.paymentType = "alipay";
        result.currentType = currentType;
        result.orderId = orderId;
        result.rechargeCfgId = id;
        SessionManager.getInstance().sendMsg(VipExtension.CHARGE, result, playerId);

        welfareCardService.buyWelfareCard(playerId, charge.type, id);
        if (!data.isFirstRechargeFlag()) {
            data.setFirstRechargeFlag(true);
        }

        if (type != TYPE_TIMED && type != TYPE_SPECIAL) {//限时礼包和特价礼包不计入累计充值
            activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_FIRST_RECHARGE, (int) charge.rmb, ActivityConsts.UpdateType.T_ADD, true);//累充礼包
        }

        //购买月卡开启理财
        if (type == TYPE_WEEKLY) {
            activityService.openActivity(playerId, ActivityConsts.ActivityType.T_GROW_FUND);
        }

        activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_TIMED_MONEY, (int) charge.rmb, ActivityConsts.UpdateType.T_ADD, true);//充了钱就算礼包
        activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_DAILY_RECHARGE_DIAMONDS, charge.total, ActivityConsts.UpdateType.T_ADD, true);//每日充值钻石
        activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_TIMED_BAG, id, ActivityConsts.UpdateType.T_VALUE, true);//限时礼包和特价礼包
        activityService.onceRecharge(playerId, charge.rmb);//单笔充值满足(取最大那个)
        activityService.dailyRecharge(playerId, charge.rmb);//每日充值(7日充值)

        if (charge.shopGoodsId != 0) {
            shopService.buy(playerId, charge.shopGoodsId, 1);
        }
        ServerLogger.warn("活动钻石:" + charge.total + " 额外钻石:" + charge.add + " 人民币:" + charge.rmb + " 美元:" + charge.us + " 玩家id:" + playerId + " rechargeId:" + id + " encodeOrder:" + cpId + " order:" + (cpId ^ KEY));

        Player player = playerService.getPlayer(playerId);
        Context.getLoggerService().logCharge(playerId, player.getName(), id, currentType, f, player.getServerId(), paymentType);

        data.setTotalCharge(data.getTotalCharge() + charge.rmb);
        playerService.saveCharge(player.getAccName(), playerId, player.getName(), Math.round(data.getTotalCharge()), data.getMaxLoginContinueDays());
    }

    /**
     * 领取vip礼包
     *
     * @param playerId
     * @param vipLevel
     * @return
     */
    public IntParam getVipGift(int playerId, int vipLevel) {
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);

        IntParam param = new IntParam();
        if (player.getVip() < vipLevel) {
            param.param = Response.NO_VIP;
            return param;
        }

        if (data.getVipGifts().contains(vipLevel)) {
            param.param = Response.HAS_TAKE_REWARD;
            return param;
        }

        data.getVipGifts().add(vipLevel);

        List<GoodsEntry> goods = new ArrayList<>();
        VIPConfig config = ConfigData.getConfig(VIPConfig.class, vipLevel);

        if (config == null) {
            param.param = Response.ERR_PARAM;
            ServerLogger.warn("vip gift config not found, viplevel =" + vipLevel);
            return param;
        }

        //扣除钻石
        param.param = goodsService.decConsume(playerId, new int[][]{{Goods.DIAMOND, config.price}}, LogConsume.VIP_BAG_COST);
        if (param.param == Response.SUCCESS) {
            for (int i = 0; i < config.rewards.length; i++) {
                int[] item = config.rewards[i];
                goods.add(new GoodsEntry(item[0], item[1]));
            }
            goodsService.addRewards(playerId, goods, LogConsume.TASK_REWARD, vipLevel);
        }

        return param;
    }

    /**
     * 领取vip奖励领取记录
     *
     * @param playerId
     * @return
     */

    public ListParam<IntParam> getVipGiftResultList(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ListParam<IntParam> result = new ListParam();
        result.params = new ArrayList();
        for (int vipLevel : data.getVipGifts()) {
            IntParam param = new IntParam();
            param.param = vipLevel;
            result.params.add(param);
        }
        return result;
    }

    /**
     * 获取CP订单号
     *
     * @param playerId
     * @return
     */
    public Long2Param getCpId(int playerId, int rechargeId) {
        PlayerData data = playerService.getPlayerData(playerId);
//        int orderID = data.getCpId();
//        orderID += (RandomUtil.randInt(20) + 1); //随机ID
        long orderID = System.nanoTime();
        data.getCpIdSet().add(orderID);
        Long2Param param = new Long2Param();
        param.param1 = orderID;
        param.param2 = rechargeId;
        ServerLogger.warn("rechargeId = " + rechargeId + " order = " + orderID);
        return param;
    }
}
