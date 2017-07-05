package com.game.util;

import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.game.data.ChargeConfig;
import com.game.data.CopyConfig;
import com.game.data.GlobalConfig;
import com.game.data.GoodsConfig;
import com.game.data.MonsterRefreshConfig;
import com.game.data.RewardMailCfg;
import com.game.data.ShopCfg;
import com.game.data.SkillCardComposeCfg;
import com.game.data.TaskConfig;
import com.game.module.copy.CopyInstance;
import com.game.module.shop.ShopService;
import com.game.module.task.Task;
import com.server.util.GameData;

/**
 * 一些不是唯一key的配置，可以在这里定义一些辅助函数操作
 */
public class ConfigData {

	// [场景id_难度:怪]
	private static Map<String, Map<Integer, MonsterRefreshConfig>> monsterRefreshs;

	private static GlobalConfig globalCfg;

	// 副本，按章节，难度归类
	private static Map<Integer, Map<Integer, List<Integer>>> copys;
	// 无尽漩涡副本
	public static int[] endlessCopys;

	// 充值配置
	private static List<ChargeConfig> charges;

	// 所有任务
	private static Map<Integer, List<Integer>> tasks = new ConcurrentHashMap<Integer, List<Integer>>();

	// 技能卡合成
	private static Map<String, List<Integer>> skillCardRates = new ConcurrentHashMap<>();
	private static Map<String, List<Integer>> skillCardIds = new ConcurrentHashMap<>();

	public static List<Integer> getSkillCardIds(int type, int quality) {
		return skillCardIds.get(String.format("%d_%d", type, quality));
	}

	public static List<Integer> getSkillCardRates(int type, int quality) {
		return new ArrayList<Integer>(skillCardRates.get(String.format("%d_%d",
				type, quality)));
	}

	public static Map<Integer, List<Integer>> RefreshIds = new ConcurrentHashMap<Integer, List<Integer>>();
	public static Map<Integer, List<Integer>> RefreshRates = new ConcurrentHashMap<Integer, List<Integer>>();

	public static Set<Integer> copy4Mystery = new ConcurrentSet<Integer>();

	public static Map<Integer, List<RewardMailCfg>> rewardMails = new ConcurrentHashMap<Integer, List<RewardMailCfg>>();

	// 获取充值配置
	public static List<ChargeConfig> getCharges() {
		return charges;
	}

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
		monsterRefreshs = new HashMap<String, Map<Integer, MonsterRefreshConfig>>();
		for (Object monster : GameData.getConfigs(MonsterRefreshConfig.class)) {
			MonsterRefreshConfig m = (MonsterRefreshConfig) monster;
			String key = String.format("%d_%d", m.copyId, m.group);
			Map<Integer, MonsterRefreshConfig> monsters = monsterRefreshs
					.get(key);
			if (monsters == null) {
				monsters = new HashMap<Integer, MonsterRefreshConfig>();
				monsterRefreshs.put(key, monsters);
			}
			monsters.put(m.id, m);
		}

		// 副本章节难度归类
		copys = new HashMap<Integer, Map<Integer, List<Integer>>>();
		endlessCopys = new int[2];
		for (Object cfg : GameData.getConfigs(CopyConfig.class)) {
			CopyConfig copy = (CopyConfig) cfg;

			if (copy.type == CopyInstance.TYPE_COMMON) {
				Map<Integer, List<Integer>> chapterCopys = copys
						.get(copy.chapterId);
				if (chapterCopys == null) {
					chapterCopys = new HashMap<Integer, List<Integer>>();
					copys.put(copy.chapterId, chapterCopys);
				}
				List<Integer> difficultCopys = chapterCopys
						.get(copy.difficulty);
				if (difficultCopys == null) {
					difficultCopys = new ArrayList<Integer>();
					chapterCopys.put(copy.difficulty, difficultCopys);
				}
				if (!difficultCopys.contains(copy.id)) {
					difficultCopys.add(copy.id);
				}
			} else if (copy.type == CopyInstance.TYPE_ENDLESS) {
				endlessCopys[copy.difficulty - 1] = copy.id;
			}

		}

		// 每日任务
		for (Object cfg : GameData.getConfigs(TaskConfig.class)) {
			TaskConfig task = (TaskConfig) cfg;
			List<Integer> list = tasks.get(task.taskType);
			if(list == null){
				list = new ArrayList<Integer>();
				tasks.put(task.taskType, list);
			}
			list.add(task.id);
		}

		// 充值排序
		charges = new ArrayList<ChargeConfig>();
		for (Object cfg : GameData.getConfigs(ChargeConfig.class)) {
			ChargeConfig charge = (ChargeConfig) cfg;
			charges.add(charge);
		}
		Collections.sort(charges, new Comparator<ChargeConfig>() {
			@Override
			public int compare(ChargeConfig o1, ChargeConfig o2) {
				if (o1.rmb != o2.rmb) {
					return (int) Math.ceil(o1.rmb - o2.rmb);
				}
				return o1.type - o2.type;
			}
		});

		// 设置一下物品的最大叠加数量
		for (Object cfg : GameData.getConfigs(GoodsConfig.class)) {
			GoodsConfig g = (GoodsConfig) cfg;
			if (g.maxStack >= 9) {
				g.maxStack = 9999;
			}
		}

		// 技能卡合成
		for (Object cfg : GameData.getConfigs(SkillCardComposeCfg.class)) {
			SkillCardComposeCfg g = (SkillCardComposeCfg) cfg;
			String key = String.format("%d_%d", g.type, g.quality);
			List<Integer> ids = skillCardIds.get(key);
			if (ids == null) {
				ids = new ArrayList<Integer>();
				skillCardIds.put(key, ids);
			}
			ids.add(g.id);
			List<Integer> rates = skillCardRates.get(key);
			if (rates == null) {
				rates = new ArrayList<Integer>();
				skillCardRates.put(key, rates);
			}
			rates.add(g.rate);
		}

		// 刷新商店
		for (Object cfg : GameData.getConfigs(ShopCfg.class)) {
			ShopCfg g = (ShopCfg) cfg;
			if (g.tab != ShopService.REFRESH) {
				continue;
			}
			List<Integer> ids = RefreshIds.get(g.shopType);
			if (ids == null) {
				ids = new ArrayList<Integer>();
				RefreshIds.put(g.shopType, ids);
			}
			List<Integer> rates = RefreshRates.get(g.shopType);
			if (rates == null) {
				rates = new ArrayList<Integer>();
				RefreshRates.put(g.shopType, rates);
			}
			ids.add(g.id);
			rates.add(g.refreshRate);
		}

		// 神秘商店的特殊副本
		for (int id : globalCfg.mysterySpecialCopyIds) {
			copy4Mystery.add(id);
		}

		rewardMails.clear();
		for (Object obj : GameData.getConfigs(RewardMailCfg.class)) {
			RewardMailCfg cfg = (RewardMailCfg) obj;
			List<RewardMailCfg> list = rewardMails.get(cfg.group);
			if (list == null) {
				list = new ArrayList<RewardMailCfg>();
				rewardMails.put(cfg.group, list);
			}
			list.add(cfg);
		}
	}

}
