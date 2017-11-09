package com.game.module.attach.lottery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.game.data.GoodsConfig;
import com.game.module.admin.MessageConsts;
import com.game.module.admin.MessageService;
import com.game.module.goods.Goods;
import com.game.util.ConfigData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.LotteryCfg;
import com.game.data.Response;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.Int2Param;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.lottery.LotteryResultVO;
import com.game.params.lottery.LotteryVO;
import com.game.util.TimeUtil;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.util.GameData;

@Extension
public class LotteryExtension {

	@Autowired
	private LotteryLogic lotteryLogic;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private RandomRewardService rewardService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private MessageService messageService;
	@Command(2601)
	public Object getInfo(int playerId, Object param){
		ListParam<LotteryVO> result = new ListParam<LotteryVO>();
		LotteryAttach attach = lotteryLogic.getAttach(playerId);
		Map<Integer, LotteryVO> map = attach.getRecords();
		if(!map.isEmpty()){
			result.params = new ArrayList<>(map.values());
		}
		return result;
	}
	
	@Command(2602)
	public Object takeReward(int playerId, Int2Param param){
		LotteryResultVO result = new LotteryResultVO();
		LotteryCfg cfg = GameData.getConfig(LotteryCfg.class, param.param1);
		if(cfg == null){
			result.code = Response.ERR_PARAM;
			return result;
		}
		Player player = playerService.getPlayer(playerId);
		if(cfg.vipLimit > player.getVip()){
			result.code = Response.NO_VIP;
			return result;
		}
		LotteryAttach attach = lotteryLogic.getAttach(playerId);
		Map<Integer, LotteryVO> map = attach.getRecords();
		LotteryVO record = map.get(cfg.id);
		if(record == null){
			record = new LotteryVO();
			record.id = cfg.id;
			map.put(cfg.id, record);
		}
		int time = param.param2 == 1 ? 1 : 10;
		if(record.count + time > cfg.limit){
			result.code = Response.NO_TODAY_TIMES;
			return result;
		}
		result.id = param.param1;
		if(time == 1 && cfg.freeLimit > record.freeCount && System.currentTimeMillis() - record.lastFree >= cfg.freePeriod * TimeUtil.ONE_HOUR){
			record.freeCount++;
			record.lastFree = System.currentTimeMillis();
		}else{
			List<GoodsEntry> req = null;
			if(time == 1){
				req = Arrays.asList(new GoodsEntry(cfg.singlePrice[0], cfg.singlePrice[1]));
			}else{
				req = Arrays.asList(new GoodsEntry(cfg.multiPrice[0], cfg.multiPrice[1]));
			}
			result.code = goodsService.decConsume(playerId, req, LogConsume.LOTTERY_REQUEST, cfg.id, time);
			if(result.code > 0){
				return result;
			}
		}
		if(record.curCount /10 < (record.curCount + time) / 10){
			if(time == 1){
				result.rewards = rewardService.getRewards(playerId, cfg.multiId, 1, LogConsume.LOTTERY_REWARD);	
			}else{
				int n = 10 - record.curCount % 10 - 1;
				if(n > 0){
					result.rewards = rewardService.getRewards(playerId, cfg.singleId, n, LogConsume.LOTTERY_REWARD);
				}else{
					result.rewards = new ArrayList<Reward>();
				}
				result.rewards.addAll(rewardService.getRewards(playerId, cfg.multiId, 1, LogConsume.LOTTERY_REWARD));
				int m = 10 - n - 1;
				if(m > 0){
					result.rewards.addAll(rewardService.getRewards(playerId, cfg.singleId, m, LogConsume.LOTTERY_REWARD));
				}
			}
		}else{
			result.rewards = rewardService.getRewards(playerId, cfg.singleId, 1, LogConsume.LOTTERY_REWARD);			
		}

		for(Reward reward : result.rewards) {
			GoodsConfig conf = ConfigData.getConfig(GoodsConfig.class,reward.id);
			if(conf == null) {
				ServerLogger.warn("goods don't exist id = " + reward.id);
			}
			if(conf.type == Goods.SKILL_CARD && (conf.color == Goods.QUALITY_VIOLET || conf.color == Goods.QUALITY_ORANGE)) { //消息广播
				messageService.sendSysMsg(MessageConsts.MSG_LOTTERY,player.getName(),conf.name);
			}
		}
		record.count += time;
		record.curCount += time;
		attach.commitSync();
		taskService.doTask(playerId, Task.FINISH_LOTTERY, param.param1, time);
		return result;
	}
}
