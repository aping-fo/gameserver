package com.game.module.goods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.EquipAddAttrCfg;
import com.game.data.EquipJewelCfg;
import com.game.data.EquipStarCfg;
import com.game.data.EquipStrengthCfg;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.module.admin.MessageService;
import com.game.module.log.LogConsume;
import com.game.module.player.Jewel;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.GainGoodNotify;
import com.game.params.goods.AttrItem;
import com.game.params.goods.EquipInfo;
import com.game.params.goods.SGoodsVo;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.server.SessionManager;

@Service
public class EquipService {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PlayerCalculator playerCalculator;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private MessageService messageService;

	// 穿装备
	public int wear(int playerId, long id) {
		Player player = playerService.getPlayer(playerId);
		PlayerBag bag = goodsService.getPlayerBag(playerId);
		Goods goods = goodsService.getGoods(playerId, id);
		if(goods == null) {
			return Response.ERR_GOODS_TYPE;
		}
		GoodsConfig config = goodsService.getGoodsConfig(goods.getGoodsId());
		// 类型不符
		int[] canWear = ConfigData.globalParam().equipTypes;
		if (!CommonUtil.contain(canWear, config.type)) {
			return Response.ERR_GOODS_TYPE;
		}
		if (config.vocation > 0 && player.getVocation() != config.vocation) {
			return Response.NO_VOCATION;
		}
		if (config.level > 0 && player.getLev() < config.level) {
			return Response.NO_LEV;
		}
		List<SGoodsVo> vo = new ArrayList<SGoodsVo>();

		// 找到身上穿的
		Goods curEquip = null;
		for (Goods g : bag.getAllGoods().values()) {
			if (g.getId() != id && !g.isInBag()) {
				GoodsConfig curCfg = goodsService.getGoodsConfig(g.getGoodsId());
				if (curCfg.type == config.type) {
					curEquip = g;
					break;
				}
			}
		}
		if (curEquip != null) {
			curEquip.setStoreType(Goods.BAG);
			vo.add(goodsService.toVO(curEquip));
		}
		goods.setStoreType(Goods.EQUIP);

		playerCalculator.calculate(player);

		vo.add(goodsService.toVO(goods));
		goodsService.refreshGoodsToClient(playerId, vo);
		taskService.doTask(playerId, Task.FINISH_WEAR, config.color);
		return Response.SUCCESS;
	}

	// 脱掉装备
	public int putOff(int playerId, long id) {
		Player player = playerService.getPlayer(playerId);

		Goods goods = goodsService.getGoods(playerId, id);
		if (goods == null) {
			return Response.SYS_ERR;
		}

		GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
		if (!goodsService.checkCanAdd(playerId, cfg.id, 1)) {
			return Response.BAG_FULL;
		}

		goods.setStoreType(Goods.BAG);

		playerCalculator.calculate(player);

		List<SGoodsVo> vo = new ArrayList<SGoodsVo>();
		vo.add(goodsService.toVO(goods));
		goodsService.refreshGoodsToClient(playerId, vo);

		return Response.SUCCESS;
	}
	
	//分解
	public Object decompose(int playerId,Collection<Long> ids){
		//计算总的获得
		int goodsId = 0;
		int count = 0;
		for(long id:ids){
			int equipMaterials = 0;
			Goods goods = goodsService.getGoods(playerId, id);
			GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
			if(cfg == null) {
				ServerLogger.warn("goods don't exist id = " + cfg.id);
			}
			goodsId = cfg.decompose[0][0];
			equipMaterials +=cfg.decompose[0][1];
			
			//升星返还
			if(goods.getStar()>0){
				EquipStarCfg nextCfg = ConfigData.getConfig(EquipStarCfg.class, cfg.type*100000+cfg.level*100+goods.getStar());
				if(nextCfg!=null){
					equipMaterials+=nextCfg.decompose;
				}
			}
			count+=equipMaterials;
			//扣除物品
			goodsService.decSpecGoods(goods, goods.getStackNum(), LogConsume.DECOMPOSE_DEC);
			goodsService.addRewrad(playerId, cfg.decompose[0][0], equipMaterials, LogConsume.DECOMPOSE_DEC);
		}
		GainGoodNotify notify = new GainGoodNotify();
		//加奖励
		//playerService.addCurrency(playerId, Goods.EQUIP_TOOL, equipMaterials, LogConsume.DECOMPOSE_ADD);
		taskService.doTask(playerId, Task.FINISH_DECOMPOSE, 1);
		notify.id = goodsId;
		notify.count = count;
		return notify;
	}
	
	//升星
	public int upStar(int playerId,long id){
		//已经到满星
		Goods goods = goodsService.getGoods(playerId, id);
		GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
		int nextStar = goods.getStar()+1;
		EquipStarCfg nextCfg = ConfigData.getConfig(EquipStarCfg.class, cfg.type*100000+cfg.level*100+nextStar);
		if(nextCfg==null){
			return Response.MAX_STAR;
		}
		//扣除材料
		if(!playerService.decCurrency(playerId, Goods.EQUIP_TOOL,nextCfg.cost, LogConsume.UP_STAR_COST, cfg.id)){
			return Response.NO_MATERIAL;
		};
		//更新星级
		goods.setStar(nextStar);
		//更新物品
		goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
		//属性更新
		if(!goods.isInBag()){
			playerCalculator.calculate(playerId);
		}
		taskService.doTask(playerId, Task.FINISH_STAR, nextStar, 1);
		return Response.SUCCESS;
	}
	
	//强化
	public int strength(int playerId,int type,boolean useTicket){
		//类型错误
		if(!CommonUtil.contain(ConfigData.globalParam().equipTypes, type)){
			return Response.ERR_PARAM;
		}
		//已经到最高级
		PlayerData data = playerService.getPlayerData(playerId);
		Integer curStrength = data.getStrengths().get(type);
		if(curStrength==null){
			curStrength = 0;
		}
		Player player = playerService.getPlayer(playerId);
		if(curStrength>=player.getLev()){
			return Response.EXCEED_LEV;
		}
		int next = curStrength+1;
		EquipStrengthCfg nextCfg = ConfigData.getConfig(EquipStrengthCfg.class, type*1000+next);
		if(nextCfg==null){
			return Response.MAX_STRENGTH;
		}
		//检查金币
		
		if(player.getCoin()<nextCfg.costCoin){
			return Response.NO_COIN;
		}
		//扣除物品
		
		
		List<GoodsEntry> goods = new ArrayList<GoodsEntry>(6);
		if(nextCfg.costTools!=null){
			for (int i = 0; i < nextCfg.costTools.length; i++) {
				goods.add(new GoodsEntry(nextCfg.costTools[i][0], nextCfg.costTools[i][1]));
			}
			if(goodsService.checkHasEnough(playerId, goods)!=Response.SUCCESS){
				return Response.NO_MATERIAL;
			}
		}
		//扣除金币
		if(!playerService.decCoin(playerId, nextCfg.costCoin, LogConsume.STRENGTH_COST, type)){
			return Response.NO_COIN;
		}
		//是否消耗强化券
		int rate = nextCfg.successRate;
		if(useTicket){
			if(goodsService.decGoodsFromBag(playerId, ConfigData.globalParam().strengthTicket, 1, LogConsume.STRENGTH_COST, type)){
				rate += ConfigData.globalParam().strengthTicketAdd;
			}
		}
		int result = Response.SUCCESS;
		//计算概率
		boolean success = RandomUtil.randomHitPercent(rate);
		if(success){
			data.getStrengths().put(type, next);
			//更新装备位属性
			playerCalculator.calculate(playerId);
			//更新前端数据
			updateEquip2Client(playerId);
		}else{
			//材料减半
			if(goods.size() >0){
				for(GoodsEntry ge : goods){
					ge.count >>= 1;
				}
			}
			//返还金币
			playerService.addCoin(playerId, nextCfg.costCoin>>1, LogConsume.STRENGTH_COST, type);
			result = Response.STRENGTH_FAIL;
		}
		goodsService.decConsume(playerId, goods, LogConsume.STRENGTH_COST, type);
		taskService.doTask(playerId, Task.FINISH_STRONG, success?next:curStrength, type, 1);
		return result;
	}
	
	//升级宝石
	public int upJewel(int playerId,int type){
		//参数验证
		if(!CommonUtil.contain(ConfigData.globalParam().equipTypes, type)){
			return Response.ERR_PARAM;
		}
		//已经满级了
		PlayerData data = playerService.getPlayerData(playerId);
		Jewel jewel = data.getJewels().get(type);
		if(jewel==null){
			jewel = new Jewel();
			data.getJewels().put(type, jewel);
		}
		
		int next = jewel.getLev()+1;
		EquipJewelCfg nextCfg = ConfigData.getConfig(EquipJewelCfg.class, type*1000+next);
		if(nextCfg==null){
			return Response.MAX_LEV;
		}
		EquipJewelCfg curCfg = ConfigData.getConfig(EquipJewelCfg.class, type*1000+jewel.getLev());
		//依次扣除，直到满级
		boolean full = false;
		boolean upgrade =false;
		for(int id:ConfigData.globalParam().jewelCost.get(type)){
			Collection<Goods> costs = goodsService.getExistBagGoods(playerId, id);
			for(Goods cost:costs){
				GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, cost.getGoodsId());
				int delCount = 0;
				int count = cost.getStackNum();
				for(int i=1;i<=count;i++){
					delCount++;
					int addExp = cfg.decompose[0][1];
					jewel.setExp(jewel.getExp()+addExp);
					//更新经验，等级
					while(jewel.getExp()>=curCfg.exp){
						jewel.setLev(jewel.getLev()+1);
						jewel.setExp(jewel.getExp()-curCfg.exp);
						upgrade = true;
						
						curCfg = ConfigData.getConfig(EquipJewelCfg.class, type*1000+jewel.getLev());
						nextCfg = ConfigData.getConfig(EquipJewelCfg.class, type*1000+jewel.getLev()+1);
						full = (nextCfg==null);
						if(full||jewel.getExp()<curCfg.exp){
							break;
						}
					}
					if(full){
						break;
					}
				}
				goodsService.decSpecGoods(cost, delCount, LogConsume.JEWEL_UP_COST, type);
				if(full){
					break;
				}
			}
			if(full){
				break;
			}
		}
		//更新人物属性
		if(upgrade){
			playerCalculator.calculate(playerId);
		}
		//更新前端
		updateEquip2Client(playerId);
		taskService.doTask(playerId, Task.FINISH_STONE, curCfg.lev, type, 1);
		return Response.SUCCESS;
	}
	
	
	//洗练
	public int clear(int playerId,long id,int lock){
		Goods goods = goodsService.getGoods(playerId, id);
		GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
		if(cfg == null) {
			ServerLogger.warn("goods don't exist id = " + cfg.id);
		}
		//扣除锁定
		if(lock>0){
			if(!playerService.decDiamond(playerId, ConfigData.globalParam().clearCostDiamond, LogConsume.CLEAR_LOCK,goods.getGoodsId() )){
				return Response.NO_DIAMOND;
			}
		}
		//扣除消耗
		if(!playerService.decCoin(playerId, ConfigData.globalParam().clearCostCoin, LogConsume.CLEAR_COST, goods.getGoodsId())){
			return Response.NO_COIN;
		}
		//重新随机
		int addId = cfg.level*1000+cfg.color;
		EquipAddAttrCfg addCfg = ConfigData.getConfig(EquipAddAttrCfg.class, addId);
		goods.getLastAttrs().clear();
		for(int i=0;i<2;i++){
			AttrItem attr = goods.getAddAttrList().get(i);
			if(i==lock-1){
				goods.getLastAttrs().add(attr);
			}else{
				int typeIndex = RandomUtil.getRandomIndex(addCfg.typeRates);
				int type = addCfg.types[typeIndex];
				int[] range = addCfg.parameter.get(type);
				int value = RandomUtil.randInt(range[0], range[1]);
				attr = new AttrItem();
				attr.type = type;
				attr.value = value;
				goods.getLastAttrs().add(attr);
			}
		}
		//更新vo
		goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
		taskService.doTask(playerId, Task.FINISH_CLEAR, 1);
		return Response.SUCCESS;
	}
	
	//替换
	public int replace(int playerId,long id){
		//直接替换
		Goods goods = goodsService.getGoods(playerId, id);
		if(goods.getLastAttrs().isEmpty()){
			return Response.ERR_PARAM;
		}
		//更新前端vo
		goods.getAddAttrList().clear();
		goods.getAddAttrList().addAll(goods.getLastAttrs());
		goods.getLastAttrs().clear();
		goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
		//在身上，更新人物属性
		if(!goods.isInBag()){
			playerCalculator.calculate(playerId);
		}
		return Response.SUCCESS;
	}
	
	//更新装备信息
	public void updateEquip2Client(int playerId){
		SessionManager.getInstance().sendMsg(BagExtension.UPDATE_EQUP, getEquip(playerId), playerId);
	}
	
	//获取装备信息
	public EquipInfo getEquip(int playerId){
		//更新一下宝石的数据
		PlayerData data = playerService.getPlayerData(playerId);
		playerCalculator.initJewel(playerId);
		EquipInfo equip = new  EquipInfo();
		
		equip.strengths = new ArrayList<AttrItem>();
		for(Entry<Integer,Integer> strength:data.getStrengths().entrySet()){
			AttrItem s = new AttrItem();
			s.type = strength.getKey();
			s.value = strength.getValue();
			equip.strengths.add(s);
		}
		equip.jewels = new ArrayList<com.game.params.goods.Jewel>();
		for(Entry<Integer, Jewel> j:data.getJewels().entrySet()){
			com.game.params.goods.Jewel jewel = new com.game.params.goods.Jewel();
			jewel.type = j.getKey();
			jewel.exp = j.getValue().getExp();
			jewel.lev = j.getValue().getLev();
			equip.jewels.add(jewel);
		}


		return equip;
	}
	
}
