package com.game.module.attach.experience;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.goods.GoodsEntry;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.IntParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.CopyConfig;
import com.game.data.Response;
import com.game.data.VIPConfig;
import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.CopyReward;
import com.game.params.RewardList;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;

@Service
public class ExperienceLogic extends AttachLogic<ExperienceAttach> {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private CopyService copyService;
	@Autowired
	private TaskService taskService;
	@Autowired
    private ActivityService activityService;

	@Override
	public byte getType() {
		return AttachType.EXPERIENCE;
	}

	@Override
	public ExperienceAttach generalNewAttach(int playerId) {
		ExperienceAttach attach = new ExperienceAttach(playerId, getType());
		attach.setChallenge(ConfigData.globalParam().extremeEvasionChallengeCount);
		return attach;
	}

	public void updateCopy(int playerId, CopyResult result){
		ExperienceAttach attach = getAttach(playerId);
		attach.alterChallenge(-1);
		attach.setLastChallengeTime(System.currentTimeMillis());
		attach.commitSync();
	}
	
	public int buyChallengeTime(int playerId, IntParam param){
		Player player = playerService.getPlayer(playerId);
		ExperienceAttach attach = getAttach(playerId);
		int buyCount = param.param;

        if (buyCount <= 0) {
            return Response.ERR_PARAM;
        }

		VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
		if(attach.getBuyTime() + buyCount > vip.buyExtremeEvasionCopy){
			return Response.NO_TODAY_TIMES;
		}

		// 扣钱
		Map<Integer, Integer> price = ConfigData.globalParam().buyExtremeEvasionPrice;
		for(int key:price.keySet()){
			price.replace(key, price.get(key) * buyCount);
		}
		int code = goodsService.decConsume(playerId, price, LogConsume.BUY_EXPRIENCE_TIME);
		if (code != Response.SUCCESS) {
			return code;
		}
		attach.alterChallenge(buyCount);
		attach.addBuyTime(buyCount);
		attach.commitSync();

		//极限躲避活动
        activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_RESOURCE_PURCHASE, buyCount);

        return Response.SUCCESS;
	}
	
	public CopyReward sweep(int playerId, int copyId){
		CopyReward result = new CopyReward();
		result.reward = new ArrayList<RewardList>();
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
		Player player = playerService.getPlayer(playerId);
		ExperienceAttach attach = getAttach(playerId);

		if (cfg == null || cfg.type != CopyInstance.TYPE_EXPERIENCE) {
			result.code = Response.ERR_PARAM;
			return result;
		}

		// 检查等级
		if (player.getLev() < cfg.lev) {
			result.code = Response.NO_LEV;
			return result;
		}
		
		if(attach.getChallenge() <= 0){
			result.code = Response.NO_TODAY_TIMES;
			return result;
		}
		
		// 活动副本扣除体力
		if (cfg.needEnergy>0) {
			if (player.getEnergy() < cfg.needEnergy) {
				result.code = Response.NO_ENERGY;
				return result;
			}
		}
					
		// 扣钱
		int code = goodsService.decConsume(playerId, ConfigData.globalParam().quickExtremeEvasionCopy,LogConsume.QUICK_EXPRIENCE,copyId);
		if (code != Response.SUCCESS) {
			result.code = code;
			return result;
		}
		
		if (cfg.needEnergy>0) {
			playerService.decEnergy(playerId, cfg.needEnergy, LogConsume.COPY_ENERGY, copyId);
		}

		Map<Integer,GoodsEntry> map = Maps.newHashMap();
		List<RewardList> rewardLists = copyService.swipeCopyInner(playerId, copyId,map);
		if (rewardLists != null) {
			for (int i = 0; i < rewardLists.size(); ++i) {
				result.reward.add(rewardLists.get(i));
			}
		}
		goodsService.addRewards(playerId, Lists.newArrayList(map.values()), LogConsume.COPY_REWARD, copyId);
		attach.alterChallenge(-1);
		attach.commitSync();
		taskService.doTask(playerId, Task.TYPE_PASS_TYPE_COPY,cfg.type,1);
		return result;
	}
	
	public void dailyReset(int playerId){
		ExperienceAttach attach = getAttach(playerId);
		attach.setChallenge(ConfigData.globalParam().extremeEvasionChallengeCount);
		attach.setBuyTime(0);
		attach.commitSync();
	}
}
