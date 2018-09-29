package com.game.module.attach.leadaway;

import java.util.Map;

import com.game.data.CopyConfig;
import com.game.data.DropGoods;
import com.game.data.Response;
import com.game.data.VIPConfig;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.CopyReward;
import com.game.params.IntParam;
import com.game.params.Reward;
import com.game.params.RewardList;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.google.common.collect.Maps;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 顺手牵羊活动玩法
 */
@Service
public class LeadAwayLogic extends AttachLogic<LeadAwayAttach> {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private ActivityService activityService;

    @Override
    public byte getType() {
        return AttachType.LEADAWAY;
    }

    @Override
    public LeadAwayAttach generalNewAttach(int playerId) {
        LeadAwayAttach attach = new LeadAwayAttach(playerId, getType());
        attach.setChallenge(ConfigData.globalParam().LeadawayChallengeCount);
        return attach;
    }

    public void updateCopy(int playerId, CopyResult result) {
        LeadAwayAttach attach = getAttach(playerId);
        attach.alterChallenge(-1);
        attach.setLastChallengeTime(System.currentTimeMillis());
        attach.commitSync();
    }

    public int buyChallengeTime(int playerId, IntParam param) {
        Player player = playerService.getPlayer(playerId);
        LeadAwayAttach attach = getAttach(playerId);
        int buyCount = param.param;

        if (buyCount <= 0) {
            return Response.ERR_PARAM;
        }

        VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
        if (attach.getBuyTime() + buyCount > vip.buyLeadawayCopy) {
            return Response.NO_TODAY_TIMES;
        }
        // 扣钱
        Map<Integer, Integer> price = Maps.newHashMap(ConfigData.globalParam().buyLeadawayPrice);
        for(int key:price.keySet()){
            price.replace(key, price.get(key) * buyCount);
        }
        int code = goodsService.decConsume(playerId, price, LogConsume.BUY_LEADAWAY_TIME);
        if (code != Response.SUCCESS) {
            return code;
        }
        attach.alterChallenge(buyCount);
        attach.addBuyTime(buyCount);
        attach.commitSync();

        //顺手牵羊活动
        activityService.completionCumulative(playerId, ActivityConsts.ActivityTaskCondType.T_RESOURCE_PURCHASE, buyCount);

        return Response.SUCCESS;
    }

    public CopyReward sweep(int playerId, int copyId, int difficulty) {
        CopyReward result = new CopyReward();
        result.reward = new ArrayList<>();
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        Player player = playerService.getPlayer(playerId);
        LeadAwayAttach attach = getAttach(playerId);

        if (cfg == null || cfg.type != CopyInstance.TYPE_LEADAWAY) {
            result.code = Response.ERR_PARAM;
            return result;
        }

        // 检查等级
        if (player.getLev() < cfg.lev) {
            result.code = Response.NO_LEV;
            return result;
        }

        if (attach.getChallenge() <= 0) {
            result.code = Response.NO_TODAY_TIMES;
            return result;
        }

        // 活动副本扣除体力
        if (cfg.needEnergy > 0) {
            if (player.getEnergy() < cfg.needEnergy) {
                result.code = Response.NO_ENERGY;
                return result;
            }
        }

        // 扣钱
        int code = goodsService.decConsume(playerId, ConfigData.globalParam().quickLeadawayCopy, LogConsume.QUICK_LEADAWAY, difficulty);
        if (code != Response.SUCCESS) {
            result.code = code;
            return result;
        }

        if (cfg.needEnergy > 0) {
            playerService.decEnergy(playerId, cfg.needEnergy, LogConsume.COPY_ENERGY, difficulty);
        }

        //随机奖励
        RewardList list = new RewardList();
        list.rewards = new ArrayList<>();
        List<GoodsEntry> copyRewards = new ArrayList<>();
        int dropId = ConfigData.leadawayAwardsDrop.get(copyId);
        DropGoods dropGoods = ConfigData.getConfig(DropGoods.class, dropId);
        int weight = 0;
        for (int n : dropGoods.rate) {
            weight += n;
        }

        for (int i = 0; i < 30; i++) {
            int index = RandomUtil.randInt(weight);
            int k = 0;
            int w = 0;
            for (int n = 0; n < dropGoods.rate.length; n++) {
                w += dropGoods.rate[n];
                if (index <= w) {
                    break;
                }
                k++;
            }
            int[] award = dropGoods.rewards[k];

            Reward reward = new Reward();
            reward.id = award[0];
            reward.count = award[1];
            list.rewards.add(reward);

            copyRewards.add(new GoodsEntry(award[0], award[1]));
        }

        //活动奖励
        Reward activityReward = copyService.activityReward(playerId, CopyInstance.TYPE_LEADAWAY);
        if (activityReward != null) {
            copyRewards.add(new GoodsEntry(activityReward.id, activityReward.count));
            list.rewards.add(activityReward);
        }

        result.reward.add(list);
        attach.alterChallenge(-1);
        attach.commitSync();
        goodsService.addRewards(playerId, copyRewards, LogConsume.COPY_REWARD, copyId);
        taskService.doTask(playerId, Task.TYPE_PASS_TYPE_COPY, cfg.type, 1);
        return result;
    }

    public void dailyReset(int playerId) {
        LeadAwayAttach attach = getAttach(playerId);
        attach.setChallenge(ConfigData.globalParam().LeadawayChallengeCount);
        attach.setBuyTime(0);
        attach.commitSync();
    }
}
