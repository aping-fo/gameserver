package com.game.module.copy;

import com.game.SysConfig;
import com.game.data.*;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.admin.MessageService;
import com.game.module.attach.catchgold.CatchGoldLogic;
import com.game.module.attach.endless.EndlessAttach;
import com.game.module.attach.endless.EndlessLogic;
import com.game.module.attach.experience.ExperienceAttach;
import com.game.module.attach.experience.ExperienceLogic;
import com.game.module.attach.leadaway.LeadAwayLogic;
import com.game.module.attach.treasure.TreasureAttach;
import com.game.module.attach.treasure.TreasureLogic;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.group.GroupService;
import com.game.module.log.LogConsume;
import com.game.module.player.CheatReventionService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.sct.SkillCardTrainService;
import com.game.module.sct.Train;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.shop.ShopService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.team.TeamService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.module.traversing.TraversingService;
import com.game.params.*;
import com.game.params.copy.CopyInfo;
import com.game.params.copy.CopyResult;
import com.game.params.copy.CopyVo;
import com.game.params.copy.SEnterCopy;
import com.game.params.rank.NormalCopyRankVO;
import com.game.params.scene.CMonster;
import com.game.params.scene.SMonsterVo;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.game.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CopyService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private EndlessLogic endlessLogic;
    @Autowired
    private TreasureLogic treasureLogic;
    @Autowired
    private ExperienceLogic experienceLogic;
    @Autowired
    private ShopService shopService;
    @Autowired
    private TraversingService traversingService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private LeadAwayLogic leadAwayLogic;
    @Autowired
    private CatchGoldLogic catchGoldLogic;
    @Autowired
    private GroupService groupService;
    @Autowired
    private SkillCardTrainService skillCardTrainService;
    @Autowired
    private TitleService titleService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivityService activityService;

    private AtomicInteger uniId = new AtomicInteger(100);
    private int LEARN_ID = 110001;
    private Map<Integer, CopyInstance> instances = new ConcurrentHashMap<>();

    // 获取所有副本信息
    public CopyInfo getCopys(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<CopyVo> copys = new ArrayList<>();
        for (Entry<Integer, Copy> copy : data.getCopys().entrySet()) {
            int copyId = copy.getKey();
            CopyVo vo = new CopyVo();
            vo.copyId = copyId;
            vo.state = (short) (copy.getValue().getState());

            CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
            if (cfg == null) {
                ServerLogger.warn("Err Copy Id:", copyId);
                continue;
            }
            if (cfg.count > 0) {
                Integer count = data.getCopyTimes().get(copyId);
                vo.count = (short) (count == null ? 0 : count);

                Integer mainCopyTime = data.getCopyBuyTimes().get(copyId);
                vo.buyTimes = (short) (mainCopyTime == null ? 0 : mainCopyTime);

                Integer reset = data.getResetCopy().get(copyId);
                vo.reset = (short) (reset == null ? 0 : reset);
            }

            copys.add(vo);
        }

        // 所有副本信息
        CopyInfo copyInfo = getCopyInfo(playerId);
        copyInfo.copys = copys;
        return copyInfo;
    }

    //其他副本信息
    public CopyInfo getCopyInfo(int playerId) {
        CopyInfo copyInfo = new CopyInfo();
        PlayerData data = playerService.getPlayerData(playerId);
        copyInfo.threeStars = new ArrayList<>(data.getThreeStars());
        return copyInfo;
    }

    // 进入副本
    public int enter(int playerId, int copyId) {
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        Player player = playerService.getPlayer(playerId);
        PlayerData playerData = playerService.getPlayerData(playerId);

        if (cfg == null) {
            ServerLogger.warn("ErrCopyId:", copyId);
            return Response.ERR_PARAM;
        }

        Copy myCopy = playerData.getCopys().get(copyId);
        if (myCopy == null) {
            myCopy = new Copy();
            playerData.getCopys().putIfAbsent(copyId, myCopy);
        }
        // 检查等级
        if (player.getLev() < cfg.lev) {
            return Response.NO_LEV;
        }
        // 检查前置副本
        if (cfg.preId > 0) {
            Copy copy = playerData.getCopys().get(cfg.preId);
            if (copy == null || copy.getState() == 0) {
                return Response.COPY_NO_PRE;
            }

            // 困难和噩梦副本增加上一副本通关判断
            if (cfg.difficulty != CopyInstance.EASY && cfg.chapterId > 1)
            {
                int preCopyId = copyId - 100;
                CopyConfig preCopyCfg = ConfigData.getConfig(CopyConfig.class, preCopyId);
                if (preCopyCfg != null) {
                    Copy preCopy = playerData.getCopys().get(preCopyId);
                    if (preCopy == null || preCopy.getState() == 0) {
                        return Response.COPY_NO_PRE;
                    }
                }
            }
        }

        if (cfg.type == CopyInstance.TYPE_TRAIN) {
            Train train = playerData.getTrain();
            TrialFieldCfg trainCfg = ConfigData.trainCopy.get(copyId);
            int count = train.getGroupTimes().get(trainCfg.type);
            int totalCount = ConfigData.trainCount.get(trainCfg.type);
            if (count >= totalCount) {
                return Response.NO_TODAY_TIMES;
            }
        } else {
            // 次数
            if (cfg.count > 0) {
                Integer curCount = playerService.getPlayerData(playerId).getCopyTimes().get(copyId);
                if (curCount == null) {
                    curCount = 0;
                }

                Integer buyTimes = playerData.getCopyBuyTimes().get(copyId);
                if (buyTimes == null) {
                    buyTimes = 0;
                }

                if (curCount >= cfg.count + buyTimes) {
                    return Response.NO_TODAY_TIMES;
                }
            }
        }

        //去除时空仪的体力判断
        if (cfg.type != CopyInstance.TYPE_TRAVERSING && cfg.needEnergy > 0) {
            if (player.getEnergy() < cfg.needEnergy) {
                return Response.NO_ENERGY;
            }
        }

        if (cfg.type == CopyInstance.TYPE_ENDLESS) {
            EndlessAttach attach = endlessLogic.getAttach(playerId);
            if (attach.getChallenge() == 0) {
                return Response.NO_TODAY_TIMES;
            }
            if (attach.getClearTime() > 0) {
                return Response.ERR_PARAM;
            }
            /*if((attach.getCurrLayer() % endlessLogic.getConfig().sectionLayer == 0 && cfg.difficulty != CopyInstance.HARD)
                    ||(attach.getCurrLayer() % endlessLogic.getConfig().sectionLayer != 0 && cfg.difficulty == CopyInstance.HARD)){
				return Response.ERR_PARAM;
			}*/
        } else if (cfg.type == CopyInstance.TYPE_TREASURE) {
            TreasureAttach treasureAttach = treasureLogic.getAttach(playerId);
            if (treasureAttach.getChallenge() == 0) {
                return Response.NO_TODAY_TIMES;
            }
            if (System.currentTimeMillis() - treasureAttach.getLastChallengeTime() < ConfigData.globalParam().treasureDelTime) {
                return Response.ERR_PARAM;
            }
        } else if (cfg.type == CopyInstance.TYPE_EXPERIENCE) {
            ExperienceAttach experienceAttach = experienceLogic.getAttach(playerId);
            if (experienceAttach.getChallenge() == 0) {
                return Response.NO_TODAY_TIMES;
            }
            if (System.currentTimeMillis() - experienceAttach.getLastChallengeTime() < ConfigData.globalParam().extremeEvasionDelTime) {
                return Response.ERR_PARAM;
            }
        } else if (cfg.type == CopyInstance.TYPE_LEADAWAY) {

        } else if (cfg.type == CopyInstance.TYPE_GROUP) {

        }

        if (cfg.type == CopyInstance.TYPE_LEADAWAY
                || cfg.type == CopyInstance.TYPE_GOLD
                || cfg.type == CopyInstance.TYPE_ENDLESS
                || cfg.type == CopyInstance.TYPE_TREASURE
                || cfg.type == CopyInstance.TYPE_TRAIN
                //|| cfg.type == CopyInstance.TYPE_TRAVERSING
                || cfg.type == CopyInstance.TYPE_EXPERIENCE) {
            taskService.doTask(playerId, Task.TYPE_PASS_TYPE_COPY, cfg.type, 1);
        }

        int passId = cfg.id;
        try {
            createCopyInstance(playerId, copyId, passId);
        } catch (Exception e) {
            ServerLogger.err(e, "Err enter Copy Id:" + copyId);
            return Response.ERR_PARAM;
        }
        return Response.SUCCESS;
    }

    // 获取奖励
    public CopyResult getRewards(int playerId, int copyId, CopyResult result) {
        Player player = playerService.getPlayer(playerId);
        int star = result.star;
        result.victory = true;
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        // 扣除体力
        if (cfg.needEnergy > 0) {
            if (cfg.type == CopyInstance.TYPE_TRAVERSING) {
                //移到进入副本时候扣除
                //playerService.decCurrency(playerId, Goods.TRAVERSING_ENERGY, cfg.needEnergy, LogConsume.TRAVERSING_COPY, cfg.id);
            } else {
                playerService.decEnergy(playerId, cfg.needEnergy, LogConsume.COPY_ENERGY, copyId);
            }
        }
        //在进入副本就扣除
        /**
         if (cfg.type == CopyInstance.TYPE_TRAVERSING) {
         Team team = teamService.getTeam(player.getTeamId());
         int leaderId = team.getLeader();
         if (playerId == leaderId) {
         traversingService.remvoeMap(playerId, team.getMapId());
         }
         }
         **/

        // 掉落
        CopyInstance copy = instances.get(playerService.getPlayer(playerId).getCopyId());
        if (copy == null) {
            return null;
        }
        List<GoodsEntry> items = calculateCopyReward(playerId, copyId, star);
        //List<GoodsEntry> items = calculateCopyRewardOccupation(playerId, copyId, star);

        if (cfg.type == CopyInstance.TYPE_LEADAWAY
                || cfg.type == CopyInstance.TYPE_GOLD) { //顺手牵羊,金币，奖励

            Map<Integer, Integer> rewardGoodsCount = null;
            if (cfg.type == CopyInstance.TYPE_LEADAWAY) {
                rewardGoodsCount = copy.getDropRewardGoods();
            }

            for (Reward reward : result.rewards) {
                int count = reward.count;
                if (count < 0) {
                    continue;
                }
                if (cfg.rewards != null) {
                    boolean foundReward = false;
                    for (int[] rewards : cfg.rewards) {
                        int configCount = rewards[1];
                        if (rewards[0] == reward.id) {
                            foundReward = true;
                            if (configCount < count) {
                                int factor = count / configCount;
                                count = configCount / factor;

                                ServerLogger.warn("副本作弊，作弊玩家ID=" + playerId + " 物品ID=" + reward.id + " 作弊物品数量=" + reward.count + " 最终获得数量=" + count);
                                break;
                            }
                        }
                    }
                    // 没有找到奖励的物品，有可能是作弊修改了奖励的物品
                    if (!foundReward) {
                        continue;
                    }
                }
                else if (cfg.type == CopyInstance.TYPE_LEADAWAY)
                {
                    int maxAppearTime = (int)(60 / ConfigData.globalParam().LeadawayGoldAppearTime[0]);
                    if (rewardGoodsCount == null || !rewardGoodsCount.containsKey(reward.id)) { // 没有找到奖励的物品，有可能是作弊修改了奖励的物品
                        continue;
                    }
                    int configCount = rewardGoodsCount.get(reward.id);
                    if (count / configCount > maxAppearTime) { // 超过了奖励的次数，作弊了
                        count = configCount;
                    }
                }

                items.add(new GoodsEntry(reward.id, count));
            }
        }

        result.rewards = new ArrayList<>();
        // 构造奖励
        if (cfg.type == CopyInstance.TYPE_ENDLESS) {
            EndlessAttach attach = endlessLogic.getAttach(playerId);
            EndlessCfg eCfg = endlessLogic.getConfig();
            int multiple = (attach.getCurrLayer() / eCfg.sectionLayer + 1) * eCfg.sectionMultiple;

            for (GoodsEntry g : items) {
                GoodsConfig config = ConfigData.getConfig(GoodsConfig.class, g.id);
                //活动材料不翻倍
                if (config != null && config.type != Goods.ACTIVITY_MATERIAL) {
                    g.count *= multiple;
                }
            }
            //无尽漩涡称号
            titleService.complete(playerId, TitleConsts.WJXW_LAYER, attach.getMaxLayer(), ActivityConsts.UpdateType.T_VALUE);
        } else if (cfg.type == CopyInstance.TYPE_TRAVERSING) {

            List<Reward> affixReward = traversingService.takeReward(playerId, playerId, copy.getTraverseMap());
            if (affixReward != null) {
                result.rewards.addAll(affixReward);
            }
        }

        goodsService.addRewards(playerId, items, LogConsume.COPY_REWARD, copyId);
        for (GoodsEntry g : items) {
            Reward reward = new Reward();
            reward.id = g.id;
            reward.count = g.count;
            result.rewards.add(reward);
        }
        // 特殊物品公告
        String myName = playerService.getPlayer(playerId).getName();
        for (GoodsNotice g : copy.getSpecReward()) {
            messageService.sendSysMsg(g.getNoticeId(), myName, g.getGoodsName());
        }

        if (cfg.type == CopyInstance.TYPE_COMMON) {
            CopyRank rank = updateCopyRank(playerId, copyId, result.time);
            result.passTime = rank.getPassTime();
            result.name = rank.getName();
        }
        return result;
    }

    public List<GoodsEntry> calculateCopyReward(int playerId, int copyId, int star) {
        Player player = playerService.getPlayer(playerId);
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        Map<Integer, Integer> totalRewards = new HashMap<>();
        // 构造奖励
        List<GoodsEntry> items = new ArrayList<>();

        if (cfg.type == CopyInstance.TYPE_LEADAWAY ||
                cfg.type == CopyInstance.TYPE_GOLD) {
            return items;
        }
        // 副本奖励
        if (cfg.rewards != null) {
            for (int i = 0; i < cfg.rewards.length; i++) {
                int[] item = cfg.rewards[i];
                Reward reward = new Reward();
                reward.id = item[0];
                reward.count = item[1];
                addItem(totalRewards, reward.id, reward.count);
            }
        }
        // 掉落
        CopyInstance copy = instances.get(playerService.getPlayer(playerId).getCopyId());
        if (copy != null) {
            for (Entry<Integer, Integer> drop : copy.getDrops().entrySet()) {
                int id = drop.getKey();
                int count = drop.getValue();
                addItem(totalRewards, id, count);
            }
        }
        // 随机奖励
        if (cfg.randomRates != null) {
            int id;
            int count;
            if (cfg.type == CopyInstance.TYPE_TRAVERSING) { //时空仪奖励
                List<int[]> rewardsList = new ArrayList<>();
                List<Integer> rateList = Lists.newArrayListWithCapacity(cfg.randomRates.length);
                for (int i = 0; i < cfg.randomRewards.length; i++) {
                    if (ConfigData.getConfig(GoodsConfig.class, cfg.randomRewards[i][0]).vocation == player.getVocation()) {
                        rewardsList.add(cfg.randomRewards[i]);
                        rateList.add(cfg.randomRates[i]);
                    }
                }
                int index = RandomUtil.getRandomIndex(rateList);
                int[] itemArr = rewardsList.get(index);
                id = itemArr[0];
                count = itemArr[1];
            } else {
                //避免概率和奖励配错的情况
                int length = cfg.randomRates.length;
                if (cfg.randomRates.length != cfg.randomRewards.length) {
                    ServerLogger.err(null, "randomRates and randomRewards unequal" + copyId);
                    if (length > cfg.randomRewards.length)
                        length = cfg.randomRewards.length;
                }

                int index = RandomUtil.getRandomIndex(cfg.randomRates, length);
                id = cfg.randomRewards[index][0];
                count = cfg.randomRewards[index][1];
            }

            if (ConfigData.getConfig(GoodsConfig.class, id) == null) {
                ServerLogger.warn("goods don't exist id = " + id);
            }
            if (id > 0 && count > 0) {
                addItem(totalRewards, id, count);
            }
        }
        // 3星奖励
        if (cfg.starRewards != null) {
            int[][] starRewards = cfg.starRewards.get(star);
            if (starRewards != null) {
                for (int i = 0; i < starRewards.length; i++) {
                    int id = starRewards[i][0];
                    int count = starRewards[i][1];
                    int vocation = ConfigData.getConfig(GoodsConfig.class, id).vocation;
                    if (vocation == 0 || vocation == player.getVocation()) {
                        if (id > 0 && count > 0) {
                            addItem(totalRewards, id, count);
                        }
                    }
                }
            }
        }

        for (Entry<Integer, Integer> item : totalRewards.entrySet()) {
            items.add(new GoodsEntry(item.getKey(), item.getValue()));
        }

        // 首次掉落(不计入各种加成）
        if (cfg.firstReward != null) {
            PlayerData data = playerService.getPlayerData(playerId);
            Copy copyVo = data.getCopys().get(copyId);
            if (copyVo == null || copyVo.getState() == 0) {
                for (int i = 0; i < cfg.firstReward.length; i++) {
                    int[] item = cfg.firstReward[i];
                    /*int vocation = ConfigData.getConfig(GoodsConfig.class, item[0]).vocation;
                    if (vocation != 0 && vocation != player.getVocation()) {
						continue;
					}*/
                    items.add(new GoodsEntry(item[0], item[1]));
                }

                //时空仪增加首充掉落
                if (cfg.type == CopyInstance.TYPE_TRAVERSING) {
                    if (copyVo == null) {
                        copyVo = new Copy();
                        data.getCopys().putIfAbsent(copyId, copyVo);
                    } else {
                        copyVo.setState(1);
                    }
                }
            }
        }

        //声望加成
        PlayerData data = playerService.getPlayerData(playerId);
//        if (data.getActivityCamp() != 0) {
//            int itemId = ConfigData.FameMap.get(data.getActivityCamp());
//            GoodsEntry g = new GoodsEntry(itemId, (int) ConfigData.globalParam().fameAddRate);
//            items.add(g);
//        }

        //公会科技加成
        if (player.getGangId() > 0) {
            for (GoodsEntry g : items) {
                if (g.id == Goods.COIN) {
                    for (int techId : data.getTechnologys()) {
                        GangScienceCfg conf = ConfigData.getConfig(GangScienceCfg.class, techId);
                        if (conf.type == 7) { //科技金币加成
                            g.count = Math.round(g.count * (1 + conf.param / 100.0f));
                        }
                    }
                } else if (g.id == Goods.EXP) {
                    for (int techId : data.getTechnologys()) {
                        GangScienceCfg conf = ConfigData.getConfig(GangScienceCfg.class, techId);
                        if (conf.type == 8) { //科技经验加成
                            g.count = Math.round(g.count * (1 + conf.param / 100.0f));
                        }
                    }
                } else {
                    GoodsConfig conf = ConfigData.getConfig(GoodsConfig.class, g.id);
                    if (conf.type == Goods.FAME) { //科技声望加成
                        for (int techId : data.getTechnologys()) {
                            GangScienceCfg config = ConfigData.getConfig(GangScienceCfg.class, techId);
                            if (config.type == 9) { //科技声望加成
                                g.count = Math.round(g.count * (1 + config.param / 100.0f));
                            }
                        }
                    }
                }
            }
        }

        //活动奖励
        Reward activityReward = activityReward(playerId, cfg.type);
        if (activityReward != null) {
            items.add(new GoodsEntry(activityReward.id, activityReward.count));
        }
        return items;
    }

    // 汇总奖励
    private void addItem(Map<Integer, Integer> items, int id, int count) {
        if (id == 0) {
            return;
        }
        Integer curCount = items.get(id);
        if (curCount == null) {
            curCount = 0;
        }
        curCount += count;
        items.put(id, curCount);
    }

    // 更新次数
    public void updateCopy(int playerId, CopyInstance copyInstance, CopyResult result) {
        if (result.star == 0) {
            result.star = 1;
        }
        int copyId = copyInstance.getCopyId();
        PlayerData playerData = playerService.getPlayerData(playerId);

        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        // 有次数的副本

        if (cfg.type == CopyInstance.TYPE_TRAIN) { //更新试练场挑战次数
            skillCardTrainService.updateCopyTimes(playerId, 1, copyId);
        } else {
            updateCopyTimes(copyId, playerData, cfg);
        }

        if (cfg.type == CopyInstance.TYPE_ENDLESS) {
            endlessLogic.updateLayer(playerId, result);
        } else if (cfg.type == CopyInstance.TYPE_TREASURE) {
            treasureLogic.updateCopy(playerId, result);
        } else if (cfg.type == CopyInstance.TYPE_EXPERIENCE) {
            experienceLogic.updateCopy(playerId, result);
        } else if (cfg.type == CopyInstance.TYPE_LEADAWAY) {
            leadAwayLogic.updateCopy(playerId, result);
        } else if (cfg.type == CopyInstance.TYPE_GOLD) {
            catchGoldLogic.updateCopy(playerId, result);
        }

        if (copyId != LEARN_ID) {
            Copy copy = playerData.getCopys().get(copyId);
            if (copy == null) {//组队时,普通队员没有copy对象
                copy = new Copy();
                playerData.getCopys().putIfAbsent(copyId, copy);
            }
            if (copy.getState() < result.star) {
                copy.setState(result.star);
            }
            playerData.getCopys().put(copyId, copy);
            // 更新数据到前端
            refreshCopyInfo(playerId, copyId, playerData);
        }

        Map<Integer, int[]> condParams = Maps.newHashMap();
        condParams.put(Task.FINISH_TRANSIT, new int[]{copyId, cfg.type, result.star, 1});
        //condParams.put(Task.TYPE_PASS_COPY_TEAM, new int[]{copyId, 1});
        condParams.put(Task.TYPE_PASS_COPY_SINGLE, new int[]{copyId, 1});

        taskService.doTask(playerId, condParams);
    }

    /**
     * 更新副本次数
     *
     * @param copyId
     * @param playerData
     * @param cfg
     */
    private void updateCopyTimes(int copyId, PlayerData playerData, CopyConfig cfg) {
        updateCopyTimes(copyId, playerData, cfg, 1);
    }

    /**
     * 更新副本次数
     *
     * @param copyId
     * @param playerData
     * @param cfg
     */
    private void updateCopyTimes(int copyId, PlayerData playerData, CopyConfig cfg, int times) {
        if (cfg.count > 0) {
            Integer count = playerData.getCopyTimes().get(copyId);
            if (count == null) {
                count = 0;
            }
            count += times;
            playerData.getCopyTimes().put(copyId, count);
        }
    }

    // 更新副本
    private CopyRank updateCopyRank(int playerId, int copyId, int sec) {
        SerialData data = serialDataService.getData();
        CopyRank rank = data.getCopyRanks().get(copyId);
        String name = playerService.getPlayer(playerId).getName();
        if (rank == null) {
            rank = new CopyRank();
            rank.setName(name);
            rank.setPassTime(sec);
            data.getCopyRanks().put(copyId, rank);
        } else {
            if (sec < rank.getPassTime()) {
                rank.setName(name);
                rank.setPassTime(sec);
                data.getCopyRanks().put(copyId, rank);

            }
        }
        return rank;
    }

    // 更新数据到前端
    private void refreshCopyInfo(int playerId, int copyId, PlayerData playerData) {

        Copy copy = playerData.getCopys().get(copyId);
        CopyVo vo = new CopyVo();
        vo.copyId = copyId;
        vo.state = (short) copy.getState();
        Integer count = playerData.getCopyTimes().get(copyId);
        vo.count = (short) (count == null ? 0 : count);

        Integer copyBuyTime = playerData.getCopyBuyTimes().get(copyId);
        vo.buyTimes = (short) (copyBuyTime == null ? 0 : copyBuyTime);

        Integer reset = playerData.getResetCopy().get(copyId);
        vo.reset = (short) (reset == null ? 0 : reset);

        CopyInfo info = getCopyInfo(playerId);
        info.copys = new ArrayList<CopyVo>(1);
        info.copys.add(vo);

        SessionManager.getInstance().sendMsg(CopyExtension.CMD_REFRESH, info, playerId);
    }

    // 创建副本实例
    public void createCopyInstance(int playerId, int copyId, int passId) {
        removeCopy(playerId);

        CopyInstance instance = new CopyInstance();
        instance.setCopyId(copyId);
        instance.setPassId(passId);
        Player player = playerService.getPlayer(playerId);
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, passId);
        if (cfg.type != CopyInstance.TYPE_LADDER) {
            for (int i = 0; i < cfg.scenes.length; i++) {
                int sceneId = cfg.scenes[i];
                Map<Integer, SMonsterVo> monsters = new ConcurrentHashMap<Integer, SMonsterVo>();
                Map<Integer, MonsterRefreshConfig> _monsters = null;
                if (cfg.type == CopyInstance.TYPE_ENDLESS) {
                    _monsters = endlessLogic.getSceneMonster(playerId, copyId, i + 1);
                } else {
                    _monsters = ConfigData.getSceneMonster(passId, i + 1);
                }
                if (_monsters == null) {
                    throw new RuntimeException(String.format("can not found the monster, copyid=%d,group=%d", copyId, i + 1));
                }
                for (MonsterRefreshConfig m : _monsters.values()) {
                    int monsterId = m.monsterId;
                    MonsterConfig monsterCfg = ConfigData.getConfig(MonsterConfig.class, monsterId);
                    SMonsterVo vo = new SMonsterVo();
                    if (monsterCfg == null) {
                        ServerLogger.warn("Err MonsterRefresh:" + m.id);
                    }
                    GlobalConfig globalConfig = ConfigData.globalParam();
                    float[] robotParas = globalConfig.RobotParas;
                    if (cfg.type == CopyInstance.TYPE_ENDLESS) {
                        EndlessCfg eCfg = endlessLogic.getConfig();
                        EndlessAttach attach = endlessLogic.getAttach(playerId);
                        int fight = Math.round(eCfg.baseData + (eCfg.baseData * (attach.getCurrLayer() - 1) * eCfg.growRatio
                                + eCfg.baseData * (attach.getCurrLayer() / eCfg.sectionLayer) * eCfg.sectionMultiple * eCfg.scetionRatio));
                        vo.curHp = vo.hp = Math.round(fight * 3.32f);
                        vo.attack = Math.round(fight * 0.18f);
                        vo.crit = Math.round(fight * 0.13f);
                        vo.defense = Math.round(fight * 0.05f);
                        vo.symptom = Math.round(fight * 0.1f);
                        vo.fu = Math.round(fight * 0.1f);
                    } else if (cfg.type == CopyInstance.TYPE_TRAIN) {
                        vo.curHp = vo.hp = Math.round(player.getFight() * robotParas[0]);
                        vo.attack = Math.round(player.getFight() * robotParas[1]);
                        vo.crit = Math.round(player.getFight() * robotParas[5]);
                        vo.defense = Math.round(player.getFight() * robotParas[2]);
                        vo.symptom = Math.round(player.getFight() * robotParas[3]);
                        vo.fu = Math.round(player.getFight() * robotParas[4]);
                    } else {
                        vo.curHp = vo.hp = monsterCfg.hp;
                        vo.attack = monsterCfg.physicAttack;
                        vo.crit = monsterCfg.crit;
                        vo.defense = monsterCfg.physicDefense;
                        vo.symptom = monsterCfg.symptom;
                    }
                    vo.monsterId = monsterId;
                    vo.id = m.id;
                    vo.wave = m.wave;
                    monsters.put(vo.id, vo);
                }
                instance.addMonsters(sceneId, monsters);
            }
        }
        int instanceId = uniId.incrementAndGet();
        if (cfg.type == CopyInstance.TYPE_GROUP) {
            instanceId = groupService.onEnterBattle(playerId, copyId);
        } else if (cfg.type == CopyInstance.TYPE_TRAVERSING) {
            instanceId = teamService.onEnterBattle(playerId);
        }
        player.setCopyId(instanceId);
        instances.put(instanceId, instance);

    }

    // 获取副本实例
    public CopyInstance getCopyInstance(int instanceId) {
        return instances.get(instanceId);
    }

    // 移除副本
    public void removeCopy(int playerId) {
        Player player = playerService.getPlayer(playerId);

        if (player.getCopyId() > 0) {
            player.setCopyId(0);
            CopyInstance copyIns = instances.get(playerId);
            if (copyIns != null && copyIns.getMembers().decrementAndGet() == 0) {
                instances.remove(player.getCopyId());
            }
        }
    }

    private Reward getDropReward(int dropId, Player player) {
        DropGoods drop = ConfigData.getConfig(DropGoods.class, dropId);
        if (drop == null) {
            return null;
        }
        int index = RandomUtil.getRandomIndex(drop.rate);
        // 计算概率
        int[] rewards = drop.rewards[index];
        if (rewards[0] == 0 || rewards[1] == 0) {// 没有随机到
            return null;
        }
        // 验证物品职业
        GoodsConfig goodsCfg = ConfigData.getConfig(GoodsConfig.class, rewards[0]);
        if (goodsCfg == null) {
            ServerLogger.warn("错误的掉落物品:" + rewards[0]);
            return null;
        }
        if (goodsCfg.vocation > 0 && player.getVocation() != goodsCfg.vocation) {
            return null;
        }
        Reward reward = new Reward();
        reward.id = rewards[0];
        reward.count = rewards[1];
        return reward;
    }

    // 杀死怪物
    public DropReward killMonster(int playerId, CMonster m) {
        int id = m.id;
        DropReward dropReward = new DropReward();
        dropReward.id = id;
        dropReward.rewards = new ArrayList<Reward>();
        dropReward.keyCode = RandomUtil.randInt(2, 100);
        dropReward.x = m.x;
        dropReward.z = m.z;

        Player player = playerService.getPlayer(playerId);

        CopyInstance copy = getCopyInstance(player.getCopyId());
        if (copy == null) {
            return dropReward;
        }
        SMonsterVo monster = copy.removeMonster(player.getSceneId(), id);

        if (monster == null) {
            return dropReward;
        }

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在" + playerId);
        }

        if (!player.checkHurt(m.hightHurt, 1.5f)) {
            // 超过1.5倍的最大范围值，肯定是作弊了
            ServerLogger.info("作弊:单次伤害超过1.5倍最大伤害范围，作弊玩家=" + playerId + " 副本=" + copy.getCopyId() + " harm=" + m.hightHurt);
            return dropReward;
        }

//        CopyConfig config  = ConfigData.getConfig(CopyConfig.class, copy.getCopyId());

        //防作弊
//        if (config.type == CopyInstance.TYPE_COMMON ||
//                config.type == CopyInstance.TYPE_ENDLESS)
        {
//            if (m.hp < monster.hp || monster.hp < m.hp * 1.5f) {
//            if (m.hp < monster.hp ||  monster.hp * 1.5f < m.hp) {
            if (m.hp < monster.hp) {
                // 两边血量不相等，作弊了
                ServerLogger.info("作弊:前后端血量不相等，作弊玩家=" + playerId + " 副本=" + copy.getCopyId() + " 服务器.hp=" + monster.hp + " 客户端.hp=" + m.hp);
                return  null;
            }
            else if (m.hightHurt <= 0 || m.hurt <= 0) {
                ServerLogger.info("作弊:伤害值记录为0，作弊玩家=" + playerId + " 副本=" + copy.getCopyId() + " hurt=" + m.hurt + " hightHurt=" + m.hightHurt);
                return  null;
            }
        }

        if (m.hp < monster.hp * 0.8 || m.hurt < monster.hp * 0.8) {
            ServerLogger.info("killMonster: 作弊玩家=" + playerId + " 副本=" + copy.getCopyId() + " 怪物id="+m.id + " m.hp=" + m.hp + " monster.hp=" +monster.hp + " m.hurt=" + m.hurt);
        }

        playerData.setHurt(playerData.getHurt() + monster.hp);

        MonsterConfig monsterCfg = GameData.getConfig(MonsterConfig.class, monster.monsterId);
        Map<Integer, int[]> condParams = Maps.newHashMap();
        condParams.put(Task.FINISH_KILL, new int[]{monsterCfg.type, monster.monsterId, 1});
        condParams.put(Task.TYPE_KILL, new int[]{monsterCfg.type, 1});
//        condParams.put(Task.TYPE_KILL, new int[]{0, 1});
        taskService.doTask(playerId, condParams);

        if (m.reward == 0) {// 不需要奖励
            return dropReward;
        }
        int dropIds[] = monsterCfg.dropGoods;
        if (dropIds == null) {
            return dropReward;
        }
        // 读取掉落配置
        for (int dropId : dropIds) {
            if (dropId == 0) {
                continue;
            }
            Reward reward = getDropReward(dropId, player);
            if (reward == null) {
                continue;
            }
            dropReward.rewards.add(reward);
            GoodsConfig goodsCfg = ConfigData.getConfig(GoodsConfig.class, reward.id);
            if (goodsCfg == null) {
                ServerLogger.warn("goods don't exist id = " + reward.id);
                continue;
            }
            if (goodsCfg.type != Goods.BOTTLE) {
                // 加入缓存
                addItem(copy.getDrops(), reward.id, reward.count);
            }
        }

        int copyId = copy.getCopyId();
        Copy myCopy = playerData.getCopys().get(copyId);
        if (myCopy == null) {
            myCopy = new Copy();
            playerData.getCopys().put(copyId, myCopy);
        }
        return dropReward;
    }

    // 检查副本结果,简单防一下时间
    public boolean checkCopyResult(int playerId, CopyInstance copy, CopyResult result) {
//        if (SysConfig.debug) {
//            return true;
//        }

        int copyId = copy.getCopyId();
        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copy.getCopyId());
        if ((cfg.type == CopyInstance.TYPE_COMMON ||
                cfg.type == CopyInstance.TYPE_ENDLESS) && !copy.isOver()) {
            ServerLogger.warn("Error Copy Fight, TYPE_ENDLESS is not over:", playerId, cfg.name, cfg.id);
            return false;
        }

        long now = System.currentTimeMillis();
        long pass = (now - copy.getCreateTime()) / TimeUtil.ONE_SECOND;
        if (pass <= 1) {
            ServerLogger.warn("Err Copy", result.id, result.star, result.time, result.combo, result.hp,
                    copy.getCopyId());
            return false;
        }

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            ServerLogger.warn("玩家数据不存在" + playerId);
            return false;
        }

        if (cfg.type != CopyInstance.TYPE_LEADAWAY &&
                cfg.type != CopyInstance.TYPE_GOLD &&
                cfg.type != CopyInstance.TYPE_EXPERIENCE &&
                cfg.type != CopyInstance.TYPE_TREASURE) { //不需要检测伤害
            if (playerData.getHurt() <= 0 || result.toltalHurt <= 0) {
                ServerLogger.info("伤害值记录小于等于0：作弊玩家=" + playerId + " copyId=" + copyId + " 统计伤害值=" + playerData.getHurt() + "上报伤害=" + result.toltalHurt + " 类型=" + cfg.type);
                return false;
            }

            if (!CheatReventionService.hasPlayerHurtRecord(playerId)) {
                ServerLogger.info("没有找到玩家伤害上报记录：作弊玩家=" + playerId + " copyId=" + copyId + " 统计伤害值=" + playerData.getHurt() + "上报伤害=" + result.toltalHurt + " 类型=" + cfg.type);
                return false;
            }
        }

        if (cfg.type == CopyInstance.TYPE_COMMON ||
                cfg.type == CopyInstance.TYPE_ENDLESS) {
            if ((result.toltalHurt < playerData.getHurt() * 0.8) || (playerData.getHurt() <= 0)) {
                ServerLogger.info("伤害值记录低于血量：作弊玩家=" + playerId + " copyId=" + copyId + " 统计伤害值=" + playerData.getHurt() + "上报伤害=" + result.toltalHurt + " 类型=" + cfg.type);
                return false;
            }
        }
        result.time = (int) pass;

        //更新自己最快记录
        Map<Integer, Integer> fastestRecordMap = playerData.getFastestRecordMap();
        if (fastestRecordMap == null) {
            fastestRecordMap = new ConcurrentHashMap<>();
        }
        if (fastestRecordMap.get(result.id) == null || result.time < fastestRecordMap.get(result.id)) {
            fastestRecordMap.put(result.id, result.time);
        }
        result.selfRecord = fastestRecordMap.get(result.id);

        //更新全服最快记录
        SerialData data = serialDataService.getData();
        if (data == null) {
            ServerLogger.warn("全服最快记录 data == null");
            return false;
        }
        Map<Integer, Int2Param> copyPassFastestTimeMap = data.getCopyPassFastestTimeMap();
        if (copyPassFastestTimeMap == null) {
            copyPassFastestTimeMap = new ConcurrentHashMap<>();
        }
        if (copyPassFastestTimeMap.get(result.id) == null || copyPassFastestTimeMap.get(result.id) == null || result.time < copyPassFastestTimeMap.get(result.id).param2 || result.selfRecord < copyPassFastestTimeMap.get(result.id).param2) {
            Int2Param int2Param = new Int2Param();
            int2Param.param1 = playerId;
            int2Param.param2 = result.time;
            if (result.selfRecord < result.time) {
                int2Param.param2 = result.selfRecord;
            }
            copyPassFastestTimeMap.put(result.id, int2Param);
            ServerLogger.warn("最快记录 playerId=" + playerId + ",result.time=" + result.time + ",cfg.type=" + cfg.type + ",cfg.id=" + cfg.id);
        }

        //每次获取最新值保持数据同步
        Int2Param int2Param1 = copyPassFastestTimeMap.get(result.id);
        if (int2Param1 == null) {
            ServerLogger.warn("数据同步");
            return false;
        }
        Player player = playerService.getPlayer(int2Param1.param1);
        if (player == null) {
            ServerLogger.warn("没有玩家=" + int2Param1.param1 + " result=" + result.id);
            int2Param1.param1 = playerId;
            int2Param1.param2 = result.time;
            player = playerService.getPlayer(playerId);
            copyPassFastestTimeMap.put(result.id, int2Param1);
        }
        RecordHolder recordHolder = new RecordHolder();
        recordHolder.id = player.getPlayerId();
        recordHolder.name = player.getName();
        recordHolder.record = int2Param1.param2;
        recordHolder.vocation = player.getVocation();
        recordHolder.lv = player.getLev();
        recordHolder.vip = player.getVip();

        result.recordHolder = new ArrayList<>();
        result.recordHolder.add(recordHolder);

        //检查一下战力
        /*
        Player player = playerService.getPlayer(playerId);
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copy.getCopyId());
		if(cfg.type!=CopyInstance.TYPE_ACTIVITY&&(cfg.recommendFight>=100000&&cfg.recommendFight>player.getFight()*2)){
			ServerLogger.warn("Error Copy Fight:",playerId,cfg.recommendFight,player.getFight(),cfg.name,cfg.id);
			return false;
		}*/
        return true;
    }

    // 复活
    public int revive(int playerId, int copyId, int count) {
        CopyConfig copyCfg = ConfigData.getConfig(CopyConfig.class, copyId);
        // 复活价格
        List<GoodsEntry> cost = new ArrayList<GoodsEntry>(copyCfg.reviveCost.length);
        for (int[] item : copyCfg.reviveCost) {
            cost.add(new GoodsEntry(item[0], item[1] * count));
        }
        int code = goodsService.decConsume(playerId, cost, LogConsume.REVIVE,
                copyId);
        if (code != Response.SUCCESS) {
            return code;
        }
        return Response.SUCCESS;
    }

    // 副本扫荡
    public CopyReward swipeCopy(int playerId, int copyId, int times) {
        CopyReward result = new CopyReward();
        result.reward = new ArrayList<>();

        CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (cfg.type == CopyInstance.TYPE_TRAIN) {
            Train train = playerData.getTrain();
            TrialFieldCfg trainCfg = ConfigData.trainCopy.get(copyId);
            int count = train.getGroupTimes().get(trainCfg.type);
            int totalCount = ConfigData.trainCount.get(trainCfg.type);
            if ((times + count) > totalCount) {
                result.code = Response.NO_TODAY_TIMES;
                return result;
            }
        } else {
            // 检查次数
            if (cfg.count > 0) {// 普通副本
                Integer count = playerData.getCopyTimes().get(copyId);
                if (count == null) {
                    count = 0;
                }

                Integer buyTimes = playerData.getCopyBuyTimes().get(copyId);
                if (buyTimes == null) {
                    buyTimes = 0;
                }

                if (count + times > cfg.count + buyTimes) {
                    result.code = Response.NO_TODAY_TIMES;
                    return result;
                }
            }
        }
        // 活动副本扣除体力
        if (cfg.needEnergy > 0) {
            if (!playerService.decEnergy(playerId, cfg.needEnergy * times, LogConsume.COPY_ENERGY, copyId)) {
                result.code = Response.NO_ENERGY;
                return result;
            }
        }

        if (cfg.type == CopyInstance.TYPE_TRAIN) {
            skillCardTrainService.updateCopyTimes(playerId, times, copyId);
        } else {
            Map<Integer, Integer> sweepNeedGoods = Maps.newHashMap();
            for (Map.Entry<Integer, Integer> e : ConfigData.globalParam().sweepNeedGoods.entrySet()) {
                sweepNeedGoods.put(e.getKey(), e.getValue() * times);
            }
            if (goodsService.decConsume(playerId, sweepNeedGoods, LogConsume.SWEEP_COPY) > 0) {
                result.code = Response.NO_MATERIAL;
                return result;
            }
            boolean show = shopService.triggerMysteryShop(playerId, copyId, times, null);
            if (show) {
                result.showMystery = true;
            }
            // 更新副本次数
            updateCopyTimes(copyId, playerData, cfg, times);
        }
        Map<Integer, GoodsEntry> map = Maps.newHashMap();
//        for (int i = 0; i < times; i++) {
//            RewardList list = new RewardList();
//            list.rewards = swipeCopyInner(playerId, copyId, map);
//            result.reward.add(list);
//        }
        List<RewardList> rewardLists = swipeCopyInner(playerId, copyId, times, map);
        if (rewardLists != null) {
            for (int i = 0; i < rewardLists.size(); ++i) {
                result.reward.add(rewardLists.get(i));
            }
        }

        goodsService.addRewards(playerId, Lists.newArrayList(map.values()), LogConsume.COPY_REWARD, copyId);
        Map<Integer, int[]> condParams = Maps.newHashMapWithExpectedSize(2);
        if (cfg.type == CopyInstance.TYPE_LEADAWAY
                || cfg.type == CopyInstance.TYPE_GOLD
                || cfg.type == CopyInstance.TYPE_ENDLESS
                || cfg.type == CopyInstance.TYPE_TREASURE
                || cfg.type == CopyInstance.TYPE_TRAIN
                || cfg.type == CopyInstance.TYPE_TRAVERSING
                || cfg.type == CopyInstance.TYPE_EXPERIENCE) {
            condParams.put(Task.TYPE_PASS_TYPE_COPY, new int[]{cfg.type, times});
        }
        condParams.put(Task.TYPE_SWIPE_COPY, new int[]{copyId, times});
        taskService.doTask(playerId, condParams);
        refreshCopyInfo(playerId, copyId, playerData);

        //杀怪任务
        if (cfg.type == CopyInstance.TYPE_COMMON) {
            List<Map<Integer, int[]>> monsterMapList = Lists.newArrayList();
            for (int i = 0; i < cfg.scenes.length; i++) {
                Map<Integer, MonsterRefreshConfig> sceneMonster = ConfigData.getSceneMonster(cfg.id, i + 1);
                if (sceneMonster == null) {
                    ServerLogger.warn("未发现怪物，副本id=" + cfg.id);
                    continue;
                }
                for (MonsterRefreshConfig vo : sceneMonster.values()) {
                    MonsterConfig monsterCfg = GameData.getConfig(MonsterConfig.class, vo.monsterId);
                    if (monsterCfg == null) {
                        ServerLogger.warn("未发现怪物，怪物id=" + vo.monsterId);
                        continue;
                    }
                    Map<Integer, int[]> monsterMap = Maps.newHashMap();
                    monsterMap.put(Task.FINISH_KILL, new int[]{monsterCfg.type, vo.monsterId, times});
                    monsterMap.put(Task.TYPE_KILL, new int[]{monsterCfg.type, times});

                    monsterMapList.add(monsterMap);
                }
            }
            taskService.doTaskList(playerId, monsterMapList);
        }

        return result;
    }


    // 扫荡副本
    public List<RewardList> swipeCopyInner(int playerId, int copyId, Map<Integer, GoodsEntry> map) {
        return swipeCopyInner(playerId, copyId, 1, map);
    }

    public List<RewardList> swipeCopyInner(int playerId, int copyId, int count, Map<Integer, GoodsEntry> map) {

        Map<Integer, int[]> condParams = Maps.newHashMapWithExpectedSize(1);

        List<RewardList> rewardLists = Lists.newArrayList();
        for (int i = 0; i < count; ++i) {
            createCopyInstance(playerId, copyId, copyId);
            int star = 1;
            Copy copy = playerService.getPlayerData(playerId).getCopys().get(copyId);
            CopyConfig cfg = GameData.getConfig(CopyConfig.class, copyId);
            if (copy != null) {
                star = copy.getState();
            } else if (cfg.type != CopyInstance.TYPE_TREASURE && cfg.type != CopyInstance.TYPE_EXPERIENCE
                    && cfg.type != CopyInstance.TYPE_LEADAWAY) {
                return null;
            }
            List<GoodsEntry> copyRewards = calculateCopyReward(playerId, copyId, star);
            for (GoodsEntry goodsEntry : copyRewards) {
                GoodsEntry goodsEntryTmp = map.get(goodsEntry.id);
                if (goodsEntryTmp == null) {
                    map.put(goodsEntry.id, goodsEntry);
                } else {
                    goodsEntryTmp.count = goodsEntryTmp.count + goodsEntry.count;
                }
            }

            //goodsService.addRewards(playerId, copyRewards, LogConsume.COPY_REWARD, copyId);
            List<Reward> rewards = new ArrayList<>(copyRewards.size());
            for (GoodsEntry item : copyRewards) {
                Reward r = new Reward();
                r.id = item.id;
                r.count = item.count;
                rewards.add(r);
            }

            RewardList list = new RewardList();
            list.rewards = rewards;
            rewardLists.add(list);

            condParams.put(Task.FINISH_TRANSIT, new int[]{copyId, cfg.type, star, count});
            removeCopy(playerId);
        }

        taskService.doTask(playerId, condParams);
        return rewardLists;
    }

    // 重置副本
    public int resetCopy(int playerId, int copyId) {
        // 判断次数
        PlayerData data = playerService.getPlayerData(playerId);
        Integer count = data.getResetCopy().get(copyId);
        if (count == null) {
            count = 0;
        }

        VIPConfig vip = ConfigData.getConfig(VIPConfig.class, playerService.getPlayer(playerId).getVip());
        if (count >= vip.resetCopy) {
            return Response.NO_TODAY_TIMES;
        }
        // 扣钱
        if (!playerService.decDiamond(playerId, ConfigData.globalParam().resetCopyPrice, LogConsume.RESET_COPY)) {
            return Response.NO_DIAMOND;
        }
        // 加重置次数
        data.getResetCopy().put(copyId, count + 1);
        // 清除已调整次数
        data.getCopyTimes().remove(copyId);
        // 更新数据到前端
        refreshCopyInfo(playerId, copyId, data);
        return Response.SUCCESS;
    }

    // 获得三星奖励
    public Int2Param get3starReward(int playerId, int id) {
        Int2Param param = new Int2Param();
        // 有无领取过
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getThreeStars().contains(id)) {
            param.param1 = Response.SYS_ERR;
            return param;
        }
        // 验证条件
        // 设置已经领取
        data.getThreeStars().add(id);
        // 加物品
        ThreeStarRewardCfg cfg = ConfigData.getConfig(ThreeStarRewardCfg.class, id);
        // 更新
        goodsService.addRewards(playerId, cfg.rewards, LogConsume.THREE_STAR);
        param.param1 = Response.SUCCESS;
        param.param2 = id;
        return param;
    }

    /**
     * 购买主线副本次数
     *
     * @param playerId
     * @param copyId
     */
    public void buyMainCopyTimes(int playerId, int copyId) {
        Player player = playerService.getPlayer(playerId);
        PlayerData data = playerService.getPlayerData(playerId);

        VIPConfig config = ConfigData.getConfig(VIPConfig.class, player.getVip());
        Integer count = data.getCopyBuyTimes().get(copyId);
        if (count == null) count = 0;

        if (count >= config.buyMainCopy) {
            return;
        }

        Integer price = ConfigData.globalParam().buyMainCopyPrice.get(Goods.DIAMOND);
        if (price == null) {
            return;
        }

        if (!playerService.decDiamond(playerId, price, LogConsume.BUY_COPY_TIMES)) {
            return;
        }

        data.getCopyBuyTimes().put(copyId, count + 1);

        refreshCopyInfo(playerId, copyId, data);

        //购买副本活动
        CopyConfig copyConfig = ConfigData.getConfig(CopyConfig.class, copyId);
        if (copyConfig == null) {
            ServerLogger.warn("副本不存在，副本id=" + copyId);
            return;
        }

        if (copyConfig.difficulty == CopyInstance.HARD) {
            activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_DIFFICULT_COPY_PURCHASE);
        }
    }

    /**
     * 更新世界记录和自身记录
     */
    public void updateRecord(int playerId, SEnterCopy result) {
        //获取全服记录
        SerialData data = serialDataService.getData();
        if (data == null) {
            return;
        }
        Map<Integer, Int2Param> copyPassFastestTimeMap = data.getCopyPassFastestTimeMap();
        if (copyPassFastestTimeMap == null || copyPassFastestTimeMap.get(result.copyId) == null) {
            return;
        }
        Int2Param int2Param = copyPassFastestTimeMap.get(result.copyId);
        if (int2Param == null) {
            return;
        }

        //每次获取最新值保持数据同步
        Player player = playerService.getPlayer(int2Param.param1);
        if (player == null) {
            return;
        }
        RecordHolder recordHolder = new RecordHolder();
        recordHolder.id = player.getPlayerId();
        recordHolder.name = player.getName();
        recordHolder.record = int2Param.param2;
        recordHolder.vocation = player.getVocation();
        recordHolder.lv = player.getLev();
        recordHolder.vip = player.getVip();

        result.recordHolder = new ArrayList<>();
        result.recordHolder.add(recordHolder);

        //获取自身记录
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return;
        }
        Map<Integer, Integer> fastestRecordMap = playerData.getFastestRecordMap();
        if (fastestRecordMap == null || fastestRecordMap.get(result.copyId) == null) {
            return;
        }
        result.selfRecord = fastestRecordMap.get(result.copyId);
    }

    //活动奖励
    public Reward activityReward(int playerId, int copyType) {
        Collection<Object> activityDropCfgs = ConfigData.getConfigs(ActivityDropCfg.class);
        Reward reward = new Reward();
        if (activityDropCfgs != null && !activityDropCfgs.isEmpty()) {
            for (Object object : activityDropCfgs) {
                ActivityDropCfg activityDropCfg = (ActivityDropCfg) object;
                //副本类型
                if (activityDropCfg.type == null) {
                    continue;
                }
                boolean isExistence = false;
                for (int i : activityDropCfg.type) {
                    if (copyType == i) {
                        isExistence = true;
                        break;
                    }
                }
                if (!isExistence) {
                    continue;
                }
                Player player = playerService.getPlayer(playerId);
                if (player == null) {
                    break;
                }
                if (player.getVip() < activityDropCfg.VipLev) {
                    break;
                }
                if (player.getLev() < activityDropCfg.lev[0] || player.getLev() > activityDropCfg.lev[1]) {
                    break;
                }
                LocalDateTime nowDate = LocalDateTime.now();
                //指定时间开启
                if (activityDropCfg.TimeType == ActivityConsts.ActivityDropTimeCondType.T_APPOINT_TIME) {
                    if (activityDropCfg.BeginTime != null && !"".equals(activityDropCfg.BeginTime)) {
                        LocalDateTime beginDate = LocalDateTime.parse(activityDropCfg.BeginTime, TimeUtil.formatter);
                        if (nowDate.isBefore(beginDate)) { //还未开启
                            continue;
                        }
                    }
                    if (activityDropCfg.EndTime != null && !"".equals(activityDropCfg.EndTime)) {
                        LocalDateTime beginDate = LocalDateTime.parse(activityDropCfg.EndTime, TimeUtil.formatter);
                        if (nowDate.isAfter(beginDate)) { //活动结束
                            continue;
                        }
                    }
                }
                PlayerData playerData = playerService.getPlayerData(playerId);
                if (playerData == null) {
                    break;
                }
                //构造奖励
                int length = activityDropCfg.randomRates.length;
                if (activityDropCfg.randomRates.length != activityDropCfg.randomRewards.length) {
                    ServerLogger.err(null, "randomRates and randomRewards unequal" + copyType);
                    if (length > activityDropCfg.randomRewards.length)
                        length = activityDropCfg.randomRewards.length;
                }
                int index = RandomUtil.getRandomIndex(activityDropCfg.randomRates, length);
                reward.id = activityDropCfg.randomRewards[index][0];
                reward.count = activityDropCfg.randomRewards[index][1];
                if (reward.count == 0) {
                    break;
                }
                //获取奖励次数
                Map<Integer, Integer> activityDropTimeMap = playerData.getActivityDropTimeMap();
                if (activityDropTimeMap == null) {
                    activityDropTimeMap = new ConcurrentHashMap<>();
                }
                if (activityDropTimeMap.get(activityDropCfg.id) == null) {
                    activityDropTimeMap.put(activityDropCfg.id, 1);
                } else {
                    activityDropTimeMap.put(activityDropCfg.id, activityDropTimeMap.get(activityDropCfg.id) + 1);
                }
                //超过次数
                if (activityDropTimeMap.get(activityDropCfg.id) > activityDropCfg.count) {
                    break;
                }
                return reward;
            }
        }
        return null;
    }

//    //获取满星副本排行
//    public ListParam<NormalCopyRankVO> getMaxStarCopyRankings() {
//        ListParam listParam = new ListParam();
//
//        SerialData serialData = serialDataService.getData();
//        if (serialData == null) {
//            ServerLogger.warn("序列化数据不存在");
//            listParam.code = Response.ERR_PARAM;
//            return listParam;
//        }
//
//        listParam.params = new ArrayList<>(serialDataService.getData().getCopyRankingsMap().values());
//        Collections.sort(listParam.params, COMPARATOR);
//        return listParam;
//    }
//
//    //满星副本排序
//    private static final Comparator<NormalCopyRankVO> COMPARATOR = new Comparator<NormalCopyRankVO>() {
//        @Override
//        public int compare(NormalCopyRankVO o1, NormalCopyRankVO o2) {
//            if (o1.count == o2.count) {
//                return o2.fightingValue - o1.fightingValue;
//            }
//            return o2.count - o1.count;
//        }
//    };
//
//    //更新满星副本排行
//    public void updateMaxStarCopyRankings(int playerId, int value) {
//        Player player = playerService.getPlayer(playerId);
//        if (player == null) {
//            ServerLogger.warn("玩家不存在，玩家ID=" + playerId);
//            return;
//        }
//
//        SerialData serialData = serialDataService.getData();
//        if (serialData == null) {
//            ServerLogger.warn("序列化数据不存在");
//            return;
//        }
//
//        NormalCopyRankVO normalCopyRankVO = new NormalCopyRankVO();
//        normalCopyRankVO.name = player.getName();
//        normalCopyRankVO.level = player.getLev();
//        normalCopyRankVO.vocation = player.getVocation();
//        normalCopyRankVO.fightingValue = player.getFight();
//        normalCopyRankVO.playerId = playerId;
//        normalCopyRankVO.count = value;
//        serialDataService.getData().getCopyRankingsMap().put(playerId, normalCopyRankVO);
//    }
}
