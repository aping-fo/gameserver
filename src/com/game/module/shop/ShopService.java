package com.game.module.shop;

import com.game.data.*;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.copy.CopyInstance;
import com.game.module.fame.FameService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.BuyShopVO;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ShopInfo;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商城
 */
@Service
public class ShopService {

    public static final int COMMON = 1;
    public static final int MYSTERY = 2;//黑市商店
    public static final int ENDLESS = 3;
    public static final int GANG = 4;//公会商店
    public static final int TRAINING = 5;
    public static final int AI_ARENA = 6;
    public static final int FAME_7 = 7;
    public static final int FAME_8 = 8;
    public static final int FAME_9 = 9;
    public static final int FAME_10 = 10;
    public static final int FAME_11 = 11;
    public static final int FAME_12 = 12;
    public static final int FAME_13 = 13;
    public static final int FAME_14 = 14;
    public static final int FAME_15 = 15;
    public static final int CQ = 16;

    private static final int[] SHOP_TYPES = {COMMON, ENDLESS, GANG, TRAINING, AI_ARENA, FAME_7, FAME_8, FAME_9, FAME_10, FAME_11, FAME_12, FAME_13, FAME_14, FAME_15, CQ};
    public static final int LIMIT_DAILY = 1;
    public static final int LIMIT_REFRESH = 2;

    public static final int REFRESH = 1;

    @Autowired
    private SerialDataService serial;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private FameService fameService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivityService activityService;

    // 获取商城信息
    public ShopInfo getInfo(int playerId, int type) {
        PlayerData data = playerService.getPlayerData(playerId);

        if (type == MYSTERY && (System.currentTimeMillis() - data.getMysteryShopTime() > ConfigData.globalParam().mysteryShopTime * TimeUtil.ONE_MIN)) {
            return null;
        }

        ShopInfo shop = new ShopInfo();
        shop.type = type;

        if (ConfigData.RefreshIds.containsKey(type)) {
            // 刷新商品
            ConcurrentHashMap<Integer, List<Integer>> refreshes = serial.getData().getPlayerRefreshShops().get(type);
            if (refreshes == null) {
                refreshes = new ConcurrentHashMap<>();
                refreshes = serial.getData().getPlayerRefreshShops().putIfAbsent(type, refreshes);
                if (refreshes == null) {
                    refreshes = serial.getData().getPlayerRefreshShops().get(type);
                }
            }
            List<Integer> myRefreshes = refreshes.get(playerId);
            if (myRefreshes == null) {
                myRefreshes = genRefreshs(playerId, type);
                refreshes.put(playerId, myRefreshes);
                //同时清除购买记录
                refreshBuyRecord(playerId, type, LIMIT_REFRESH);
            }
            shop.refreshShopIds = new ArrayList<>(myRefreshes);
        }

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
        for (int i = 0; i < size; i++) {
            ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, ids.get(i));
            if (cfg.isFixed) {
                result.add(ids.get(i));
                rates.set(i, 0);
            }
        }

        int n = 6;
        if (type == COMMON) {
            n = 10;
        } else if (type == FAME_7 || type == FAME_8 || type == FAME_9 || type == FAME_10 || type == FAME_11 || type == FAME_12 || type == FAME_13 || type == FAME_14) {
            n = 8;
        }

        if (result.size() < n) {
            for (int i = 0; i < 60; i++) {
                int index = RandomUtil.getRandomIndex(rates);
                // 过滤职业
                ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, ids.get(index));
                if (cfg == null) {
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
                int itemId = ids.get(index);
                if (!result.contains(itemId)) {
                    result.add(itemId);
                    rates.set(index, 0);
                }

                if (result.size() >= n) {
                    break;
                }
            }
        }
        return result;
    }

    // 购买
    public BuyShopVO buy(int playerId, int id, int count) {
        BuyShopVO vo = new BuyShopVO();
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);
        // 刷新，id，是否已经购买过
        ShopCfg cfg = ConfigData.getConfig(ShopCfg.class, id);
        if (cfg == null || count <= 0) {
            vo.errCode = Response.ERR_PARAM;
            return vo;
        }

        //阵营等级判断
        if (cfg.fameLevLimit != null) {
            if (!fameService.checkFameShopBuy(playerId, cfg.fameLevLimit[0], cfg.fameLevLimit[1])) {
                vo.errCode = Response.ERR_PARAM;
                return vo;
            }
        }
        if (cfg.tab == REFRESH) {
            //count = 1;
            ConcurrentHashMap<Integer, List<Integer>> map = serial.getData().getPlayerRefreshShops().get(cfg.shopType);
            if (map == null || !map.get(playerId).contains(id)) {
                vo.errCode = Response.SHOP_HAS_REFRESH;
                return vo;
            }
        }

        // 限购数量
        if (cfg.limitCount > 0) {
            Integer buyCount = data.getShopBuyRecords().get(id);
            if (buyCount != null) {
                VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
                int limit = cfg.limitCount;
                if (cfg.isCanAddBuy) {
                    limit *= 1 + vip.addBuy;
                }
                if ((buyCount + count) > limit) {
                    vo.errCode = Response.ERR_PARAM;
                    return vo;
                }
            }
        }
        // vip验证
        if (player.getVip() < cfg.vip) {
            vo.errCode = Response.NO_VIP;
            return vo;
        }
        // 职业计算
        GoodsConfig g = ConfigData.getConfig(GoodsConfig.class, cfg.goodsId);
        if (g == null || (g.vocation > 0 && g.vocation != player.getVocation())) {
            vo.errCode = Response.NO_VOCATION;
            return vo;
        }
        // 背包检查
        if (!goodsService.checkCanAdd(playerId, cfg.goodsId, cfg.count * count)) {
            vo.errCode = Response.BAG_FULL;
            return vo;
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

        //int lkPrice = cfg.moneyCount;
        //int lkDiscountPrice = price;

        if (cfg.moneyType != -1) {
            price *= count;
            // 扣钱
            int code = goodsService.decConsume(playerId, Arrays.asList(new GoodsEntry(cfg.moneyType, price)),
                    LogConsume.SHOP_BUY_COST, id);
            if (code != Response.SUCCESS) {
                vo.errCode = code;
                return vo;
            }
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

        // int subjectId = cfg.goodsId == Goods.DIAMOND ? 5 : 4;
        data.setBuyCount(data.getBuyCount() + 1);
        taskService.doTask(playerId, Task.TYPE_SHOP_BUY_COUNT, data.getBuyCount());
        vo.errCode = Response.SUCCESS;
        vo.id = id;
        vo.count = count;

        if (cfg.moneyType == -1) {
            ShopInfo shopInfo = getInfo(playerId, COMMON);
            SessionManager.getInstance().sendMsg(1701, shopInfo, playerId);
        }

        //商店购买活动
        if (cfg.shopType == COMMON || cfg.shopType == GANG || cfg.shopType == MYSTERY) {
            PlayerData playerData = playerService.getPlayerData(playerId);
            if (playerData != null) {
                if (activityService.checkIsOpen(playerData, ActivityConsts.ActivityTaskCondType.T_STORE_PURCHASE)) {
                    Map<Integer, Integer> typeNumberMap = getTypeNumberMap(playerId, cfg.shopType);
                    if (typeNumberMap != null && !typeNumberMap.isEmpty()) {
                        activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_STORE_PURCHASE, true, typeNumberMap, true);
                    }
                }
            } else {
                ServerLogger.warn("玩家数据不存在，玩家id=" + playerId);
            }
        }

        return vo;
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
        ShopTypeCfg conf = ConfigData.getConfig(ShopTypeCfg.class, type);
        int[] prices = conf.refreshPrice;
        int priceIndex = Math.min(count + 1, prices.length - 1);//第一位为货币类型
        int price = prices[priceIndex];
        if (price > 0) {
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
        for (int type : SHOP_TYPES) {
            serial.getData().getPlayerRefreshShops().remove(type);
        }
    }

    public void dailyReset(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        data.getShopRefreshCount().clear();
        refreshBuyRecord(playerId, -1, ShopService.LIMIT_DAILY);

        Player player = playerService.getPlayer(playerId);
        data.setBuyEnergyTimes(0);
        data.setBuyCoinTimes(0);
    }

    /**
     * 用于刷新玩家的商店购买记录，
     *
     * @param playerId
     * @param shopType  商店类型，如果为－1即表示所以商店，
     * @param limitType
     */
    public void refreshBuyRecord(int playerId, int shopType, int limitType) {
        PlayerData data = playerService.getPlayerData(playerId);
        Enumeration<Integer> keys = data.getShopBuyRecords().keys();
        while (keys.hasMoreElements()) {
            int itemId = keys.nextElement();
            ShopCfg gc = GameData.getConfig(ShopCfg.class, itemId);
            if (gc == null || ((shopType == -1 || gc.shopType == shopType) && gc.limitType == limitType)) {
                data.getShopBuyRecords().remove(itemId);
            }
        }
    }

    private static final int _base = 0;//基础
    private static final int _3star = 1;//三星
    private static final int _sweep = 2;//扫荡
    private static final int _special = 3;//特殊关卡

    public boolean triggerMysteryShop(int playerId, int copyId, int times, CopyResult result) {
        boolean show = false;
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);

        if (cfg.type == CopyInstance.TYPE_TRAVERSING || cfg.needEnergy == 0) {
            return false;
        }
        data.setPower4Mystery(data.getPower4Mystery() + cfg.needEnergy * times);

        GlobalConfig global = ConfigData.globalParam();
        if (System.currentTimeMillis() - data.getMysteryShopTime() < global.mysteryShopTime * TimeUtil.ONE_MIN) {
            return false;
        }
        if (data.getPower4Mystery() < global.mysteryTriggerPower) {
            return false;
        }
        int property = global.mysteryProperty[_base];

        if (result == null) {
            property += global.mysteryProperty[_sweep];
        } else {
            //是否为三星
            if (result.star == 3) {
                property += global.mysteryProperty[_3star];
            }
        }
        //VIP等级
        VIPConfig vip = ConfigData.getConfig(VIPConfig.class, player.getVip());
        property += vip.mysteryProperty;
        if (ConfigData.copy4Mystery.contains(copyId)) {
            property += global.mysteryProperty[_special];
        }
        if (property >= RandomUtil.randInt(100)) {
            show = true;
            data.setPower4Mystery(0);
            data.setMysteryShopTime(System.currentTimeMillis());
            refreshBuyRecord(playerId, MYSTERY, LIMIT_REFRESH);
            Map<Integer, List<Integer>> map = serial.getData().getPlayerRefreshShops().get(MYSTERY);
            if (map != null) {
                map.remove(playerId);
            }
        }

        return show;
    }

    /**
     * 购买体力
     *
     * @param playerId
     * @return
     */
    public Int2Param buyEnergy(int playerId) {
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);
        VIPConfig config = ConfigData.getConfig(VIPConfig.class, player.getVip());
        Int2Param cli = new Int2Param();
        if (data.getBuyEnergyTimes() > config.buyEnergy) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }
        int index = data.getBuyEnergyTimes();
        if (index >= ConfigData.globalParam().energyPrice.length - 1) {
            index = ConfigData.globalParam().energyPrice.length - 1;
        }
        int price = ConfigData.globalParam().energyPrice[index];
        boolean ret = playerService.decDiamond(playerId, price, LogConsume.BUY_ENERGY);
        if (!ret) {
            cli.param1 = Response.NO_DIAMOND;
            return cli;
        }

        data.setBuyEnergyTimes(data.getBuyEnergyTimes() + 1);

        playerService.addEnergy(playerId, ConfigData.globalParam().addEnergyPerTime, LogConsume.BUY_ENERGY);
        cli.param1 = Response.SUCCESS;
        cli.param2 = data.getBuyEnergyTimes();

        //购买体力活动
        activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_BUYING_STRENGTH);

        return cli;
    }

    /**
     * 购买金币
     *
     * @param playerId
     * @return
     */
    public Int2Param buyCoin(int playerId) {
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);
        Int2Param cli = new Int2Param();
        VIPConfig config = ConfigData.getConfig(VIPConfig.class, player.getVip());
        if (data.getBuyCoinTimes() > config.buyCoin) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }

        int index = data.getBuyCoinTimes();
        if (index >= ConfigData.globalParam().coinPrice.length - 1) {
            index = ConfigData.globalParam().coinPrice.length - 1;
        }
        int price = ConfigData.globalParam().coinPrice[index];
        boolean ret = true;
        if (price > 0) {
            ret = playerService.decDiamond(playerId, price, LogConsume.BUY_COIN);
        }
        if (!ret) {
            cli.param1 = Response.NO_DIAMOND;
            return cli;
        }
        data.setBuyCoinTimes(data.getBuyCoinTimes() + 1);

        playerService.addCoin(playerId, ConfigData.globalParam().coinBuy, LogConsume.BUY_COIN);
        cli.param1 = Response.SUCCESS;
        cli.param2 = data.getBuyCoinTimes();

        //购买金币活动
        activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_BUYING_ALCHEMY);

        return cli;
    }

    public IntParam getBuyEnergyTimes(int playerId) {
        IntParam cli = new IntParam();
        PlayerData data = playerService.getPlayerData(playerId);
        cli.param = data.getBuyEnergyTimes();
        return cli;
    }

    public IntParam getBuyCoinTimes(int playerId) {
        IntParam cli = new IntParam();
        PlayerData data = playerService.getPlayerData(playerId);
        cli.param = data.getBuyCoinTimes();
        return cli;
    }

    private Map<Integer, Integer> getTypeNumberMap(int playerId, int type) {
        Map<Integer, Integer> typeNumberMap = new ConcurrentHashMap<>();

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在,玩家id=" + playerId);
            return typeNumberMap;
        }

        //商店已有物品
        ConcurrentHashMap<Integer, List<Integer>> integerListConcurrentHashMap = serial.getData().getPlayerRefreshShops().get(type);
        if (integerListConcurrentHashMap == null || integerListConcurrentHashMap.isEmpty()) {
            ServerLogger.warn("未购买任何物品");
            return typeNumberMap;
        }

        //商店已有物品列表
        List<Integer> list = integerListConcurrentHashMap.get(playerId);
        if (list == null || list.isEmpty()) {
            ServerLogger.warn("商店物品为空");
            return typeNumberMap;
        }

        //玩家购买物品
        ConcurrentHashMap<Integer, Integer> shopBuyRecords = playerData.getShopBuyRecords();
        if (shopBuyRecords == null || shopBuyRecords.isEmpty()) {
            ServerLogger.warn("玩家未购买任何商品");
            return typeNumberMap;
        }

        //全部购买的次数
        Map<Integer, Integer> shopBuyAllMap = playerData.getShopBuyAllMap();
        if (shopBuyAllMap == null) {
            ServerLogger.warn("商店物品全部购买数据错误");
            return typeNumberMap;
        }

        //购买物品次数
        int count = 0;
        for (Integer itemId : list) {
            if (shopBuyRecords.containsKey(itemId)) {
                count++;
            }
        }

        //叠加之前全部购买次数
        Integer value = shopBuyAllMap.get(type);
        if (count >= 6) {
            if (value != null) {
                shopBuyAllMap.put(type, value + 1);
            } else {
                shopBuyAllMap.put(type, 1);
            }
        }

        if (value != null) {
            count += value * 6;
        }

        typeNumberMap.put(type, count);

        return typeNumberMap;
    }
}
