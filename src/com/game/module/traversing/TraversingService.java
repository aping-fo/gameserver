package com.game.module.traversing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.AffixCfg;
import com.game.data.GoodsConfig;
import com.game.event.InitHandler;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.copy.TraverseMap;
import com.game.module.log.LogConsume;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Reward;
import com.game.util.RandomUtil;
import com.server.util.GameData;
import com.server.util.ServerLogger;

@Service
public class TraversingService implements InitHandler {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private RandomRewardService randonRewardService;
	
	
	@Override
	public void handleInit() {
		// TODO Auto-generated method stub

	}
	
	public void addMap(int playerId, GoodsConfig config, int count){
		PlayerData data = playerService.getPlayerData(playerId);
		int maxId = data.getMaxTraverseId();
		for(int i = 0; i < count; i++){
			TraverseMap map = createMap(config);
			map.setMapId(++maxId);
			map.setGoodsCfgId(config.id);
			data.getTraverseMaps().put(map.getMapId(), map);
		}
		data.setMaxTraverseId(maxId);
	}
	
	public void remvoeMap(int playerId, int mapId){
		Map<Integer, TraverseMap> maps = playerService.getPlayerData(playerId).getTraverseMaps();
		if(maps.remove(mapId) == null){
			ServerLogger.warn("remove player traverse map fail, playerid=" + playerId + ", mapId" + mapId);
		}
		
	}
	
	public TraverseMap createMap(GoodsConfig config){
		TraverseMap map = new TraverseMap();
		map.setCopyId(config.param1[0]);
		int size = config.color;
		if(size > 0){
		int[] affixs = new int[size];
			int[] rates = config.param2[0];
			int[] candidations = config.param2[1];
			Set<Integer> exclude = new HashSet<Integer>();
			int n = 0;
			while(size > 0){
				if(n++ > 20){
					ServerLogger.warn("create traversing map fail, this is not enough candidate");
					break;
				}
				int index = RandomUtil.getRandomIndex(rates);
				int affix = candidations[index];
				if(exclude.contains(affix)){
					continue;
				}
				AffixCfg cfg = GameData.getConfig(AffixCfg.class, affix);
				exclude.add(affix);
				if(cfg.lev > config.level){
					continue;
				}
				affixs[--size] = affix;
				if(cfg.mutex != null && cfg.mutex.length > 0){
					for(int id : cfg.mutex){
						exclude.add(id);
					}
				}
			}
			map.setAffixs(affixs);
		}
		return map;
	}
	
	public List<Reward> takeReward(int playerId, int leaderId, TraverseMap map){
		PlayerData data = playerService.getPlayerData(leaderId);
		if(!data.getTraverseMaps().containsValue(map)){
			return null;
		}
		int[] affixs = map.getAffixs();
		List<Reward> result = null;
		if(affixs != null){
			result = new ArrayList<Reward>();
			for(int affix : affixs){
				AffixCfg affixCfg = GameData.getConfig(AffixCfg.class, affix);
				if(affixCfg.reward <= 0){
					continue;
				}
				List<Reward> affixReward = randonRewardService.getRewards(playerId, affixCfg.reward, LogConsume.TRAVERSING_COPY);
				if(affixReward == null){
					continue;
				}
				result.addAll(affixReward);
			}
		}
		return result;
	}

}
