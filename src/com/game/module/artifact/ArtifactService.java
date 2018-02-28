package com.game.module.artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.game.data.*;
import com.game.params.ArtifactLevelUpVO;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.server.util.GameData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;

/**  
 * 神器
 */
@Service
public class ArtifactService {
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PlayerCalculator playerCalculator;
	@Autowired
	private TaskService taskService;

	//合成一个随机的部件
	public int compose(int playerId,int id){
		//随机生成一个
		ArtifactCfg cfg = ConfigData.getConfig(ArtifactCfg.class, id);
		int[] rates = cfg.decompose[0];
		int rateIndex = RandomUtil.getRandomIndex(rates);
		PlayerData data = playerService.getPlayerData(playerId);
		int[] components = data.getArtifacts().get(cfg.id);
		if(components == null) {
			components = new int[6];
			data.getArtifacts().put(cfg.id,components);
		}
		int count = cfg.clip[1];
		if(goodsService.checkHasEnough(playerId, Arrays.asList(new GoodsEntry(cfg.clip[0], count))) > 0){
			return Response.NO_MATERIAL;
		}
		boolean isSame = components != null && components[rateIndex] == 1;
		taskService.doTask(playerId, Task.FINISH_ARTIFACT, 1);
		if(isSame){
			//随机到的该位置已经激活，则只扣除50%的材料
			goodsService.decGoodsFromBag(playerId, cfg.clip[0], count>>1, LogConsume.COMPOSE_ARTIFACT_COST, id, isSame);
			return Response.ARTIFACT_SAME_PART;
		}else{			
			//扣除碎片
			goodsService.decGoodsFromBag(playerId, cfg.clip[0], count, LogConsume.COMPOSE_ARTIFACT_COST, id, isSame);
			int componentId = cfg.decompose[1][rateIndex];
			goodsService.addGoodsToBag(playerId, componentId, 1, LogConsume.COMPOSE_ARTIFACT, id);
			//检测是否足够激活
			checkActive(playerId);
			boolean ret = true;
			for(int idx : components){
				if(idx != 1){
					ret = false;
				}
			}

			if(ret){
				taskService.doTask(playerId,Task.TYPE_SQ,id);
			}
			return Response.SUCCESS;
		}
	}


	public void gmArtifact(int playerId){
		for (Object obj : GameData.getConfigs(ArtifactCfg.class)) {
			ArtifactCfg cfg = (ArtifactCfg) obj;
			for(int[] arr : cfg.components) {
				goodsService.addGoodsToBag(playerId, arr[0], 1, LogConsume.COMPOSE_ARTIFACT);
			}
		}
	}

	//分解掉多余的部件
	public int decompose(int playerId,int id){
		//计算多余的部位的分解碎片数量
		ArtifactCfg cfg = ConfigData.getConfig(ArtifactCfg.class, id);
		//扣除多余的
		List<GoodsEntry> dec = new ArrayList<GoodsEntry>();
		int clip = 0;
		for(int[] component:cfg.components){
			int componentId = component[0];
			int curCount = goodsService.getGoodsCount(playerId, componentId);
			if(curCount>component[1]){
				int decCount = curCount-component[1];
				dec.add(new GoodsEntry(componentId,decCount ));
				GoodsConfig gCfg = ConfigData.getConfig(GoodsConfig.class, componentId);
				clip += gCfg.decompose[0][1]*decCount;
			}
		}
		//增加数量
		if(dec.isEmpty()){
			return Response.ERR_PARAM;
		}
		goodsService.decConsume(playerId, dec, LogConsume.DECOMPOSE_DEC, id);
		goodsService.addGoodsToBag(playerId, ConfigData.globalParam().magicClipId, clip, LogConsume.DECOMPOSE_ADD, id);
		return Response.SUCCESS;
	}
	
	//检查是否激活神器部位
	public void checkActive(int playerId){
		//是否为空，数量不够，添加初始的数据
		PlayerData data = playerService.getPlayerData(playerId);
		boolean hasNew = data.getArtifacts().size()<ConfigData.getConfigs(ArtifactCfg.class).size();
		if(data.getArtifacts().isEmpty()||hasNew){
			for(Object o:ConfigData.getConfigs(ArtifactCfg.class)){
				ArtifactCfg cfg = (ArtifactCfg)o;
				int[] components = data.getArtifacts().get(cfg.id);
				if(components==null){
					components = new int[6];
					components = data.getArtifacts().putIfAbsent(cfg.id, components);
					if(components == null) {
						components = data.getArtifacts().get(cfg.id);
					}
				}
			}
		}

		//每一个神器检测部位所需的数量是否足够,设为1
		boolean update = false;
		for(Object o:ConfigData.getConfigs(ArtifactCfg.class)){
			ArtifactCfg cfg = (ArtifactCfg)o;
			int[] components = data.getArtifacts().get(cfg.id);
			int num = 0;
			for(int i=0;i<components.length;i++){
				int component = components[i];
				if(component == 1){
					num += 1;
					continue;
				}
				int[] need = cfg.components[i];
				if(goodsService.getGoodsCount(playerId, need[0])>=need[1]){
					update = true;
					num += 1;
					components[i] = 1;
				}
			}
			if(num == components.length) {
				Integer lv = data.getArtifactsLevelUp().get(cfg.id);
				if(lv == null || lv < 1) {
					data.getArtifactsLevelUp().put(cfg.id,1);
				}
			}
		}
		//更新战力
		if(update){
			playerCalculator.calculate(playerId);
		}
	}

	/**
	 * 神器升阶
	 *
	 * @param playerId
	 * @param sid
	 * @return
	 */
	public ArtifactLevelUpVO levelUp(int playerId, int sid) {
		ArtifactLevelUpVO param = new ArtifactLevelUpVO();
		PlayerData data = playerService.getPlayerData(playerId);
		Integer level = data.getArtifactsLevelUp().get(sid);
		if (level == null) {
			level = 1;
		}

		int maxLevel = ConfigData.getArtifactMaxLevel().get(sid);
		if (level >= maxLevel) {
			param.code = Response.MAX_LEV;
			return param;
		}

		ArtifactLevelUpCfg conf = ConfigData.getArtifactLevelUpCfgs().get(sid + "_" + 1);
		int consume = 0;
		int itemId = conf.consume[0];
		int totalCount = goodsService.getGoodsCount(playerId, itemId);
		int upTimes = 0;
		for (int i = level + 1; i <= level + 1; i++) {
			conf = ConfigData.getArtifactLevelUpCfgs().get(sid + "_" + i);
			int needCount = conf.consume[1];
			if (needCount > totalCount) {
				break;
			}
			upTimes += 1;
			totalCount -= needCount;
			consume += needCount;
		}
		param.code = Response.SUCCESS;
		List<GoodsEntry> dec = new ArrayList<>();
		GoodsEntry ge = new GoodsEntry(itemId,consume);
		dec.add(ge);
		goodsService.decConsume(playerId, dec, LogConsume.SQ_UP, sid);
		if (upTimes > 0) {
			data.getArtifactsLevelUp().put(sid, level + upTimes);
			playerCalculator.calculate(playerId);
		}
		param.id = sid;
		param.level = level + upTimes;

		taskService.doTask(playerId,Task.TYPE_SQ_UP,level + upTimes);
		return param;
	}

	/**
	 * 获取神器等阶信息
	 * @param playerId
	 * @return
	 */
	public ListParam<Int2Param> artifactList(int playerId) {
		ListParam<Int2Param> result = new ListParam<>();
		result.code = Response.SUCCESS;
		result.params = new ArrayList<>();
		PlayerData data = playerService.getPlayerData(playerId);
		for(Map.Entry<Integer,Integer> s : data.getArtifactsLevelUp().entrySet()) {
			Int2Param param = new Int2Param();
			param.param1 = s.getKey();
			param.param2 = s.getValue();
			result.params.add(param);
		}
		return result;
	}
}
