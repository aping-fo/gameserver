package com.game.util;

import com.game.data.*;
import com.game.module.copy.CopyInstance;
import com.game.module.shop.ShopService;
import com.game.module.task.Task;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.util.GameData;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一些不是唯一key的配置，可以在这里定义一些辅助函数操作
 */
public class ConfigData {

    // [场景id_难度:怪]
    private static Map<String, Map<Integer, MonsterRefreshConfig>> monsterRefreshs = new HashMap<>();

    private static GlobalConfig globalCfg;

    // 副本，按章节，难度归类
    private static Map<Integer, Map<Integer, List<Integer>>> copys = new HashMap<>();
    // 无尽漩涡副本
    public static int[] endlessCopys = new int[2];

    // 充值配置
    private static List<ChargeConfig> charges = new ArrayList<>();

    // 所有任务
    private static Map<Integer, List<Integer>> tasks = new HashMap<>();

    // 技能卡合成
    private static Map<String, List<Integer>> skillCardRates = new HashMap<>();
    private static Map<String, List<Integer>> skillCardIds = new HashMap<>();

    private static Map<String, ArtifactLevelUpCfg> artifactLevelUpCfgs = new ConcurrentHashMap<>();
    private static Map<Integer, Integer> artifactMaxLevel = new ConcurrentHashMap<>();

    public static List<Integer> getSkillCardIds(int type, int quality) {
        return skillCardIds.get(String.format("%d_%d", type, quality));
    }

    public static List<Integer> getSkillCardRates(int type, int quality) {
        return new ArrayList<Integer>(skillCardRates.get(String.format("%d_%d",
                type, quality)));
    }

    public static Map<Integer, List<Integer>> RefreshIds = new HashMap<>();
    public static Map<Integer, List<Integer>> RefreshRates = new HashMap<>();

    public static Set<Integer> copy4Mystery = new ConcurrentSet<>();

    public static Map<Integer, List<RewardMailCfg>> rewardMails = new ConcurrentHashMap<>();
    public static Map<String, Integer> guildTechnology = new HashMap<>();
    public static Map<Integer, Integer> leadawayAwardsDrop = new HashMap<>();
    public static Set<String> accountSet = new HashSet<>();
    public static Map<Integer, Integer> trainCount = new HashMap<>();
    public static Map<Integer, TrialFieldCfg> trainCopy = new HashMap<>();
    public static Map<Integer, List<ActivityTaskCfg>> ActivityTasks = Maps.newConcurrentMap();
    //主动技能
    public static Map<Integer, List<Integer>> PassiveSkills = Maps.newHashMap();
    //套装
    public static Map<Integer, Integer> SuitMap = Maps.newHashMap();
    //公会副本
    public static Map<Integer, List<MonsterRefreshConfig>> GangMonsters = new HashMap<>();

    public static Map<Integer, int[]> DonateCfg = new HashMap<>();
    //声望
    public static Map<Integer, Integer> FameMap = new HashMap<>();

    // 获取充值配置
    public static List<ChargeConfig> getCharges() {
        return charges;
    }

    //礼包
    public static Map<String, CdkeyConfig> giftBagMap = new HashMap<>();

    public static Map<Integer, List<String>> FirstNameList = Maps.newHashMap();
    public static Map<Integer, List<String>> SecondNameList = Maps.newHashMap();

    // 获取章节副本
    public static List<Integer> getChapterCopys(int chapter, int difficult) {
        Map<Integer, List<Integer>> chapterCopys = copys.get(chapter);
        if (chapterCopys != null) {
            return chapterCopys.get(difficult);
        }
        return null;
    }

    // 每日任务
    public static List<Integer> getDailyTasks() {
        return tasks.get(Task.TYPE_DAILY);
    }

    public static List<Integer> getWeeklyTasks() {
        return tasks.get(Task.TYPE_CHELLENGE);
    }

    public static List<Integer> getJointTasks() {
        return tasks.get(Task.TYPE_JOINT);
    }

    public static List<Integer> getGangTasks() {
        return tasks.get(Task.TYPE_GANG);
    }

    public static Map<String, ArtifactLevelUpCfg> getArtifactLevelUpCfgs() {
        return artifactLevelUpCfgs;
    }

    public static Map<Integer, Integer> getArtifactMaxLevel() {
        return artifactMaxLevel;
    }

    // 全局配置表
    public static GlobalConfig globalParam() {
        return globalCfg;
    }

    public static <T> T getConfig(Class<T> t, int id) {
        T cfg = GameData.getConfig(t, id);
        return cfg;
    }

    public static Collection<Object> getConfigs(Class<?> t) {
        return GameData.getConfigs(t);
    }

    // 场景怪
    public static Map<Integer, MonsterRefreshConfig> getSceneMonster(
            int copyId, int group) {
        String key = String.format("%d_%d", copyId, group);
        return monsterRefreshs.get(key);
    }

    public static void init() {

        globalCfg = GameData.getConfig(GlobalConfig.class, 1);

        Map<String, Map<Integer, MonsterRefreshConfig>> monsterRefreshsTmp = new HashMap<>();
        for (Object monster : GameData.getConfigs(MonsterRefreshConfig.class)) {
            MonsterRefreshConfig m = (MonsterRefreshConfig) monster;
            String key = String.format("%d_%d", m.copyId, m.group);
            Map<Integer, MonsterRefreshConfig> monsters = monsterRefreshsTmp.get(key);
            if (monsters == null) {
                monsters = new HashMap<Integer, MonsterRefreshConfig>();
                monsterRefreshsTmp.put(key, monsters);
            }
            monsters.put(m.id, m);
        }
        monsterRefreshs = monsterRefreshsTmp;


        // 副本章节难度归类
        Map<Integer, Map<Integer, List<Integer>>> copysTmp = new HashMap<Integer, Map<Integer, List<Integer>>>();
        int[] endlessCopysTmp = new int[2];

        for (Object cfg : GameData.getConfigs(CopyConfig.class)) {
            CopyConfig copy = (CopyConfig) cfg;

            if (copy.type == CopyInstance.TYPE_COMMON) {
                Map<Integer, List<Integer>> chapterCopys = copysTmp.get(copy.chapterId);
                if (chapterCopys == null) {
                    chapterCopys = new HashMap<Integer, List<Integer>>();
                    copysTmp.put(copy.chapterId, chapterCopys);
                }
                List<Integer> difficultCopys = chapterCopys.get(copy.difficulty);
                if (difficultCopys == null) {
                    difficultCopys = new ArrayList<Integer>();
                    chapterCopys.put(copy.difficulty, difficultCopys);
                }
                if (!difficultCopys.contains(copy.id)) {
                    difficultCopys.add(copy.id);
                }
            } else if (copy.type == CopyInstance.TYPE_ENDLESS) {
                endlessCopysTmp[copy.difficulty - 1] = copy.id;
            }
        }
        copys = copysTmp;
        endlessCopys = endlessCopysTmp;


        // 每日任务
        Map<Integer, List<Integer>> tasksTmp = new HashMap<>();
        for (Object cfg : GameData.getConfigs(TaskConfig.class)) {
            TaskConfig task = (TaskConfig) cfg;
            List<Integer> list = tasksTmp.get(task.taskType);
            if (list == null) {
                list = new ArrayList<>();
                tasksTmp.put(task.taskType, list);
            }
            list.add(task.id);
        }
        tasks = tasksTmp;

        // 充值排序
        List<ChargeConfig> chargesTmp = new ArrayList<>();
        for (Object cfg : GameData.getConfigs(ChargeConfig.class)) {
            ChargeConfig charge = (ChargeConfig) cfg;
            chargesTmp.add(charge);
        }
        Collections.sort(chargesTmp, new Comparator<ChargeConfig>() {
            @Override
            public int compare(ChargeConfig o1, ChargeConfig o2) {
                if (o1.rmb != o2.rmb) {
                    return (int) Math.ceil(o1.rmb - o2.rmb);
                }
                return o1.type - o2.type;
            }
        });
        charges = chargesTmp;

        // 设置一下物品的最大叠加数量
        Map<Integer, Integer> fameMapTmp = new HashMap<>();
        for (Object cfg : GameData.getConfigs(GoodsConfig.class)) {
            GoodsConfig g = (GoodsConfig) cfg;
            if (g.maxStack >= 9) {
                g.maxStack = 99999999;
            }

            if (g.type == 122) { //
                fameMapTmp.put(g.param1[0], g.id);
            }
        }
        FameMap = fameMapTmp;

        // 技能卡合成
        Map<String, List<Integer>> skillCardRatesTmp = new HashMap<>();
        Map<String, List<Integer>> skillCardIdsTmp = new HashMap<>();
        for (Object cfg : GameData.getConfigs(SkillCardComposeCfg.class)) {
            SkillCardComposeCfg g = (SkillCardComposeCfg) cfg;
            String key = String.format("%d_%d", g.type, g.quality);
            List<Integer> ids = skillCardIdsTmp.get(key);
            if (ids == null) {
                ids = new ArrayList<Integer>();
                skillCardIdsTmp.put(key, ids);
            }
            ids.add(g.id);
            List<Integer> rates = skillCardRatesTmp.get(key);
            if (rates == null) {
                rates = new ArrayList<Integer>();
                skillCardRatesTmp.put(key, rates);
            }
            rates.add(g.rate);
        }
        skillCardIds = skillCardIdsTmp;
        skillCardIds = skillCardIdsTmp;

        // 刷新商店
        Map<Integer, List<Integer>> RefreshIdsTmp = new HashMap<>();
        Map<Integer, List<Integer>> RefreshRatesTmp = new HashMap<>();
        for (Object cfg : GameData.getConfigs(ShopCfg.class)) {
            ShopCfg g = (ShopCfg) cfg;
            if (g.tab != ShopService.REFRESH) {
                continue;
            }
            List<Integer> ids = RefreshIdsTmp.get(g.shopType);
            if (ids == null) {
                ids = new ArrayList<>();
                RefreshIdsTmp.put(g.shopType, ids);
            }
            List<Integer> rates = RefreshRatesTmp.get(g.shopType);
            if (rates == null) {
                rates = new ArrayList<>();
                RefreshRatesTmp.put(g.shopType, rates);
            }
            ids.add(g.id);
            rates.add(g.refreshRate);
        }
        RefreshRates = RefreshRatesTmp;
        RefreshIds = RefreshIdsTmp;

        // 神秘商店的特殊副本
        Set<Integer> copy4MysteryTmp = new ConcurrentSet<>();
        for (int id : globalCfg.mysterySpecialCopyIds) {
            copy4MysteryTmp.add(id);
        }
        copy4Mystery = copy4MysteryTmp;

        Map<Integer, List<RewardMailCfg>> rewardMailsTmp = new ConcurrentHashMap<>();
        for (Object obj : GameData.getConfigs(RewardMailCfg.class)) {
            RewardMailCfg cfg = (RewardMailCfg) obj;
            List<RewardMailCfg> list = rewardMails.get(cfg.group);
            if (list == null) {
                list = new ArrayList<>();
                rewardMails.put(cfg.group, list);
            }
            list.add(cfg);
        }
        rewardMails = rewardMailsTmp;

        Map<String, ArtifactLevelUpCfg> artifactLevelUpCfgsTmp = new ConcurrentHashMap<>();
        Map<Integer, Integer> artifactMaxLevelTmp = new ConcurrentHashMap<>();
        for (Object obj : GameData.getConfigs(ArtifactLevelUpCfg.class)) {
            ArtifactLevelUpCfg cfg = (ArtifactLevelUpCfg) obj;
            artifactLevelUpCfgsTmp.put(cfg.sid + "_" + cfg.level, cfg);
            artifactMaxLevelTmp.put(cfg.sid, cfg.level);
        }
        artifactLevelUpCfgs = artifactLevelUpCfgsTmp;
        artifactMaxLevel = artifactMaxLevelTmp;

        Map<Integer, Integer> leadawayAwardsDropTmp = new HashMap<>();
        for (Object obj : GameData.getConfigs(CopyConfig.class)) {
            CopyConfig cfg = (CopyConfig) obj;
            if (cfg.type == 11) {
                for (Object obj1 : GameData.getConfigs(MonsterRefreshConfig.class)) {
                    MonsterRefreshConfig conf = (MonsterRefreshConfig) obj1;
                    if (cfg.id == conf.copyId) {
                        MonsterConfig monsterConfig = getConfig(MonsterConfig.class, conf.monsterId);
                        int dropId = monsterConfig.dropGoods[0];
                        leadawayAwardsDropTmp.put(cfg.id, dropId);
                    }
                }
            }
        }
        leadawayAwardsDrop = leadawayAwardsDropTmp;

        Map<String, Integer> guildTechnologyTmp = new HashMap<>();
        for (Object obj : GameData.getConfigs(GangScienceCfg.class)) {
            GangScienceCfg cfg = (GangScienceCfg) obj;
            if (cfg.lv == 0) {
                guildTechnologyTmp.put(cfg.type + "_" + cfg.NeedLevel, cfg.id);
            }
        }
        guildTechnology = guildTechnologyTmp;

        Set<String> accountSetTmp = new HashSet<>();
        for (Object obj : GameData.getConfigs(AccountCfg.class)) {
            AccountCfg cfg = (AccountCfg) obj;
            accountSetTmp.add(cfg.name);
        }
        accountSet = accountSetTmp;


        Map<Integer, Integer> trainCountTmp = new HashMap<>();
        Map<Integer, TrialFieldCfg> trainCopyTmp = new HashMap<>();
        for (Object obj : GameData.getConfigs(TrialFieldCfg.class)) {
            TrialFieldCfg cfg = (TrialFieldCfg) obj;
            trainCountTmp.put(cfg.type, cfg.count);
            trainCopyTmp.put(cfg.copyId, cfg);
        }
        trainCount = trainCountTmp;
        trainCopy = trainCopyTmp;


        //活动数据预加载
        Map<Integer, List<ActivityTaskCfg>> ActivityTasksTmp = Maps.newConcurrentMap();
        for (Object obj1 : GameData.getConfigs(ActivityTaskCfg.class)) {
            ActivityTaskCfg conf = (ActivityTaskCfg) obj1;
            List<ActivityTaskCfg> list = ActivityTasksTmp.get(conf.ActivityId);
            if (list == null) {
                list = Lists.newArrayList();
                ActivityTasksTmp.put(conf.ActivityId, list);
            }
            list.add(conf);
        }
        ActivityTasks = ActivityTasksTmp;

        //宠物技能
        Map<Integer, List<Integer>> passiveSkillsTmp = Maps.newHashMap();
        for (Object obj : GameData.getConfigs(PetSkillConfig.class)) {
            PetSkillConfig cfg = (PetSkillConfig) obj;

            List<Integer> skills = passiveSkillsTmp.get(cfg.quality);
            if (skills == null) {
                skills = Lists.newArrayList();
                passiveSkillsTmp.put(cfg.quality, skills);
            }
            skills.add(cfg.id);
        }
        PassiveSkills = passiveSkillsTmp;

        //套装预加载
        Map<Integer, Integer> suitTmp = Maps.newHashMap();
        for (Object obj : GameData.getConfigs(SuitConfig.class)) {
            SuitConfig cfg = (SuitConfig) obj;
            for (int itemId : cfg.equips) {
                suitTmp.put(itemId, cfg.id);
            }
        }
        SuitMap = suitTmp;

        //公会副本怪物
        Map<Integer, List<MonsterRefreshConfig>> monsterMapTmp = new HashMap<>();
        for (Object obj : GameData.getConfigs(GangCopyCfg.class)) {
            GangCopyCfg cfg = (GangCopyCfg) obj;
            List<MonsterRefreshConfig> list = monsterMapTmp.get(cfg.copyId);
            if (list == null) {
                list = new ArrayList<>();
                monsterMapTmp.put(cfg.copyId, list);
            }

            for (Object obj1 : GameData.getConfigs(MonsterRefreshConfig.class)) {
                MonsterRefreshConfig conf = (MonsterRefreshConfig) obj1;
                if (cfg.copyId == conf.copyId) {
                    list.add(conf);
                }
            }
        }
        GangMonsters = monsterMapTmp;

        //公会捐献
        Map<Integer, int[]> donateCfgTmp = new HashMap<>();
        for (int[] arr : ConfigData.globalParam().donateParams) {
            donateCfgTmp.put(arr[4], arr);
        }
        DonateCfg = donateCfgTmp;

        //加载礼包激活码
        Map<String, CdkeyConfig> giftBagMapTmp = new HashMap<>();
        for (Object cfg : GameData.getConfigs(CdkeyConfig.class)) {
            CdkeyConfig conf = (CdkeyConfig) cfg;
            giftBagMapTmp.put(conf.cdkey, conf);
        }
        giftBagMap = giftBagMapTmp;

        Map<Integer, List<String>> firstNameListTemp = Maps.newHashMap();
        List<String> manFirst = Lists.newArrayList();
        List<String> femaleFirst = Lists.newArrayList();
        firstNameListTemp.put(1, manFirst);
        firstNameListTemp.put(2, femaleFirst);
        Map<Integer, List<String>> secondNameListTemp = Maps.newHashMap();
        List<String> manSecond = Lists.newArrayList();
        List<String> femaleSecond = Lists.newArrayList();
        secondNameListTemp.put(1, manSecond);
        secondNameListTemp.put(2, femaleSecond);
        for (Object cfg : GameData.getConfigs(RobotNameCfg.class)) {
            RobotNameCfg conf = (RobotNameCfg) cfg;
            if (conf.manSingle != null) {
                manFirst.add(conf.manSingle);
            }
            if (conf.manDouble != null) {
                manSecond.add(conf.manDouble);
            }

            if (conf.womanSingle != null) {
                femaleFirst.add(conf.womanSingle);
            }
            if (conf.womanDouble != null) {
                femaleSecond.add(conf.womanDouble);
            }
        }
        FirstNameList = firstNameListTemp;
        SecondNameList = secondNameListTemp;
    }
}
