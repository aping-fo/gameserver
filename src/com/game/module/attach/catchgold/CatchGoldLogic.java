package com.game.module.attach.catchgold;

import com.game.data.CopyConfig;
import com.game.data.Response;
import com.game.data.VIPConfig;
import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.attach.leadaway.LeadAwayAttach;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.CopyReward;
import com.game.params.Reward;
import com.game.params.RewardList;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.google.common.collect.RangeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 金币活动玩法
 */
@Service
public class CatchGoldLogic extends AttachLogic<CatchGoldAttach> {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private CopyService copyService;

    @Override
    public byte getType() {
        return AttachType.LEADAWAY;
    }

    @Override
    public CatchGoldAttach generalNewAttach(int playerId) {
        CatchGoldAttach attach = new CatchGoldAttach(playerId, getType());
        attach.setChallenge(ConfigData.globalParam().catchGoldChallengeCount);
        return attach;
    }

    public void updateCopy(int playerId, CopyResult result) {
        CatchGoldAttach attach = getAttach(playerId);
        attach.alterChallenge(-1);
        attach.setLastChallengeTime(System.currentTimeMillis());
        attach.commitSync();
    }

    public int buyChallengeTime(int playerId) {
        Player player = playerService.getPlayer(playerId);
        CatchGoldAttach attach = getAttach(playerId);
        VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
        if (attach.getBuyTime() >= vip.buyLeadawayCopy) {
            return Response.NO_TODAY_TIMES;
        }
        // 扣钱
        int code = goodsService.decConsume(playerId, ConfigData.globalParam().catchGoldPrice, LogConsume.BUY_LEADAWAY_TIME);
        if (code != Response.SUCCESS) {
            return code;
        }
        attach.alterChallenge(1);
        attach.addBuyTime();
        attach.commitSync();
        return Response.SUCCESS;
    }

    public CopyReward sweep(int playerId, int copyId,int difficulty) {
        CopyReward result = new CopyReward();
        result.reward = new ArrayList<>();
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        Player player = playerService.getPlayer(playerId);
        CatchGoldAttach attach = getAttach(playerId);

        if (cfg == null || cfg.type != CopyInstance.TYPE_GOLD) {
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
        int weight = ConfigData.leadawayAwardsWeight.get(copyId);
        RangeMap<Integer,Integer> map = ConfigData.leadawayAwardsMap.get(copyId);
        for(int i = 0;i < 30;i++) {
            int random = RandomUtil.randInt(weight);
            int itemId = map.get(random);
            Reward reward = new Reward();
            reward.count = 1;
            reward.id = itemId;
            list.rewards.add(reward);
        }
        result.reward.add(list);

        attach.alterChallenge(-1);
        attach.commitSync();

        return result;
    }

    public void dailyReset(int playerId) {
        CatchGoldAttach attach = getAttach(playerId);
        attach.setChallenge(ConfigData.globalParam().LeadawayChallengeCount);
        attach.setBuyTime(0);
        attach.commitSync();
    }
}
