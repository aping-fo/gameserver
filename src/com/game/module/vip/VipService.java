package com.game.module.vip;

import com.game.data.ChargeConfig;
import com.game.data.Response;
import com.game.data.VIPConfig;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.activity.WelfareCardService;
import com.game.module.attach.charge.ChargeActivityLogic;
import com.game.module.daily.DailyService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.task.TaskService;
import com.game.params.Int2Param;
import com.game.params.IntList;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VipService {

    public static final int TYPE_MONTH = 1;// 月卡
    public static final int TYPE_WEEKLY = 6;// 周卡
    public static final int TYPE_NEW = 5;// 新手礼包
    public static final int TYPE_SPEC = 2;// 特殊的
    @SuppressWarnings("unused")
    private static final int TYPE_COMMON = 3;// 普通的
    public static final int TYPE_FUND = 4;// 基金

    private static final int MONTH_CARD_ID = 11;// 月卡充值id
    private static final int FUND_ID = 41;// 基金id

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private DailyService dailyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ChargeActivityLogic chargeActivityLogic;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private WelfareCardService welfareCardService;
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
    public void addCharge(int playerId, int id, int count) {
        int realCount = count;
        ChargeConfig charge = ConfigData.getConfig(ChargeConfig.class, id);
        int type = charge.type;
        PlayerData data = playerService.getPlayerData(playerId);
        if (type == TYPE_SPEC) {
            if (data.getCharges().contains(id)) {
                ServerLogger.warn("Err charge id:" + id, playerId);
            } else {
                data.getCharges().add(id);
            }
        } else if (type == TYPE_FUND) {
            data.setFundActive(1);
        }
        if (charge.total != count) {
            return;
        }

        playerService.addVipExp(playerId, count);

        playerService.addDiamond(playerId, count, LogConsume.CHARGE);
        playerService.addDiamond(playerId, charge.add, LogConsume.CHARGE);

        chargeActivityLogic.updateCharge(playerId, count);
        // 每日数据更新
        dailyService.refreshDailyVo(playerId);
        // 通知前端
        Int2Param result = new Int2Param();
        result.param1 = realCount;
        result.param2 = count + charge.add;
        SessionManager.getInstance().sendMsg(VipExtension.CHARGE, result, playerId);

        welfareCardService.buyWelfareCard(playerId,charge.type,id);
        if (!data.isFirstRechargeFlag() && charge.type == TYPE_NEW) {
            data.setFirstRechargeFlag(true);
            activityService.completeActivityTask(playerId,
                    ActivityConsts.ActivityTaskCondType.T_FIRST_RECHARGE,1, ActivityConsts.UpdateType.T_VALUE,true);
        }
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

        for (int i = 0; i < config.rewards.length; i++) {
            int[] item = config.rewards[i];
            goods.add(new GoodsEntry(item[0], item[1]));
        }

        goodsService.addRewards(playerId, goods, LogConsume.TASK_REWARD, vipLevel);
        param.param = Response.SUCCESS;
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
}
