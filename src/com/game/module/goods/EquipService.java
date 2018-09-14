package com.game.module.goods;

import com.game.data.*;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.activity.ActivityTask;
import com.game.module.log.LogConsume;
import com.game.module.player.*;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.*;
import com.game.params.goods.AttrItem;
import com.game.params.goods.EquipInfo;
import com.game.params.goods.SGoodsVo;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.game.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EquipService {
    private static final int[] EquipType = new int[]{201, 202, 204, 203, 205, 206};//强化装备的类型顺序

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PlayerCalculator playerCalculator;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivityService activityService;

    // 穿装备
    public int wear(int playerId, List<Long> ids) {
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);
        PlayerBag bag = goodsService.getPlayerBag(playerId);

        for (long id : ids) { //前置判断
            Goods goods = goodsService.getGoods(playerId, id);
            if (goods == null) {
                return Response.ERR_GOODS_TYPE;
            }

            GoodsConfig config = goodsService.getGoodsConfig(goods.getGoodsId());
            System.out.println("==" + config.id);
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
        }

        List<SGoodsVo> vo = new ArrayList<>();
        for (long id : ids) {
            Goods goods = goodsService.getGoods(playerId, id);
            GoodsConfig config = goodsService.getGoodsConfig(goods.getGoodsId());
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
                Integer suitId2 = ConfigData.SuitMap.get(curEquip.getGoodsId());
                if (suitId2 != null) {
                    Set<Integer> suit = data.getSuitMap().get(suitId2);
                    if (suit != null) {
                        suit.remove(curEquip.getGoodsId());
                    }
                }
            }
            goods.setStoreType(Goods.EQUIP);
            Integer suitId = ConfigData.SuitMap.get(goods.getGoodsId());
            if (suitId != null) {
                Set<Integer> suit = data.getSuitMap().get(suitId);
                if (suit == null) {
                    suit = Sets.newHashSet();
                    data.getSuitMap().put(suitId, suit);
                }
                suit.add(goods.getGoodsId());
            }
            vo.add(goodsService.toVO(goods));
        }

        playerCalculator.calculate(player);
        goodsService.refreshGoodsToClient(playerId, vo);

        //装备投资
        equipmentInvestment(playerId, data);
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
        PlayerData data = playerService.getPlayerData(playerId);
        Integer suitId = ConfigData.SuitMap.get(goods.getGoodsId());
        if (suitId != null) {
            Set<Integer> suit = data.getSuitMap().get(suitId);
            if (suit != null) {
                suit.remove(goods.getGoodsId());
            }
        }

        playerCalculator.calculate(player);

        List<SGoodsVo> vo = new ArrayList<SGoodsVo>();
        vo.add(goodsService.toVO(goods));
        goodsService.refreshGoodsToClient(playerId, vo);

        return Response.SUCCESS;
    }

    //分解
    public Object decompose(int playerId, Collection<Long> ids) {
        //计算总的获得
        int goodsId = 0;
        int count = 0;
        boolean bUpdate = false;
        PlayerData data = playerService.getPlayerData(playerId);
        List<GoodsEntry> list = Lists.newArrayList();
        List<Goods> goodList = Lists.newArrayList();
        for (long id : ids) {
//            list.clear();
            int equipMaterials = 0;
            Goods goods = goodsService.getGoods(playerId, id);
            if (goods == null) {
                ServerLogger.warn("goods don't exist id = " + id);
                continue;
            }
            GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
            if (cfg == null) {
                ServerLogger.warn("goods don't exist id = " + id);
                continue;
            }
            goodsId = cfg.decompose[0][0];
            equipMaterials += cfg.decompose[0][1];

            GoodsEntry goodsEntry = new GoodsEntry(cfg.decompose[0][0], cfg.decompose[0][1]);
            list.add(goodsEntry);
            //升星返还
            if (goods.getStar() > 0) {
                EquipStarCfg nextCfg = ConfigData.getConfig(EquipStarCfg.class, cfg.type * 100000 + cfg.level * 100 + goods.getStar());
                if (nextCfg != null) {
                    if (goodsId == Goods.EQUIP_TOOL) {
                        goodsEntry.count += nextCfg.decompose;
                    } else {
                        GoodsEntry goodsEntry1 = new GoodsEntry(Goods.EQUIP_TOOL, nextCfg.decompose);
                        list.add(goodsEntry1);
                    }
                }
            }
            count += equipMaterials;

            goodList.add(goods);

            //扣除物品
//            goodsService.decSpecGoods(goods, goods.getStackNum(), LogConsume.DECOMPOSE_DEC);
//            goodsService.addRewards(playerId, list, LogConsume.DECOMPOSE_DEC);
            if (goods.getStoreType() == Goods.EQUIP) { //goods.setStoreType(Goods.EQUIP);
                bUpdate = true;
            }

            Integer suitId = ConfigData.SuitMap.get(goods.getGoodsId());
            if (suitId != null) {
                Set<Integer> suit = data.getSuitMap().get(suitId);
                if (suit != null) {
                    suit.remove(goods.getGoodsId());
                }
            }
        }

        if (goodList.size() > 0) {
            goodsService.removeBatchGoods(playerId, goodList, LogConsume.DECOMPOSE_DEC);
            goodsService.addRewards(playerId, list, LogConsume.DECOMPOSE_DEC);
        }

        if (bUpdate) {
            playerCalculator.calculate(playerId);
        }
        GainGoodNotify notify = new GainGoodNotify();
        //加奖励
        //playerService.addCurrency(playerId, Goods.EQUIP_TOOL, equipMaterials, LogConsume.DECOMPOSE_ADD);
        taskService.doTask(playerId, Task.FINISH_DECOMPOSE, 1);
        notify.id = goodsId;
        notify.count = count;
        return notify;
    }

    //升阶
    public int upStar(int playerId, Long2Param long2Param) {
        Long id = long2Param.param1;
        PlayerCurrency currency = playerService.getPlayerData(playerId).getCurrency();
        if (currency == null) {
            return Response.ERR_PARAM;
        }

        Goods goods = goodsService.getGoods(playerId, id);
        if (goods == null) {
            ServerLogger.warn("EquipService#upStar id = " + id);
            return Response.OPERATION_TOO_FAST;
        }
        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());

        long value = currency.get(Goods.EQUIP_TOOL);
        int cost = 0;
        int count = 0;
        int nextStar = goods.getStar();

        while (value >= 0) {
            nextStar++;
            EquipStarCfg nextCfg = ConfigData.getConfig(EquipStarCfg.class, cfg.type * 100000 + cfg.level * 100 + nextStar);
            //已经到满星
            if (nextCfg == null) {
                nextStar--;
                break;
            }

            value -= nextCfg.cost;
            if (value >= 0) {
                cost += nextCfg.cost;
                count++;
                //一次升阶
                if (long2Param.param2 == 0) {
                    break;
                }
            } else {
                break;
            }
        }

        //扣除材料
        if (!playerService.decCurrency(playerId, Goods.EQUIP_TOOL, cost, LogConsume.UP_STAR_COST, cfg.id)) {
            return Response.NO_MATERIAL;
        }

        goods.setStar(nextStar);

        taskService.doTask(playerId, Task.FINISH_STAR, nextStar, count);
        //更新物品
        goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
        //属性更新
        if (!goods.isInBag()) {
            playerCalculator.calculate(playerId);
        }

        //已经到满星
//        Goods goods = goodsService.getGoods(playerId, id);
//        if (goods == null) {
//            ServerLogger.warn("EquipService#upStar id = " + id);
//            return Response.OPERATION_TOO_FAST;
//        }
//        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
//        int nextStar = goods.getStar() + 1;
//        EquipStarCfg nextCfg = ConfigData.getConfig(EquipStarCfg.class, cfg.type * 100000 + cfg.level * 100 + nextStar);
//        if (nextCfg == null) {
//            return Response.MAX_STAR;
//        }
//        //扣除材料
//        if (!playerService.decCurrency(playerId, Goods.EQUIP_TOOL, nextCfg.cost, LogConsume.UP_STAR_COST, cfg.id)) {
//            return Response.NO_MATERIAL;
//        }
//        //更新星级
//        goods.setStar(nextStar);
//        //更新物品
//        goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
//        //属性更新
//        if (!goods.isInBag()) {
//            playerCalculator.calculate(playerId);
//        }
//        taskService.doTask(playerId, Task.FINISH_STAR, nextStar, 1);
        return Response.SUCCESS;
    }


    //强化
    public int strength(int playerId, int type, boolean useTicket, int oneKey) {
        //已经到最高级
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);
        List<Integer> types = Lists.newArrayList(type);
        if (oneKey == 1) {
            types = Lists.newArrayList(201, 202, 204, 203, 205, 206);
        }

        int consumeCoin = 0;
        int consumeTicket = 0;
        List<GoodsEntry> goods = new ArrayList<>();
        boolean hasTicket = true;

        EquipStrengthCfg cfg = ConfigData.getConfig(EquipStrengthCfg.class, type * 1000 + 1);
        List<Integer> ids = Lists.newArrayList();
        for (int i = 0; i < cfg.costTools.length; i++) {
            ids.add(cfg.costTools[i][0]);
        }

        //总资源
        Map<Integer, Integer> map = goodsService.getGoods(playerId, ids);

        Map<Integer, Integer> type2level = Maps.newHashMap();
        Map<Integer, Integer> type2task = Maps.newHashMap();
        Map<Integer, Integer> type2cost = Maps.newHashMap();
        out:
        for (int t : types) {
            int taskCount = 0;
            //类型错误
            if (!CommonUtil.contain(ConfigData.globalParam().equipTypes, t)) {
                return Response.ERR_PARAM;
            }
            int curStrength = data.getStrengths().getOrDefault(t,0);
            out2:
            while (true) {
                if (curStrength >= player.getLev()) {
                    break out2;
                }

                int next = curStrength + 1;
                EquipStrengthCfg nextCfg = ConfigData.getConfig(EquipStrengthCfg.class, type * 1000 + next);
                if (nextCfg == null) {
                    break out2;
                }

                if (player.getCoin() < consumeCoin + nextCfg.costCoin) {
                    ServerLogger.info("consumeCoin = " + consumeCoin + ",getCoin = " + player.getCoin());
                    break out2;
                }
                consumeCoin += nextCfg.costCoin;
                if (nextCfg.costTools != null) {
                    for (int i = 0; i < nextCfg.costTools.length; i++) {
                        int count = map.get(nextCfg.costTools[i][0]);
                        if (count < nextCfg.costTools[i][1]) {
                            break out2;
                        }
                    }
                    for (int i = 0; i < nextCfg.costTools.length; i++) {
                        int id = nextCfg.costTools[i][0];
                        int costCount = nextCfg.costTools[i][1];
                        //
                        int count = map.get(id);
                        map.put(id, count - costCount);

                        int cost = type2cost.getOrDefault(id, 0);
                        type2cost.put(id, cost + costCount);
                    }
                }

                int rate = nextCfg.successRate;
                if (useTicket) {
                    if (hasTicket) {
                        //TODO 优化
                        if (goodsService.decGoodsFromBag(playerId, ConfigData.globalParam().strengthTicket, 1, LogConsume.STRENGTH_COST, type)) {
                            rate += ConfigData.globalParam().strengthTicketAdd;
                            consumeTicket += 1;
                        } else {
                            hasTicket = false;
                        }
                    }
                }

                boolean success = RandomUtil.randomHitPercent(rate);
                if (success) {
                    curStrength += 1;
                } else {
                    //材料减半
                    if (goods.size() > 0) {
                        for (GoodsEntry ge : goods) {
                            for (int i = 0; i < nextCfg.costTools.length; i++) {
                                if (ge.id == nextCfg.costTools[i][0]) {
                                    ge.count -= nextCfg.costTools[i][1] >> 1;
                                    break;
                                }
                            }
                        }
                    }
                    //返还金币
                    consumeCoin -= nextCfg.costCoin >> 1;
                }
                type2level.put(t, curStrength);
                taskCount += 1;
                type2task.put(t, taskCount);
                if (oneKey == 0) {
                    break out;
                }
            }
        }
        //goodsService.decGoodsFromBag(playerId, ConfigData.globalParam().strengthTicket, consumeTicket, LogConsume.STRENGTH_COST, types);
        if (!type2cost.isEmpty()) {
            for (Map.Entry<Integer, Integer> e : type2cost.entrySet()) {
                GoodsEntry ge = new GoodsEntry(e.getKey(), e.getValue());
                goods.add(ge);
            }

            //扣除金币
            playerService.decCoin(playerId, consumeCoin, LogConsume.STRENGTH_COST, types);
            //扣除材料
            goodsService.decConsume(playerId, goods, LogConsume.STRENGTH_COST, types);
            //设置强化等级
            data.getStrengths().putAll(type2level);
            //更新装备位属性
            playerCalculator.calculate(playerId);
            //更新前端数据
            updateEquip2Client(playerId);

        }
        if (!type2task.isEmpty()) {
            //强化任务
            Map<Integer, int[]> condParams = Maps.newHashMap();
            for (Map.Entry<Integer, Integer> e : type2task.entrySet()) {
                condParams.put(Task.FINISH_STRONG, new int[]{type2level.get(e.getKey()), e.getKey(), e.getValue()});
                taskService.doTask(playerId, condParams);
            }
        }
        int result = Response.SUCCESS;
        return result;
    }


    //升级宝石
    public int upJewel(int playerId, int type) {
        //参数验证
        if (!CommonUtil.contain(ConfigData.globalParam().equipTypes, type)) {
            return Response.ERR_PARAM;
        }
        //已经满级了
        PlayerData data = playerService.getPlayerData(playerId);
        Jewel jewel = data.getJewels().get(type);
        if (jewel == null) {
            jewel = new Jewel();
            data.getJewels().put(type, jewel);
        }

        int next = jewel.getLev() + 1;
        EquipJewelCfg nextCfg = ConfigData.getConfig(EquipJewelCfg.class, type * 1000 + next);
        if (nextCfg == null) {
            return Response.MAX_LEV;
        }
        EquipJewelCfg curCfg = ConfigData.getConfig(EquipJewelCfg.class, type * 1000 + jewel.getLev());
        //依次扣除，直到满级
        boolean full = false;
        boolean upgrade = false;
        for (int id : ConfigData.globalParam().jewelCost.get(type)) {
            Collection<Goods> costs = goodsService.getExistBagGoods(playerId, id);
            for (Goods cost : costs) {
                GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, cost.getGoodsId());
                int delCount = 0;
                int count = cost.getStackNum();
                for (int i = 1; i <= count; i++) {
                    delCount++;
                    int addExp = cfg.decompose[0][1];
                    jewel.setExp(jewel.getExp() + addExp);
                    //更新经验，等级
                    while (jewel.getExp() >= curCfg.exp) {
                        jewel.setLev(jewel.getLev() + 1);
                        jewel.setExp(jewel.getExp() - curCfg.exp);
                        upgrade = true;

                        curCfg = ConfigData.getConfig(EquipJewelCfg.class, type * 1000 + jewel.getLev());
                        nextCfg = ConfigData.getConfig(EquipJewelCfg.class, type * 1000 + jewel.getLev() + 1);
                        full = (nextCfg == null);
                        if (full || jewel.getExp() < curCfg.exp) {
                            break;
                        }
                    }
                    if (full) {
                        break;
                    }
                }
                goodsService.decSpecGoods(cost, delCount, LogConsume.JEWEL_UP_COST, type);
                if (full) {
                    break;
                }
            }
            if (full) {
                break;
            }
        }
        //更新人物属性
        if (upgrade) {
            playerCalculator.calculate(playerId);
        }
        //更新前端
        updateEquip2Client(playerId);
        //taskService.doTask(playerId, Task.FINISH_STONE, curCfg.lev, type, 1);
        //taskService.doTask(playerId,Task.TYPE_BS_LEVEL,type,jewel.getLev());
        Map<Integer, int[]> condParam = Maps.newHashMapWithExpectedSize(2);
        condParam.put(Task.FINISH_STONE, new int[]{curCfg.lev, type, 1});
        condParam.put(Task.TYPE_BS_LEVEL, new int[]{type, jewel.getLev()});
        taskService.doTask(playerId, condParam);
        return Response.SUCCESS;
    }


    //洗练
    public IntParam clear(int playerId, long id, int lock) {
        Goods goods = goodsService.getGoods(playerId, id);
        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
        IntParam param = new IntParam();
        if (cfg == null) {
            ServerLogger.warn("goods don't exist id = " + id);
            param.param = Response.ERR_PARAM;
            return param;
        }

        Player player = playerService.getPlayer(playerId);
        //扣除锁定
        if (lock > 0) {
            if (player.getDiamond() < ConfigData.globalParam().clearCostDiamond) {
                param.param = Response.NO_DIAMOND;
                return param;
            }
        }
        //扣除消耗
        if (!goodsService.decGoodsFromBag(playerId, Goods.CLEAR_ITEM, ConfigData.globalParam().clearCostCoin, LogConsume.CLEAR_COST, goods.getGoodsId())) {
            param.param = Response.NO_MATERIAL;
            return param;
        }
        if (lock > 0) {
            playerService.decDiamond(playerId, ConfigData.globalParam().clearCostDiamond, LogConsume.CLEAR_LOCK, goods.getGoodsId());
        }
        //重新随机
        int addId = cfg.level * 1000 + cfg.color;
        EquipAddAttrCfg addCfg = ConfigData.getConfig(EquipAddAttrCfg.class, addId);
        goods.getLastAttrs().clear();
        for (int i = 0; i < 2; i++) {
            AttrItem attr = goods.getAddAttrList().get(i);
            if (i == lock - 1) {
                goods.getLastAttrs().add(attr);
            } else {
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

        taskService.doTask(playerId, Task.FINISH_CLEAR, 1);
        //更新vo
        goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
        param.param = Response.SUCCESS;
        return param;
    }

    //替换
    public int replace(int playerId, long id) {
        //直接替换
        Goods goods = goodsService.getGoods(playerId, id);
        if (goods.getLastAttrs().isEmpty()) {
            return Response.ERR_PARAM;
        }
        //更新前端vo
        goods.getAddAttrList().clear();
        goods.getAddAttrList().addAll(goods.getLastAttrs());
        goods.getLastAttrs().clear();
        goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
        //在身上，更新人物属性
        if (!goods.isInBag()) {
            playerCalculator.calculate(playerId);
        }
        return Response.SUCCESS;
    }

    //更新装备信息
    public void updateEquip2Client(int playerId) {
        SessionManager.getInstance().sendMsg(BagExtension.UPDATE_EQUP, getEquip(playerId), playerId);
    }

    //获取装备信息
    public EquipInfo getEquip(int playerId) {
        //更新一下宝石的数据
        PlayerData data = playerService.getPlayerData(playerId);
        playerCalculator.initJewel(playerId);
        EquipInfo equip = new EquipInfo();

        equip.strengths = new ArrayList<AttrItem>();
        for (Entry<Integer, Integer> strength : data.getStrengths().entrySet()) {
            AttrItem s = new AttrItem();
            s.type = strength.getKey();
            s.value = strength.getValue();
            equip.strengths.add(s);
        }
        equip.jewels = new ArrayList<com.game.params.goods.Jewel>();
        for (Entry<Integer, Jewel> j : data.getJewels().entrySet()) {
            com.game.params.goods.Jewel jewel = new com.game.params.goods.Jewel();
            jewel.type = j.getKey();
            jewel.exp = j.getValue().getExp();
            jewel.lev = j.getValue().getLev();
            equip.jewels.add(jewel);
        }
        return equip;
    }


    public List<Integer> getBufferList(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<Integer> bufferList = Lists.newArrayList();
        for (Map.Entry<Integer, Set<Integer>> s : data.getSuitMap().entrySet()) {
            SuitConfig config = ConfigData.getConfig(SuitConfig.class, s.getKey());
            if (s.getValue().size() >= 2) {
                for (int id : config.buffAdd[1]) {
                    if (id != 0) {
                        bufferList.add(id);
                    }
                }
            }
            if (s.getValue().size() >= 3) {
                for (int id : config.buffAdd[2]) {
                    if (id != 0) {
                        bufferList.add(id);
                    }
                }
            }
            if (s.getValue().size() >= 4) {
                for (int id : config.buffAdd[3]) {
                    if (id != 0) {
                        bufferList.add(id);
                    }
                }
            }
            if (s.getValue().size() >= 5) {
                for (int id : config.buffAdd[4]) {
                    if (id != 0) {
                        bufferList.add(id);
                    }
                }
            }
            if (s.getValue().size() >= 6) {
                for (int id : config.buffAdd[5]) {
                    if (id != 0) {
                        bufferList.add(id);
                    }
                }
            }
        }
        return bufferList;
    }

    //统计装备物品品质和数量
    public Map<Integer, Integer> getTypeNumberMap(int playerId) {
        Map<Integer, Integer> goodsMap = new ConcurrentHashMap<>();
        Collection<Goods> collection = goodsService.getPlayerBag(playerId).getAllGoods().values();
        if (collection.isEmpty()) {
            return goodsMap;
        }
        for (Goods goods : collection) {
            //是否穿戴在身上
            if (goods.getStoreType() != 1) {
                continue;
            }
            GoodsConfig goodsConfig = goodsService.getGoodsConfig(goods.getGoodsId());
            if (goodsConfig != null && goodsConfig.color >= 1) {
                for (int i = 1; i <= goodsConfig.color; i++) {
                    //高级品质装备也算低级品质的数量
                    if (goodsMap.get(i) == null) {
                        goodsMap.put(i, 1);
                    } else {
                        goodsMap.put(i, goodsMap.get(i) + 1);
                    }
                }
            }
        }
        return goodsMap;
    }

    //装备投资
    public void equipmentInvestment(int playerId, PlayerData data) {
        if (activityService.checkIsOpen(data, ActivityConsts.ActivityTaskCondType.T_EQUIPMENT_INVESTMENT)) {
            Map<Integer, Integer> typeNumberMap = getTypeNumberMap(playerId);
            if (typeNumberMap != null && !typeNumberMap.isEmpty()) {
                activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_EQUIPMENT_INVESTMENT, true, typeNumberMap, true);
            }
        }
    }

    /**
     * 寻找下一个可强化的部位
     *
     * @param type 装备部位类型
     * @return 下一个可强化的部位类型
     */
    public int findNextType(int type) {
        int nextType = -1;
        for (int i = 0; i < EquipType.length; i++) {
            if (type == EquipType[i]) {
                if (i == EquipType.length - 1) {
                    nextType = EquipType[0];
                } else {
                    nextType = EquipType[i + 1];
                }
                break;
            }
        }
        return nextType;
    }
}
