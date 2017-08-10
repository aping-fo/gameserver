package com.game.module.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.CopyConfig;
import com.game.data.GlobalConfig;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.data.ShopCfg;
import com.game.data.VIPConfig;
import com.game.module.copy.CopyInstance;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.params.Int2Param;
import com.game.params.ShopInfo;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.game.util.TimeUtil;
import com.server.util.GameData;
import com.server.util.ServerLogger;

/**
 * 商城
 */
@Service
public class ShopService {

	public static final int COMMON = 1;
	public static final int MYSTERY = 2;
	public static final int ENDLESS = 3;
	public static final int GANG = 4;
	public static final int TRAINING = 5;
	public static final int AI_ARENA = 6;
	
	public static final int[] SHOP_TYPES = { COMMON, ENDLESS, GANG, TRAINING, AI_ARENA };
	public static final int LIMIT_DAILY = 1;
	public static final int LIMIT_REFRESH = 2;

	public static final int REFRESH = 1;

	@Autowired
	private SerialDataService serial;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;

	// 获取商城信息
	public ShopInfo getInfo(int playerId, int type) {
		PlayerData data = playerService.getPlayerData(playerId);
			
		if(type == MYSTERY && (System.currentTimeMillis() - data.getMysteryShopTime() > ConfigData.globalParam().mysteryShopTime * TimeUtil.ONE_MIN)){
			return null;
		}
		
		ShopInfo shop = new ShopInfo();
		shop.type = type;

		// 刷新商品
		ConcurrentHashMap<Integer, List<Integer>> refreshes = serial.getData().getPlayerRefreshShops().get(type);
		if (refreshes == null) {
			refreshes = new ConcurrentHashMap<Integer, List<Integer>>();
			refreshes = serial.getData().getPlayerRefreshShops().putIfAbsent(type, refreshes);
		}
		List<Integer> myRefreshes = refreshes.get(playerId);
		if (myRefreshes == null) {
			myRefreshes = genRefreshs(playerId, type);
			refreshes.put(playerId, myRefreshes);
			//同时清除购买记录
			refreshBuyRecord(playerId, type, LIMIT_REFRESH);
		}
		shop.refreshShopIds = new ArrayList<Integer>(myRefreshes);
		// 刷新次数
		if (data.getShopRefreshCount().containsKey(type)) {
			shop.refreshCount = data.getShopRefreshCount().get(type);
		}
		// 限购记录
		shop.limitShops = new ArrayList<Int2Param>();
		for (int id : data.getShopBuyRecords().keySet()) {
			ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, id);
			if (cfg == null || cfg.shopType != type) {
				continue;
			}
			Int2Param limit = new Int2Param();
			limit.param1 = id;
			limit.param2 = data.getShopBuyRecords().get(id);
			shop.limitShops.add(limit);
		}
		return shop;
	}

	// 生成刷新商品
	public List<Integer> genRefreshs(int playerId, int type) {
		Player player = playerService.getPlayer(playerId);

		List<Integer> ids = new ArrayList<Integer>(ConfigData.RefreshIds.get(type));
		List<Integer> rates = new ArrayList<Integer>(ConfigData.RefreshRates.get(type));

		List<Integer> result = new ArrayList<Integer>();
		int size = ids.size();
		for(int i = 0; i < size; i++){
			ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, ids.get(i));
			if(cfg.isFixed){
				result.add(ids.get(i));
				rates.set(i, 0);
			}
		}
		if(result.size() < 6){			
			for (int i = 0; i < 60; i++) {
				int index = RandomUtil.getRandomIndex(rates);
				// 过滤职业
				ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, ids.get(index));
				if(cfg == null) {
					ServerLogger.warn("ErrRefreshId：" + ids.get(index));
					continue;
				}
				GoodsConfig gCfg = ConfigData.getConfig(GoodsConfig.class, cfg.goodsId);
				if (gCfg == null) {
					ServerLogger.warn("ErrRefreshId：" + cfg.id);
					continue;
				}
				if ((gCfg.vocation != 0 && gCfg.vocation != player.getVocation())) {
					continue;
				}
				// 处理一下权重
				result.add(ids.get(index));
				rates.set(index, 0);
				if (result.size() >= 6) {
					break;
				}
			}
		}
		return result;
	}

	// 购买
	public int buy(int playerId, int id, int count) {
		PlayerData data = playerService.getPlayerData(playerId);
		Player player = playerService.getPlayer(playerId);
		// 刷新，id，是否已经购买过
		ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, id);
		if (cfg == null || count <= 0) {
			return Response.ERR_PARAM;
		}
		if (cfg.tab == REFRESH) {
			//count = 1;
			ConcurrentHashMap<Integer, List<Integer>> map = serial.getData().getPlayerRefreshShops().get(cfg.shopType);
			if (map == null || !map.get(playerId).contains(id)) {
				return Response.SHOP_HAS_REFRESH;
			}
		}
		// 限购数量
		if (cfg.limitCount > 0) {
			Integer buyCount = data.getShopBuyRecords().get(id);
			if (buyCount != null) {
				VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
				int limit = cfg.limitCount;
				if(cfg.isCanAddBuy){
					limit *= 1 + vip.addBuy;
				}
				if ((buyCount + count) > limit) {
					return Response.ERR_PARAM;
				}
			}
		}
		// vip验证
		if (player.getVip() < cfg.vip) {
			return Response.NO_VIP;
		}
		// 职业计算
		GoodsConfig g = ConfigData.getConfig(GoodsConfig.class, cfg.goodsId);
		if (g == null || (g.vocation > 0 && g.vocation != player.getVocation())) {
			return Response.NO_VOCATION;
		}
		// 背包检查
		if (!goodsService.checkCanAdd(playerId, cfg.goodsId, cfg.count * count)) {
			return Response.BAG_FULL;
		}
		// 折扣计算
		int discount = 0;
		if (cfg.vipDiscount != null && player.getVip() > 0) {
			if (player.getVip() > cfg.vipDiscount.length) {
				discount = cfg.vipDiscount[cfg.vipDiscount.length - 1];
			} else {
				discount = cfg.vipDiscount[player.getVip() - 1];
			}
		}
		int price = cfg.moneyCount;
		if (discount > 0) {
			price = (int) (price * discount / 100.0f);
		}
		price *= count;
		// 扣钱
		int code = goodsService.decConsume(playerId, Arrays.asList(new GoodsEntry(cfg.moneyType, price)),
				LogConsume.SHOP_BUY_COST, id);
		if (code != Response.SUCCESS) {
			return code;
		}

		if (cfg.limitCount > 0) {
			Integer buyCount = data.getShopBuyRecords().get(id);
			if (buyCount == null) {
				buyCount = 0;
			}
			buyCount += count;
			data.getShopBuyRecords().put(id, buyCount);
		}
		goodsService.addRewrad(playerId, cfg.goodsId, cfg.count * count, LogConsume.SHOP_BUY_ADD, id);
		return Response.SUCCESS;
	}

	// 刷新
	public int refresh(int playerId, int type) {
		Player player = playerService.getPlayer(playerId);
		PlayerData data = playerService.getPlayerData(playerId);
		VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
		// 验证次数
		Integer count = data.getShopRefreshCount().get(type);
		if (count == null) {
			count = 0;
		}
		if (count >= vip.shopRefreshTimes.get(type)) {
			return Response.NO_TODAY_TIMES;
		}
		// 验证价格
		int[] prices = ConfigData.globalParam().shopRefreshPrice.get(type);
		int priceIndex = Math.min(count + 1, prices.length - 1);//第一位为货币类型
		int price = prices[priceIndex];
		if(price>0){
			if (goodsService.decConsume(playerId, Arrays.asList(new GoodsEntry(prices[0], price)),
				LogConsume.SHOP_REFRESH_COST, type) > 0) {
				return Response.NO_CURRENCY;
			}
		}
		// 去除购买记录
		serial.getData().getPlayerRefreshShops().get(type).remove(playerId);
		refreshBuyRecord(playerId, type, LIMIT_REFRESH);
		count++;
		data.getShopRefreshCount().put(type, count);
		return Response.SUCCESS;
	}

	// 定时刷新
	public void refreshCommon() {
		for(int type : SHOP_TYPES){			
			serial.getData().getPlayerRefreshShops().remove(type);
		}
	}
	
	public void dailyReset(int playerId){
		PlayerData data = playerService.getPlayerData(playerId);
		data.getShopRefreshCount().clear();
		refreshBuyRecord(playerId, -1, ShopService.LIMIT_DAILY);
	}

	/**
	 * 用于刷新玩家的商店购买记录，
	 * @param playerId
	 * @param shopType 商店类型，如果为－1即表示所以商店，
	 * @param limitType
	 */
	public void refreshBuyRecord(int playerId, int shopType, int limitType){
		PlayerData data = playerService.getPlayerData(playerId);
		Enumeration<Integer> keys = data.getShopBuyRecords().keys();
		while(keys.hasMoreElements()){
			int itemId = keys.nextElement();
			ShopCfg gc = GameData.getConfig(ShopCfg.class, itemId);
			if(gc == null || ((shopType == -1 || gc.shopType == shopType) && gc.limitType == limitType)){
				data.getShopBuyRecords().remove(itemId);
			}
		}
	}
	
	private static final int _base = 0;//基础
	private static final int _3star = 1;//三星
	private static final int _sweep = 2;//扫荡
	private static final int _special = 3;//特殊关卡
	
	public boolean triggerMysteryShop(int playerId, int copyId, CopyResult result){
		boolean show = false;
		Player player = playerService.getPlayer(playerId);
		PlayerData data = playerService.getPlayerData(playerId);
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
		
		if(cfg.type == CopyInstance.TYPE_TRAVERSING || cfg.needEnergy == 0){
			return false;
		}
		data.setPower4Mystery(data.getPower4Mystery() + cfg.needEnergy);

		GlobalConfig global = ConfigData.globalParam();
		if(System.currentTimeMillis() - data.getMysteryShopTime() < global.mysteryShopTime * TimeUtil.ONE_MIN){
			return false;
		}
		if(data.getPower4Mystery() < global.mysteryTriggerPower){
			return false;
		}
		int property = global.mysteryProperty[_base];
		
		if(result == null){
			property += global.mysteryProperty[_sweep];
		}else{			
			//是否为三星
			if(result.star == 3){
				property += global.mysteryProperty[_3star];
			}
		}
		//VIP等级
		VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
		property += vip.mysteryProperty;
		if(ConfigData.copy4Mystery.contains(copyId)){
			property += global.mysteryProperty[_special];
		}
		if(property >= RandomUtil.randInt(100)){
			show = true;
			data.setPower4Mystery(0);
			data.setMysteryShopTime(System.currentTimeMillis());
			refreshBuyRecord(playerId, MYSTERY, LIMIT_REFRESH);
			Map<Integer, List<Integer>> map = serial.getData().getPlayerRefreshShops().get(MYSTERY);
			if(map != null){				
				map.remove(playerId);
			}
		}
		
		return show;
	}
}
