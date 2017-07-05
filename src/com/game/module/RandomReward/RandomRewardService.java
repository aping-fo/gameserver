package com.game.module.RandomReward;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.data.RewardGroupCfg;
import com.game.data.RewardItemCfg;
import com.game.event.InitHandler;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.Reward;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
/**
 * 随机奖励处理
 * @author admin
 *
 */
@Service
public class RandomRewardService implements InitHandler {

	public static final int REWARD_TYPE_ITEM = 0;
	public static final int REWARD_TYPE_GROUP = 1;
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;
	
	private final Map<Integer, RewardGroupCfg> groups = new HashMap<Integer, RewardGroupCfg>();
	private final Map<Integer, RewardItemCfg> items = new HashMap<Integer, RewardItemCfg>();
	
	@Override
	public void handleInit() {
		Collection<Object> list = ConfigData.getConfigs(RewardItemCfg.class);
		Map<Integer, List<Integer>> temp = new HashMap<Integer, List<Integer>>();
		for(Object obj : list){
			RewardItemCfg itemCfg = (RewardItemCfg)obj;
			items.put(itemCfg.id, itemCfg);
			List<Integer> cfgs = temp.getOrDefault(itemCfg.group, new ArrayList<Integer>());
			if(cfgs.isEmpty()) temp.put(itemCfg.group, cfgs);
			cfgs.add(itemCfg.id);
		}
		for(Map.Entry<Integer, List<Integer>> entry : temp.entrySet()){
			RewardGroupCfg group = ConfigData.getConfig(RewardGroupCfg.class, entry.getKey());
			if(group == null){
				//throw new NullPointerException("can't found the rewardgroup,id=" + entry.getKey());
				continue;
			}
			group.items = new int[entry.getValue().size()];
			int i = 0;
			for(Integer id : entry.getValue()){
				group.items[i++] = id;
			}
			groups.put(group.id, group);
		}
	}

	
	private static final Object OBJ = new Object();
	/**
	 * 给玩家添加随机奖励，并返回奖励列表
	 * 
	 * @param playerId
	 * @param groupId
	 * @param count
	 * @param extra 动态修改某些选项的权重[选项ID:权重差值]
	 * @param consume
	 * @return
	 */
	public List<Reward> getRandomRewards(int playerId, int groupId, int count, Map<Integer, Integer> extra, LogConsume consume){
		RewardGroupCfg groupCfg = groups.get(groupId);
		if(groupCfg == null){
			return null;
		}
		int totalWeight = groupCfg.totalWeight;
		boolean fixed = totalWeight != 0;
		List<RewardItemCfg> candidates = new ArrayList<RewardItemCfg>(groupCfg.items.length);
		for(int id : groupCfg.items){
			RewardItemCfg itemCfg = items.get(id);
			if(filter(playerId, itemCfg.id)){
				candidates.add(itemCfg);
				if(!fixed) totalWeight += itemCfg.weight + (extra == null ? 0 : extra.getOrDefault(itemCfg.id, 0));
			}
		}
		if(candidates.isEmpty()){
			return null;
		}
		List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
		List<Reward> result = new ArrayList<Reward>();
		if(!groupCfg.repeat && candidates.size() == groupCfg.count){
			for(RewardItemCfg itemCfg : candidates){
				Reward r = new Reward();
				r.id = itemCfg.itemId;
				r.count = itemCfg.count;
				result.add(r);
				rewards.add(new GoodsEntry(itemCfg.itemId, itemCfg.count));
				
			}
		}else{
			while(count-- > 0){			
				Map<Integer, Object> keys = new HashMap<Integer, Object>();
				int itemCount = 0;
				while(itemCount < groupCfg.count){
					int weight = RandomUtil.randInt(totalWeight);
					int sum = 0;
					for(RewardItemCfg itemCfg : candidates){
						sum += itemCfg.weight + (extra == null ? 0 : extra.getOrDefault(itemCfg.id, 0));
						if(weight < sum){
							if(!groupCfg.repeat){
								if(keys.containsKey(itemCfg.id)){
									continue;
								}else{
									keys.put(itemCfg.id, OBJ);
								}
							}
							if(itemCfg.type == REWARD_TYPE_GROUP){
								List<Reward> groups = getRewards(0, itemCfg.itemId, itemCfg.count, extra, consume);
								if(!groups.isEmpty()){
									for(Reward re : groups){
										rewards.add(new GoodsEntry(re.id, re.count));
									}
									result.addAll(groups);
								}
							}else{
								Reward r = new Reward();
								r.id = itemCfg.itemId;
								r.count = itemCfg.count;
								result.add(r);
								rewards.add(new GoodsEntry(itemCfg.itemId, itemCfg.count));
							}
							itemCount++;							
							break;
						}
					}
				}
			}
		}
		
		if(playerId != 0){			
			// 加奖励
			goodsService.addRewards(playerId, rewards, consume, groupId, count);
		}
		return result;
	}
	
	/**
	 * 给玩家添加奖励，并返回奖励列表
	 * @param playerId
	 * @param groupId
	 * @param count
	 * @param extra 额外的权重调整数据，如果奖励组类型为固定奖励，则extra字段会被无视
	 * @param consume
	 * @return
	 */
	public List<Reward> getRewards(int playerId, int groupId, int count, Map<Integer, Integer> extra, LogConsume consume){
		RewardGroupCfg groupCfg = groups.get(groupId);
		if(groupCfg == null){
			return null;
		}
		if(groupCfg.type == 0){
			return getRandomRewards(playerId, groupId, count, extra, consume);
		}else{
			return getFixedRewards(playerId, groupId, count, extra, consume);
		}
	}
	
	/**
	 * 给玩家添加奖励，并返回奖励列表
	 * 
	 * @param playerId
	 * @param groupId
	 * @param consume
	 * @return
	 */
	public List<Reward> getRewards(int playerId, int groupId, LogConsume consume){
		return getRewards(playerId, groupId, 1, null, consume);
	}
	
	/**
	 * 给玩家添加奖励，并返回奖励列表
	 * 
	 * @param playerId
	 * @param groupId
	 * @param consume
	 * @return
	 */
	public List<Reward> getRewards(int playerId, int groupId, int count, LogConsume consume){
		return getRewards(playerId, groupId, count, null, consume);
	}
	
	/**
	 * 获取固定奖励，将奖励组中的所有奖励项添加给玩家
	 * @param playerId
	 * @param groupId
	 * @param consume
	 * @return
	 */
	private List<Reward> getFixedRewards(int playerId, int groupId, int count, Map<Integer, Integer> extra, LogConsume consume){
		RewardGroupCfg groupCfg = groups.get(groupId);
		if(groupCfg == null){
			return null;
		}
		List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
		List<Reward> result = new ArrayList<Reward>();
		int n = groupCfg.count*count;			
		for(int id : groupCfg.items){
			RewardItemCfg itemCfg = items.get(id);
			if(itemCfg.type == REWARD_TYPE_GROUP){
				List<Reward> groups = getRewards(0, itemCfg.itemId, itemCfg.count * n, extra, consume);
				if(!groups.isEmpty()){
					for(Reward re : groups){
						rewards.add(new GoodsEntry(re.id, re.count));
					}
					result.addAll(groups);
				}
			}else{
				Reward r = new Reward();
				r.id = itemCfg.itemId;
				r.count = itemCfg.count * n;
				result.add(r);
				rewards.add(new GoodsEntry(r.id, r.count));
			}
			
		}
		if(playerId != 0){			
			// 加奖励
			goodsService.addRewards(playerId, rewards, consume, groupId, count);
		}
		return result;
	}
	
	private boolean filter(int playerId, int rewardId){
		Player player = playerService.getPlayer(playerId);
		if(player == null){
			return true;
		}
		RewardItemCfg itemCfg = items.get(rewardId);
		if(itemCfg.level > player.getLev()){
			return false;
		}
		if(itemCfg.vip > player.getVip()){
			return false;
		}
		if(itemCfg.day > SysConfig.getOpenDays()){
			return false;
		}
		/*
		//处理将魂
		if(itemCfg.itemId / 100000 == Goods.SPRITE){
			PlayerData data = playerService.getPlayerData(playerId);
			return !data.getGenerals().contains(itemCfg.itemId % 100);
		}*/

		
		return true;
	}
	
}
