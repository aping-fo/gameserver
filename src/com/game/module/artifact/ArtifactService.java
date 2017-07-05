package com.game.module.artifact;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.ArtifactCfg;
import com.game.data.GoodsConfig;
import com.game.data.Response;
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
		int count = cfg.clip[1];
		boolean isSuccess = components != null && components[rateIndex] == 1;
		if(isSuccess){
			//随机到的该位置已经激活，则只扣除50%的材料
			count >>= 1;
		}
		//扣除碎片
		if(!goodsService.decGoodsFromBag(playerId, cfg.clip[0], count, LogConsume.COMPOSE_ARTIFACT_COST, id, isSuccess)){
			return Response.NO_MATERIAL;
		}
		int componentId = cfg.decompose[1][rateIndex];
		goodsService.addGoodsToBag(playerId, componentId, 1, LogConsume.COMPOSE_ARTIFACT, id);
		//检测是否足够激活
		checkActive(playerId);
		taskService.doTask(playerId, Task.FINISH_ARTIFACT, 1);
		return Response.SUCCESS;
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
				}
			}
		}
		//每一个神器检测部位所需的数量是否足够,设为1
		boolean update = false;
		for(Object o:ConfigData.getConfigs(ArtifactCfg.class)){
			ArtifactCfg cfg = (ArtifactCfg)o;
			int[] components = data.getArtifacts().get(cfg.id);
			for(int i=0;i<components.length;i++){
				int component = components[i];
				if(component == 1){
					continue;
				}
				int[] need = cfg.components[i];
				if(goodsService.getGoodsCount(playerId, need[0])>=need[1]){
					update = true;
					components[i] = 1;
				}
			}
		}
		//更新战力
		if(update){
			playerCalculator.calculate(playerId);
		}
	}
}
