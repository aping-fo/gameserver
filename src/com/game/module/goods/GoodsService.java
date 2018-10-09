package com.game.module.goods;

import com.game.data.*;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.artifact.ArtifactService;
import com.game.module.attach.training.trainingLogic;
import com.game.module.copy.CopyService;
import com.game.module.fame.FameService;
import com.game.module.fashion.FashionService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.pet.PetService;
import com.game.module.player.*;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.traversing.TraversingService;
import com.game.params.Int2Param;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.goods.AttrItem;
import com.game.params.goods.SGoodsVo;
import com.game.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailService mailService;
    @Autowired
    private FameService fameService;
    @Autowired
    private ArtifactService artifactService;
    @Autowired
    private trainingLogic trainingLogic;
    @Autowired
    private TraversingService traversingService;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PetService petService;
    @Autowired
    private RandomRewardService randomRewardService;

    private Map<Integer, PlayerBag> playerGoods = new ConcurrentHashMap<Integer, PlayerBag>();

    // 获取玩家的所有物品集合
    public PlayerBag getPlayerBag(int playerId) {
        PlayerBag bag = playerGoods.get(playerId);
        if (bag != null) {
            return bag;
        }
        byte[] dbData = goodsDao.select(playerId);
        if (dbData != null) {
            dbData = CompressUtil.decompressBytes(dbData);
            bag = JsonUtils.string2Object(new String(dbData, Charset.forName("utf-8")), PlayerBag.class);
            if (bag == null) {
                ServerLogger.warn("Err Player Goods:", playerId, dbData.length);
                bag = new PlayerBag();
            }
        } else {
            bag = new PlayerBag();
        }
        playerGoods.put(playerId, bag);
        resetBag(playerId);
        return bag;
    }

    //初始化背包
    public void initBag(int playerId) {
        PlayerBag bag = new PlayerBag();
        playerGoods.putIfAbsent(playerId, bag);
        Context.getThreadService().execute(new Runnable() {
            @Override
            public void run() {
                goodsDao.insert(playerId);
            }
        });
    }

    //更新数据库
    public void updateBag(int playerId) {
        PlayerBag data = playerGoods.get(playerId);
        if (data == null) {
            return;
        }
        String str = JsonUtils.object2String(data);
        byte[] dbData = str.getBytes(Charset.forName("utf-8"));
        goodsDao.update(playerId, CompressUtil.compressBytes(dbData));
    }

    //获得物品
    public Goods getGoods(int playerId, long id) {
        return getPlayerBag(playerId).getAllGoods().get(id);
    }

    //增加新物品
    public void addGoods(int playerId, Goods g) {
        getPlayerBag(playerId).getAllGoods().put(g.getId(), g);
    }

    //移除物品
    public void removeGoods(int playerId, Goods g) {
        getPlayerBag(playerId).getAllGoods().remove(g.getId());
    }

    //获得同类的物品
    public List<Goods> getExistBagGoods(int playerId, int goodsId) {
        List<Goods> exists = new ArrayList<Goods>();
        for (Goods g : getPlayerBag(playerId).getAllGoods().values()) {
            if (g.isInBag() && g.getGoodsId() == goodsId) {
                exists.add(g);
            }
        }
        return exists;
    }


    // 获取物品配置
    public GoodsConfig getGoodsConfig(int goodsId) {
        return GameData.getConfig(GoodsConfig.class, goodsId);
    }

    // 检查物品是否足够扣除
    public int checkHasEnough(int playerId, List<GoodsEntry> goodsList) {
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);
        for (GoodsEntry item : goodsList) {
            int goodsId = item.id;
            int count = item.count;
            GoodsConfig cfg = GameData.getConfig(GoodsConfig.class, goodsId);
            if (goodsId == Goods.COIN) {
                if (player.getCoin() < count) {
                    return Response.NO_COIN;
                }
            } else if (goodsId == Goods.DIAMOND) {
                if (player.getDiamond() < count) {
                    return Response.NO_DIAMOND;
                }
            } else if (goodsId == Goods.ENERGY) {
                if (player.getEnergy() < count) {
                    return Response.NO_ENERGY;
                }
            } else if (goodsId == Goods.ACHIEVEMENT) {
                if (player.getAchievement() < count) {
                    return Response.ERR_PARAM;
                }
            } else if (cfg.type == Goods.CURRENCY) {
                if (!playerService.verifyCurrency(playerId, goodsId, count)) {
                    return ConfigData.globalParam().noCurrencyTips.get(cfg.id);
                }
            } else if (cfg.type == Goods.FAME) {
                Upgrade upgrade = data.getFames().get(cfg.param1[0]);
                if (upgrade == null || upgrade.getCurExp() < count) {
                    return Response.ERR_PARAM;
                }
            } else if (cfg.type == Goods.PET_MATERIAL) {
                if (!petService.checkEnough(playerId, goodsId, count)) {
                    return Response.ERR_GOODS_COUNT;
                }
            } else if (goodsId > 10000) {
                List<Goods> exists = getExistBagGoods(playerId, goodsId);
                int total = 0;
                for (Goods g : exists) {
                    total += g.getStackNum();
                }
                if (total < count) {
                    return Response.ERR_GOODS_COUNT;
                }
            } else {
                return Response.ERR_PARAM;
            }

        }
        return Response.SUCCESS;
    }

    public Map<Integer, Integer> getGoods(int playerId, List<Integer> goodsList) {
        Map<Integer, Integer> map = Maps.newHashMap();
        PlayerData data = playerService.getPlayerData(playerId);
        PlayerCurrency currency = data.getCurrency();
        for (int id : goodsList) {
            int total = 0;
            if (id == 109) {
                total = (int) currency.get(id);
            } else {
                List<Goods> exists = getExistBagGoods(playerId, id);
                for (Goods g : exists) {
                    total += g.getStackNum();
                }
            }

            map.put(id, total);
        }
        return map;
    }


    /**
     * 扣除消耗
     *
     * @return 是否成功
     */
    public int decConsume(int playerId, Map<Integer, Integer> goods, LogConsume log, Object... params) {
        List<GoodsEntry> goodsList = new ArrayList<GoodsEntry>();
        for (Map.Entry<Integer, Integer> entry : goods.entrySet()) {
            goodsList.add(new GoodsEntry(entry.getKey(), entry.getValue()));
        }
        return decConsume(playerId, goodsList, log, params);
    }

    /**
     * 扣除消耗
     *
     * @return 是否成功
     */
    public int decConsume(int playerId, List<GoodsEntry> goodsList, LogConsume log, Object... params) {
        int check = checkHasEnough(playerId, goodsList);
        if (check != Response.SUCCESS) {
            return check;
        }
        List<Int2Param> petConsume = Lists.newArrayList();
        for (GoodsEntry item : goodsList) {
            int goodsId = item.id;
            int count = item.count;
            GoodsConfig config = GameData.getConfig(GoodsConfig.class, goodsId);
            if (goodsId == Goods.COIN) {
                playerService.decCoin(playerId, count, log, params);
            } else if (goodsId == Goods.DIAMOND) {
                playerService.decDiamond(playerId, count, log, params);
            } else if (goodsId == Goods.ENERGY) {
                playerService.decEnergy(playerId, count, log, params);
            } else if (goodsId == Goods.ACHIEVEMENT) {
                playerService.decAchievement(playerId, count, log, params);
            } else if (config.type == Goods.CURRENCY) {
                playerService.decCurrency(playerId, goodsId, count, log, params);
            } else if (config.type == Goods.FAME) {
                decFame(playerId, config.param1[0], count);
            } else if (config.type == Goods.PET_MATERIAL) {
                Int2Param param = new Int2Param();
                param.param1 = goodsId;
                param.param2 = count;
                petConsume.add(param);
            } else if (goodsId > 10000) {
                decGoodsFromBag(playerId, goodsId, count, log, params);
            } else {
                throw new RuntimeException("ErrGoodsId:" + goodsId);
            }
        }
        petService.consume(playerId, petConsume);

        return Response.SUCCESS;
    }

    /**
     * 扣除声望
     *
     * @param playerId
     * @param camp
     * @param count
     */
    private void decFame(int playerId, int camp, int count) {
        PlayerData data = playerService.getPlayerData(playerId);
        Upgrade upgrade = data.getFames().get(camp);
        upgrade.setCurExp(upgrade.getCurExp() - count);
        fameService.refresh(playerId);
    }

    public int decConsume(int playerId, int[][] goodsList, LogConsume log, Object... params) {
        List<GoodsEntry> decs = new ArrayList<GoodsEntry>(goodsList.length);
        for (int i = 0; i < goodsList.length; i++) {
            decs.add(new GoodsEntry(goodsList[i][0], goodsList[i][1]));
        }
        return decConsume(playerId, decs, log, params);
    }

    public boolean addGoodsToBag(int playerId, int goodsId, int count, LogConsume log, Object... params) {
        return addGoodsToBag(playerId, new int[]{goodsId}, new int[]{count}, log, params);
    }

    /**
     * 增加物品
     *
     * @param playerId
     * @param goodsIds
     * @param counts
     * @param log      日志类型
     * @param params   日志参数（可选）
     * @return 加入是否成功（背包是否已满)
     */
    public boolean addGoodsToBag(int playerId, int[] goodsIds, int[] counts, LogConsume log, Object... params) {

        if (goodsIds == null || goodsIds.length == 0 || counts == null || counts.length < goodsIds.length)
            return false;

        PlayerBag bag = getPlayerBag(playerId);
        Player player = playerService.getPlayer(playerId);

        List<SGoodsVo> goodsUpdates = new ArrayList<SGoodsVo>();

        for (int i = 0; i < goodsIds.length; ++i) {
            int goodsId = goodsIds[i];
            int count = counts[i];

            GoodsConfig config = getGoodsConfig(goodsId);
            synchronized (bag) {

                if (count <= 0 || config == null) {
                    return false;
                }

                // 活动奖励验证职业
                if (log == LogConsume.ACTIVITY_REWARD || log == LogConsume.ACTIVITY_RE_REWARD) {
                    if (config.vocation > 0 && player.getVocation() != config.vocation) {
                        continue;
                    }
                }

                if (!checkCanAdd(playerId, goodsId, count)) {
                    return false;
                }
                // 特殊物品不要调用
                if (goodsId < 10000) {
                    throw new RuntimeException("Invalid GoodsId:" + goodsId);
                }

                List<Goods> exists = getExistBagGoods(playerId, goodsId);
                for (Goods g : exists) {
                    int left = config.maxStack - g.getStackNum();
                    if (left == 0) {
                        continue;
                    }
                    int addCount = Math.min(count, left);
                    g.setStackNum(g.getStackNum() + addCount);

                    count -= addCount;
                    goodsUpdates.add(toVO(g));
                    if (count <= 0) {
                        break;
                    }
                }
                while (count > 0) {
                    int addCount = Math.min(count, config.maxStack);
                    Goods newGoods = addNewGoods(playerId, goodsId, addCount, Goods.BAG);
                    addGoods(playerId, newGoods);
                    goodsUpdates.add(toVO(newGoods));
                    count -= addCount;
                }
                playerGoods.put(playerId, bag);

            }
        }
        // 更新数据到前端
        refreshGoodsToClient(playerId, goodsUpdates);

        Map<Integer, int[]> condParams = Maps.newHashMapWithExpectedSize(1);
        // 记录日志
        for (int i = 0; i < goodsIds.length; ++i) {
            int goodsId = goodsIds[i];
            int count = counts[i];

            GoodsConfig config = getGoodsConfig(goodsId);

            Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true,
                    count, log, goodsId, Goods.GOOODS, params);
            //特殊的物品
            if (config.type == Goods.ARTIFACT_COMPONENT) {
                artifactService.checkActive(playerId);
            }
            if (CommonUtil.contain(
                    ConfigData.globalParam().equipTypes,
                    config.type)) {
                condParams.put(Task.FINISH_WEAR, new int[]{config.color, 1});
//                taskService.doTask(playerId, Task.FINISH_WEAR, config.color, 1);
            }
        }
        if (condParams.size() > 0)
            taskService.doTask(playerId, condParams);

        return true;
    }

    public Goods addNewGoods(int playerId, int goodsId, int count, int storeType) {
        Goods newGoods = new Goods(playerId, goodsId, count, storeType);
        // 处理附加属性
        addAditiveAttrs(newGoods);
        long id = getPlayerBag(playerId).nextId();
        newGoods.setId(id);

        return newGoods;
    }

    //添加附加属性
    private void addAditiveAttrs(Goods g) {
        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, g.getGoodsId());
        if (!CommonUtil.contain(ConfigData.globalParam().equipTypes, cfg.type)) {
            return;
        }
        int id = cfg.level * 1000 + cfg.color;
        EquipAddAttrCfg addCfg = ConfigData.getConfig(EquipAddAttrCfg.class, id);
        if (addCfg == null) {
            return;
        }
        if (addCfg.typeRates != null) {
            for (int i = 1; i <= 2; i++) {
                int typeIndex = RandomUtil.getRandomIndex(addCfg.typeRates);
                int type = addCfg.types[typeIndex];
                int[] range = addCfg.parameter.get(type);
                int value = RandomUtil.randInt(range[0], range[1]);
                AttrItem attr = new AttrItem();
                attr.type = type;
                attr.value = value;
                g.getAddAttrList().add(attr);
            }
        }
    }

    /**
     * 增加一堆物品到背包，
     *
     * @param playerId
     * @param addedGoods
     * @param log        日志类型
     * @param params     日志参数（可选）
     * @return true成功 false 背包已满
     */
    public boolean addGoodsToBag(int playerId, List<GoodsEntry> addedGoods, LogConsume log, Object... params) {
        PlayerBag bag = getPlayerBag(playerId);
        synchronized (bag) {
            if (!checkCanAddToBag(playerId, addedGoods)) {
                return false;
            }
            for (GoodsEntry g : addedGoods) {
                addGoodsToBag(playerId, g.id, g.count, log, params);
            }
        }
        return true;
    }

    // 检查能否放入一个物品
    public boolean checkCanAdd(int playerId, int goodsId, int count) {
        return checkCanAddToBag(playerId, Arrays.asList(new GoodsEntry(goodsId, count)));
    }

    /**
     * 验证一堆物品能否加到背包
     */
    public boolean checkCanAddToBag(int playerId, int[][] addedGoods) {
        Map<Integer, Integer> rewards = new HashMap<Integer, Integer>();
        for (int i = 0; i < addedGoods.length; i++) {
            int[] item = addedGoods[i];
            Integer count = rewards.get(item[0]);
            if (count == null) {
                count = 0;
            }
            count += item[1];
            rewards.put(item[0], count);
        }
        List<GoodsEntry> totals = new ArrayList<GoodsEntry>(rewards.size());
        for (Entry<Integer, Integer> reward : rewards.entrySet()) {
            totals.add(new GoodsEntry(reward.getKey(), reward.getValue()));
        }
        return checkCanAddToBag(playerId, totals);
    }

    /**
     * 验证一堆物品能否加到背包
     */

    public boolean checkCanAddToBag(int playerId, List<GoodsEntry> addedGoods) {
        if (addedGoods == null) {
            return false;
        }
        List<List<GoodsEntry>> addedGoodsList = Lists.newArrayList();
        addedGoodsList.add(addedGoods);
        return checkCanAddListToBag(playerId, addedGoodsList) == 1;
    }

    public int checkCanAddListToBag(int playerId, List<List<GoodsEntry>> addedGoodsList) {
        int countValue = 0;
        int leftCounts[] = getBlankGridCounts(playerId);
        for (int i = 0; i < addedGoodsList.size(); ++i) {
            List<GoodsEntry> addedGoods = addedGoodsList.get(i);
            addedGoods = filter(addedGoods);
            for (GoodsEntry entry : addedGoods) {
                int goodsId = entry.id;
                int count = entry.count;
                if (goodsId < 10000) {// 特殊物品，直接加在身上
                    continue;
                }
                GoodsConfig config = getGoodsConfig(goodsId);
                if (config == null) {
                    ServerLogger.warn("check add goods 物品: " + goodsId + "不存在");
                    return countValue;
                }

                if (config.type == Goods.PET || config.type == Goods.PET_MATERIAL
                        || config.type == Goods.FASHION
                        || config.type == Goods.SKILL_CARD) {
                    continue;
                }

                List<Goods> existed = getExistBagGoods(playerId, goodsId);
                for (Goods g : existed) {// 已经存在的，看能否堆叠
                    int left = config.maxStack - g.getStackNum();
                    count -= left;
                }
                if (count <= 0) {
                    continue;
                }

                int bagType = getBagType(config.type);
                int getBlankGrid = leftCounts[bagType];

                if (config.maxStack == 0) {
                    config.maxStack = 99999;
                }
                int needBlank = count / config.maxStack;// 插入空格
                if (count % config.maxStack > 0) {
                    needBlank++;
                }
                if (needBlank > getBlankGrid) {
                    ServerLogger.info("背包剩下的格子放不下物品：" + goodsId);
                    return countValue;
                }
                leftCounts[bagType] -= needBlank;
            }
            countValue = (i + 1);
        }
        return countValue;
    }

    // 过滤一下相同的
    private List<GoodsEntry> filter(List<GoodsEntry> items) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (GoodsEntry item : items) {
            Integer count = result.get(item.id);
            if (count == null) {
                count = 0;
            }
            count += item.count;
            result.put(item.id, count);
        }

        List<GoodsEntry> newItems = new ArrayList<GoodsEntry>(result.size());
        for (Entry<Integer, Integer> e : result.entrySet()) {
            newItems.add(new GoodsEntry(e.getKey(), e.getValue()));
        }
        return newItems;
    }

    /**
     * 删除物品
     *
     * @param playerId
     * @param goodsId
     * @param count
     * @param log      日志类型
     * @param params   日志参数（可选）
     * @return 是否成功
     */
    public boolean decGoodsFromBag(int playerId, int goodsId, int count, LogConsume log, Object... params) {
        GoodsConfig config = getGoodsConfig(goodsId);
        if (count <= 0 || config == null) {
            return false;
        }
        PlayerBag bag = getPlayerBag(playerId);
        List<SGoodsVo> goodsUpdate = new ArrayList<SGoodsVo>();
        synchronized (bag) {
            List<Goods> exists = getExistBagGoods(playerId, goodsId);
            int curCount = 0;
            for (Goods g : exists) {
                curCount += g.getStackNum();
            }
            if (curCount < count) {
                return false;
            }

            for (Goods owned : exists) {
                int decCount = Math.min(owned.getStackNum(), count);
                owned.setStackNum(owned.getStackNum() - decCount);
                goodsUpdate.add(toVO(owned));
                if (owned.getStackNum() == 0) {// 删除
                    removeGoods(playerId, owned);
                }
                count -= decCount;
                if (count == 0) {
                    break;
                }
            }
            playerGoods.put(playerId, bag);
            // 更新到前端
            refreshGoodsToClient(playerId, goodsUpdate);
        }

        // 记录日志
        Player player = playerService.getPlayer(playerId);

        Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), false,
                count, log, goodsId, Goods.GOOODS, params);

        return true;
    }

    // 获得某类物品多少个
    public int getGoodsCount(int playerId, int goodsId) {
        List<Goods> exists = getExistBagGoods(playerId, goodsId);
        int curCount = 0;
        for (Goods g : exists) {
            curCount += g.getStackNum();
        }
        return curCount;
    }


    public ListParam<SGoodsVo> getAllGoods(int playerId) {
        ListParam<SGoodsVo> result = new ListParam<SGoodsVo>();
        Collection<Goods> all = getPlayerBag(playerId).getAllGoods().values();
        int index = 0;
        result.params = new ArrayList<SGoodsVo>(all.size());
        for (Goods g : all) {
            result.params.add(toVO(g));
            if (index++ >= 500) {
                ServerLogger.warn("Err Bag Size：", playerId, all.size());
                break;
            }
        }
        return result;
    }

    public SGoodsVo toVO(Goods g) {
        SGoodsVo vo = new SGoodsVo();
        vo.id = g.getId();
        vo.goodsId = g.getGoodsId();
        vo.stackNum = g.getStackNum();
        vo.storeType = (byte) g.getStoreType();
        vo.star = (byte) g.getStar();
        vo.bLock = g.isLock();
        vo.addAttrs = new ArrayList<>(2);
        if (!g.getAddAttrList().isEmpty()) {
            vo.addAttrs.addAll(g.getAddAttrList());
        }

        vo.lastAttrs = new ArrayList<>(2);
        if (!g.getLastAttrs().isEmpty()) {
            vo.lastAttrs.addAll(g.getLastAttrs());
        }

        return vo;
    }

    // 发送奖励api，背包已满自动发送邮件
    public void addRewards(int playerId, Map<Integer, Integer> rewards, LogConsume type, Object... params) {
        List<GoodsEntry> _rewards = new ArrayList<GoodsEntry>();
        for (Map.Entry<Integer, Integer> entry : rewards.entrySet()) {
            _rewards.add(new GoodsEntry(entry.getKey(), entry.getValue()));
        }
        addRewards(playerId, _rewards, type, params);
    }

    public void addRewards(int playerId, List<GoodsEntry> rewards, LogConsume type, Object... params) {
        if (rewards == null || rewards.size() == 0) {
            return;
        }
        if (!checkCanAddToBag(playerId, rewards)) {
            // 发送邮件
            String title = ConfigData.getConfig(ErrCode.class, Response.FULL_BAG_TITLE).tips;
            String content = ConfigData.getConfig(ErrCode.class, Response.FULL_BAG_CONTENT).tips;

            List<GoodsEntry> mailRewards = new ArrayList<GoodsEntry>();
            mailRewards.addAll(rewards);
            mailService.sendSysMail(title, content, mailRewards, playerId, type);

        } else {
            // 汇总到一个map
            HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (GoodsEntry reward : rewards) {
                Integer count = map.get(reward.id);
                if (count == null) {
                    count = 0;
                }
                count += reward.count;
                map.put(reward.id, count);
            }

            if (map.size() > 0) {
                int[] ids = new int[map.size()];
                int[] counts = new int[map.size()];
                int idx = 0;
                for (Entry<Integer, Integer> item : map.entrySet()) {
//                    addRewrad(playerId, item.getKey(), item.getValue(), type, params);
                    ids[idx] = item.getKey();
                    counts[idx] = item.getValue();
                    ++idx;
                }
                addRewrads(playerId, ids, counts, type, params);
            }
        }
    }

    // 增加单个奖励，内部使用
    public void addRewrad(int playerId, int id, int count, LogConsume type, Object... params) {
        addRewrads(playerId, new int[]{id}, new int[]{count}, type, params);
    }

    public void addRewrads(int playerId, int[] ids, int[] counts, LogConsume type, Object... params) {

        if (ids == null || ids.length == 0 || counts == null || counts.length < ids.length) {
            return;
        }

        List<Integer> normalGoods = new ArrayList<>();
        for (int i = 0; i < ids.length; ++i) {
            int id = ids[i];
            int count = counts[i];
//        Player player = playerService.getPlayer(playerId);
            // 普通物品
            GoodsConfig goods = getGoodsConfig(id);
            if (goods == null) {
                continue;
            }
            if (id > 10000) {
                if (goods.type == Goods.BOTTLE) {
                    continue;
                }
            /*if (goods.vocation != 0 && goods.vocation != player.getVocation()) {
				return;
			}*/
                //技能卡
                if (goods.type == Goods.SKILL_CARD) {
                    playerService.addSkillCard(playerId, goods.param1[0], count);
                    taskService.doTask(playerId, Task.FINISH_CARD_COMPOSE, goods.color, 0);
                } else if (goods.type == Goods.SPECIAL_MAP) {
                    traversingService.addMap(playerId, goods, count);
                } else if (goods.type == Goods.FASHION) {
                    fashionService.addFashion(playerId, goods.param1[0], goods.param1[1]);
                } else if (goods.type == Goods.PET) {
                    petService.addPet(playerId, id);
                } else if (goods.type == Goods.PET_MATERIAL) {
                    petService.addPetMaterial(playerId, id, count, true);
                } else {
                    normalGoods.add(i);
                }
            } else {
                if (id == Goods.COIN) {// 金币
                    playerService.addCoin(playerId, count, type, params);
                } else if (id == Goods.DIAMOND) {// 钻石
                    playerService.addDiamond(playerId, count, type, params);
                } else if (id == Goods.EXP) {// 经验
                    playerService.addExp(playerId, count, type, params);
                } else if (id == Goods.ACHIEVEMENT) {
                    playerService.addAchievement(playerId, count, type, params);
                } else if (id == Goods.ENERGY) {// 体力
                    playerService.addEnergy(playerId, count, type, params);
                } else if (id == Goods.VIP_EXP) {// vip经验值
                    playerService.addVipExp(playerId, count);
                } else if (id == Goods.EXPERIENCE_HP) {// 英雄试练HP
                    trainingLogic.addHP(playerId, count);
                } else if (goods.type == Goods.FAME) {//声望卡
                    fameService.addFame(playerId, goods.param1[0], count);
                } else if (goods.type == Goods.CURRENCY) {
                    playerService.addCurrency(playerId, id, count, type, params);
                }
            }
        }

        if (normalGoods.size() > 0) {
            int[] goodsIds = new int[normalGoods.size()];
            int[] goodsCount = new int[normalGoods.size()];
            for (int i = 0; i < normalGoods.size(); ++i) {
                int idx = normalGoods.get(i);
                goodsIds[i] = ids[idx];
                goodsCount[i] = counts[idx];
            }
            addGoodsToBag(playerId, goodsIds, goodsCount, type, params);
        }
    }

    /**
     * 整理背包:相同的叠加，更新数据库
     */
    public void resetBag(int playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            ServerLogger.warn("玩家不存在，玩家ID=" + playerId);
            return;
        }
        PlayerBag bag = null;
        synchronized (player) {
            bag = getPlayerBag(playerId);

            Collection<Goods> tempGoods = bag.getAllGoods().values();
            Map<Integer, List<Goods>> sameGoods = new ConcurrentHashMap<Integer, List<Goods>>();
            for (Goods g : tempGoods) {
                if (!g.isInBag()) {
                    continue;
                }
                List<Goods> list = sameGoods.get(g.getGoodsId());
                if (list == null) {
                    list = new ArrayList<Goods>();
                    sameGoods.put(g.getGoodsId(), list);
                }
                list.add(g);
            }
            for (List<Goods> list : sameGoods.values()) {
                GoodsConfig config = getGoodsConfig(list.get(0).getGoodsId());
                Collections.sort(list, LeftCountSortor.getInstance());
                for (int i = 0; i < list.size() - 1; i++) {
                    Goods from = list.get(i);

                    for (int j = i + 1; j < list.size(); j++) {
                        Goods target = list.get(j);
                        int alter = Math.min(from.getStackNum(), config.maxStack - target.getStackNum());
                        from.setStackNum(from.getStackNum() - alter);
                        target.setStackNum(target.getStackNum() + alter);
                    }
                }
                for (Goods g : list) {// 删除叠加后空的
                    if (g.getStackNum() == 0) {
                        removeGoods(playerId, g);
                    }
                }
            }
        }
    }

    // 使用道具
    public ListParam<Reward> useTool(int playerId, long id, int count) {
        Player player = playerService.getPlayer(playerId);
        Goods goods = getGoods(playerId, id);
        ListParam<Reward> result = new ListParam<>();
        result.params = Lists.newArrayList();
        if (goods == null || count <= 0) {
            return result;
        }
        if (goods.getStackNum() < count) {
            return result;
        }
        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());

        if (cfg == null) {
            ServerLogger.warn("物品不存在，物品ID=" + goods.getGoodsId());
            return result;
        }

        if (player.getLev() < cfg.level) {
            return result;
        }
        if (cfg.vocation > 0 && player.getVocation() != cfg.vocation) {
            return result;
        }
        decSpecGoods(goods, count, LogConsume.USE_TOOL);
        int groupId = cfg.contents[0][0];
        Map<Integer, Reward> map = Maps.newHashMap();
        List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
        for (int i = 0; i < count; i++) {
//            List<Reward> list = randomRewardService.getRewards(playerId, groupId, LogConsume.OPEN_BOX);
            List<Reward> list = randomRewardService.getRewards(playerId, groupId, LogConsume.OPEN_BOX, false);
            for (Reward reward : list) {
                Reward rw = map.get(reward.id);
                if (rw != null) {
                    rw.count += reward.count;
                } else {
                    map.put(reward.id, reward);
                }
            }
        }

        if (map.size() > 0) {
            for (Reward reward : map.values()) {
                rewards.add(new GoodsEntry(reward.id, reward.count));
            }
            addRewards(playerId, rewards, LogConsume.OPEN_BOX, groupId, count);
        }

        result.params.addAll(map.values());
        return result;
    }

    // 加礼包奖励
    public int addGiftBagReward(int playerId, GoodsConfig cfg, int count) {
        int[][] rewards = cfg.contents;
        List<GoodsEntry> items = new ArrayList<GoodsEntry>();

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < rewards.length; j++) {
                int[] item = rewards[j];
                items.add(new GoodsEntry(item[0], item[1]));
            }
        }

        if (!checkCanAddToBag(playerId, items)) {
            return Response.BAG_FULL;
        }
        addRewards(playerId, items, LogConsume.USE_TOOL, cfg.id);
        return Response.SUCCESS;
    }

    // 加宝石袋
    public int addJewelBag(int playerId, GoodsConfig cfg, int count) {
        Player player = playerService.getPlayer(playerId);
        int[][] rewards = cfg.contents;

        Map<Integer, Integer> jewels = new HashMap<>();

        for (int[] arr : rewards) {
            int id = arr[0];
            int addCount = arr[1];
            int vocation = ConfigData.getConfig(GoodsConfig.class, id).vocation;
            if (vocation != 0 && vocation != player.getVocation()) {
                continue;
            }
            Integer cur = jewels.get(id);
            if (cur == null) {
                cur = 0;
            }
            cur += addCount;
            jewels.put(id, cur);
        }
		/*for (int i = 0; i < count; i++) {
			int index = RandomUtil.getRandomIndex(cfg.contentsRates);
			int[] items = rewards[index];

			for(int j=0;j<items.length;j=j+2){
				int id = items[j];
				int addCount = items[j+1];
				int vocation = ConfigData.getConfig(GoodsConfig.class, id).vocation;
				if(vocation!=0&&vocation!=player.getVocation()){
					continue;
				}
				Integer cur = jewels.get(id);
				if (cur == null) {
					cur = 0;
				}
				cur += addCount;
				jewels.put(id, cur);
			}
		}*/

        List<GoodsEntry> items = new ArrayList<GoodsEntry>();
        for (Entry<Integer, Integer> jewel : jewels.entrySet()) {
            items.add(new GoodsEntry(jewel.getKey(), jewel.getValue()));
        }

        if (!checkCanAddToBag(playerId, items)) {
            return Response.BAG_FULL;
        }
        if (!items.isEmpty()) {
            addRewards(playerId, items, LogConsume.USE_TOOL, cfg.id);
        }
        return Response.SUCCESS;
    }

    // 批量扣除物品
    public void removeBatchGoods(int playerId, List<Goods> gs, LogConsume log, Object... params) {
        Player player = playerService.getPlayer(playerId);

        List<SGoodsVo> goodsUpdate = new ArrayList<SGoodsVo>();
        //final List<Long> ids = new ArrayList<Long>();
        for (Goods g : gs) {
            removeGoods(playerId, g);
            //ids.add(g.getId());
            g.setStackNum(0);
            goodsUpdate.add(toVO(g));
            Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(),
                    false, 1, log, g.getGoodsId(), Goods.GOOODS, params);
        }
        refreshGoodsToClient(playerId, goodsUpdate);
    }

    // 扣除特定物品的数量
    public void decSpecGoods(Goods owned, int decCount, LogConsume log, Object... params) {
        int playerId = owned.getPlayerId();
        List<SGoodsVo> goodsUpdate = new ArrayList<SGoodsVo>();
        owned.setStackNum(owned.getStackNum() - decCount);
        goodsUpdate.add(toVO(owned));
        if (owned.getStackNum() == 0) {// 删除
            removeGoods(playerId, owned);
        }
        refreshGoodsToClient(playerId, goodsUpdate);

        // 记录日志
        Player player = playerService.getPlayer(playerId);
        Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), false,
                1, log, owned.getGoodsId(), Goods.GOOODS, params);
    }

    // 获得物品所在的背包栏
    private static int getBagType(int goodsType) {
        GlobalConfig global = ConfigData.globalParam();
        if (CommonUtil.contain(global.specTypes, goodsType)) {//特殊
            return 1;
        } else {// 道具，装备
            return 0;
        }
    }

    // 获得背包空格子数
    public int getBlankGridCount(int playerId, int goodsType) {
        int bagType = getBagType(goodsType);
        PlayerBag bag = getPlayerBag(playerId);
        int count = 0;
        for (Goods g : bag.getAllGoods().values()) {
            if (g.getStoreType() != Goods.BAG) {
                continue;
            }
            int type = ConfigData.getConfig(GoodsConfig.class, g.getGoodsId()).type;
            if (getBagType(type) == bagType) {
                count++;
            }
        }
        PlayerData data = playerService.getPlayerData(playerId);
        int[] blankGridCounts = data.getBlankGrids();
        int[] cfgSizes = ConfigData.globalParam().bagSize;
        if (blankGridCounts[bagType] < cfgSizes[bagType]) {
            blankGridCounts[bagType] = cfgSizes[bagType];
        }
        return blankGridCounts[bagType] - count;
    }

    // 获得背包所有栏目空格子数
    private int[] getBlankGridCounts(int playerId) {
        int existCounts[] = new int[6];
        PlayerBag bag = getPlayerBag(playerId);
        for (Goods g : bag.getAllGoods().values()) {
            if (g.getStoreType() != Goods.BAG) {
                continue;
            }
            if (ConfigData.getConfig(GoodsConfig.class, g.getGoodsId()) == null) {
                ServerLogger.info("goods id don't exist id = " + g.getGoodsId());
            }
            int type = ConfigData.getConfig(GoodsConfig.class, g.getGoodsId()).type;
            int bagType = getBagType(type);
            existCounts[bagType]++;
        }

        int[] totalCounts = playerService.getPlayerData(playerId).getBlankGrids();
        if (totalCounts == null) {
            ServerLogger.info("totalCounts is null");
        }
        int[] leftCounts = new int[6];
        for (int i = 0; i < 3; i++) {
            leftCounts[i] = totalCounts[i] - existCounts[i];
        }
        return leftCounts;
    }

    // 更新物品信息到前端
    public void refreshGoodsToClient(int playerId, List<SGoodsVo> goodsUpdate) {
        ListParam<SGoodsVo> result = new ListParam<SGoodsVo>();
        result.params = goodsUpdate;
        SessionManager.getInstance().sendMsg(BagExtension.GOODS_UPDATE, result, playerId);
    }

    // 更新物品信息到前端
    public void refreshGoodsToClient(int playerId, SGoodsVo goodsUpdate) {
        ListParam<SGoodsVo> result = new ListParam<SGoodsVo>();
        result.params = new ArrayList<SGoodsVo>();
        result.params.add(goodsUpdate);
        SessionManager.getInstance().sendMsg(BagExtension.GOODS_UPDATE, result, playerId);
    }

    // 加奖励
    public void addRewards(int playerId, int[][] rewards, LogConsume type, Object... params) {
        List<GoodsEntry> items = new ArrayList<GoodsEntry>(rewards.length);

        for (int i = 0; i < rewards.length; i++) {
            int[] item = rewards[i];
            items.add(new GoodsEntry(item[0], item[1]));
        }
        addRewards(playerId, items, type, params);
    }

    /**
     * 道具出售
     *
     * @param playerId
     * @param id
     * @param count
     * @return
     */
    public int sell(int playerId, int id, int count) {
        Goods goods = getGoods(playerId, id);
        if (goods == null || count <= 0) {
            return Response.ERR_PARAM;
        }
        if (goods.getStackNum() < count) {
            return Response.ERR_GOODS_COUNT;
        }
        GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
        int sellMoney = cfg.sellPrice * count;

        decGoodsFromBagById(playerId, id, count, LogConsume.SHOP_BUY_ADD);
        addRewrad(playerId, Goods.COIN, sellMoney, LogConsume.SHOP_BUY_ADD);
        return Response.SUCCESS;
    }

    public boolean decGoodsFromBagById(int playerId, int id, int count, LogConsume log, Object... params) {
        Goods goods = getGoods(playerId, id);
        if (count <= 0 || goods == null || goods.getStackNum() < count) {
            return false;
        }
        PlayerBag bag = getPlayerBag(playerId);
        List<SGoodsVo> goodsUpdate = new ArrayList<SGoodsVo>();
        synchronized (bag) {
            int decCount = Math.min(goods.getStackNum(), count);
            goods.setStackNum(goods.getStackNum() - decCount);
            goodsUpdate.add(toVO(goods));
            if (goods.getStackNum() == 0) {// 删除
                removeGoods(playerId, goods);
            }
            playerGoods.put(playerId, bag);
            // 更新到前端
            refreshGoodsToClient(playerId, goodsUpdate);
        }
        // 记录日志
        Player player = playerService.getPlayer(playerId);
        Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), false,
                count, log, goods.getGoodsId(), Goods.GOOODS, params);
        return true;
    }

    /**
     * 物品合成
     *
     * @param playerId
     * @param bagId
     */
    public Int2Param compound(int playerId, int bagId) {
        Int2Param ret = new Int2Param();
        Goods goods = getGoods(playerId, bagId);
        if (goods == null) {
            ServerLogger.warn("物品不存在,物品id=" + bagId);
            ret.param1 = Response.ERR_PARAM;
            return ret;
        }
        GoodsConfig config = ConfigData.getConfig(GoodsConfig.class, goods.getGoodsId());
        if (!checkCanAdd(playerId, config.param2[0][0], 1)) {
            ret.param1 = Response.BAG_FULL;
            return ret;
        }

        if (!decGoodsFromBagById(playerId, bagId, config.param1[0], LogConsume.ITEM_COMPOUND)) {
            ret.param1 = Response.NO_MATERIAL;
            return ret;
        }
        addGoodsToBag(playerId, config.param2[0][0], 1, LogConsume.ITEM_COMPOUND);
        ret.param1 = Response.SUCCESS;
        ret.param2 = config.id;
        return ret;
    }

    public Int2Param lockItem(int playerId, int bagId, int lockState) {
        Int2Param ret = new Int2Param();
        Goods goods = getGoods(playerId, bagId);
        if (goods == null) {
            ret.param1 = Response.ERR_PARAM;
            return ret;
        }

        goods.setLock(lockState == 1);
        ret.param1 = Response.SUCCESS;
        ret.param2 = bagId;

        //goodsService.refreshGoodsToClient(playerId, goodsService.toVO(goods));
        return ret;
    }

    public ListParam<SGoodsVo> getOtherEquips(int playerId, int otherId) {
        ListParam<SGoodsVo> result = new ListParam<SGoodsVo>();
        Collection<Goods> all = getPlayerBag(otherId).getAllGoods().values();
        int index = 0;
        result.params = new ArrayList<>(all.size());
        for (Goods g : all) {
            if (g.getStoreType() == Goods.EQUIP) {
                result.params.add(toVO(g));
            }
        }
        return result;
    }
}
