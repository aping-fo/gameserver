package com.game.module.copy;

import com.game.SysConfig;
import com.game.data.*;
import com.game.module.admin.MessageService;
import com.game.module.attach.catchgold.CatchGoldLogic;
import com.game.module.attach.endless.EndlessAttach;
import com.game.module.attach.endless.EndlessLogic;
import com.game.module.attach.experience.ExperienceAttach;
import com.game.module.attach.experience.ExperienceLogic;
import com.game.module.attach.leadaway.LeadAwayLogic;
import com.game.module.attach.treasure.TreasureAttach;
import com.game.module.attach.treasure.TreasureLogic;
import com.game.module.daily.DailyService;
import com.game.module.friend.FriendService;
import com.game.module.gang.GangDungeonService;
import com.game.module.gang.GangService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.group.GroupService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.shop.ShopService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.team.Team;
import com.game.module.team.TeamService;
import com.game.module.traversing.TraversingService;
import com.game.params.CopyReward;
import com.game.params.DropReward;
import com.game.params.Reward;
import com.game.params.RewardList;
import com.game.params.copy.CopyInfo;
import com.game.params.copy.CopyResult;
import com.game.params.copy.CopyVo;
import com.game.params.scene.CMonster;
import com.game.params.scene.SMonsterVo;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private TaskService taskService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private PlayerDao playerDao;
	@Autowired
	private DailyService dailyService;
	@Autowired
	private GangService gangService;
	@Autowired
	private FriendService friendService;
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
	private GangDungeonService gangDungeonService;

	private AtomicInteger uniId = new AtomicInteger(100);
	private Map<Integer, CopyInstance> instances = new ConcurrentHashMap<Integer, CopyInstance>();

	// 获取所有副本信息
	public CopyInfo getCopys(int playerId) {
		PlayerData data = playerService.getPlayerData(playerId);
		List<CopyVo> copys = new ArrayList<CopyVo>();
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
		copyInfo.threeStars = new ArrayList<Integer>(data.getThreeStars());
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
		}
		// 次数
		if (cfg.count > 0) {
			Integer curCount = playerService.getPlayerData(playerId).getCopyTimes().get(copyId);
			if (curCount == null) {
				curCount = 0;
			}
			if (curCount >= cfg.count) {
				return Response.NO_TODAY_TIMES;
			}
		}
		if (cfg.needEnergy > 0) {
			if (player.getEnergy() < cfg.needEnergy) {
				return Response.NO_ENERGY;
			}
		}
		
		if(cfg.type == CopyInstance.TYPE_ENDLESS){
			EndlessAttach attach = endlessLogic.getAttach(playerId);
			if(attach.getChallenge() == 0){
				return Response.NO_TODAY_TIMES;
			}
			if(attach.getClearTime() > 0){
				return Response.ERR_PARAM;
			}
			/*if((attach.getCurrLayer() % endlessLogic.getConfig().sectionLayer == 0 && cfg.difficulty != CopyInstance.HARD)
					||(attach.getCurrLayer() % endlessLogic.getConfig().sectionLayer != 0 && cfg.difficulty == CopyInstance.HARD)){
				return Response.ERR_PARAM;
			}*/
		}else if(cfg.type == CopyInstance.TYPE_TREASURE){
			TreasureAttach treasureAttach = treasureLogic.getAttach(playerId);
			if(treasureAttach.getChallenge() == 0){
				return Response.NO_TODAY_TIMES;
			}
			if(System.currentTimeMillis() - treasureAttach.getLastChallengeTime() < ConfigData.globalParam().treasureDelTime){
				return Response.ERR_PARAM;
			}
		}else if(cfg.type == CopyInstance.TYPE_EXPERIENCE){
			ExperienceAttach experienceAttach = experienceLogic.getAttach(playerId);
			if(experienceAttach.getChallenge() == 0){
				return Response.NO_TODAY_TIMES;
			}
			if(System.currentTimeMillis() - experienceAttach.getLastChallengeTime() < ConfigData.globalParam().extremeEvasionDelTime){
				return Response.ERR_PARAM;
			}
		} else if(cfg.type == CopyInstance.TYPE_LEADAWAY) {

		} else if(cfg.type == CopyInstance.TYPE_GROUP) {

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
				playerService.decCurrency(playerId, Goods.TRAVERSING_ENERGY, cfg.needEnergy, LogConsume.TRAVERSING_COPY, cfg.id);
			}else{
				playerService.decEnergy(playerId, cfg.needEnergy, LogConsume.COPY_ENERGY, copyId);
			}
		}
		if(cfg.type == CopyInstance.TYPE_TRAVERSING){
			Team team = teamService.getTeam(player.getTeamId());
			int leaderId = team.getLeader();
			if(playerId == leaderId){
				traversingService.remvoeMap(playerId, team.getMapId());
			}
		}

		// 掉落
		CopyInstance copy = instances.get(playerService.getPlayer(playerId).getCopyId());
		List<GoodsEntry> items = calculateCopyReward(playerId, copyId, star);

		if(cfg.type == CopyInstance.TYPE_LEADAWAY
				|| cfg.type == CopyInstance.TYPE_GOLD) { //顺手牵羊,金币，奖励
			for(Reward reward : result.rewards){
				items.add(new GoodsEntry(reward.id,reward.count));
			}
		}

		result.rewards = new ArrayList<Reward>();
		// 构造奖励
		if(cfg.type == CopyInstance.TYPE_ENDLESS){
			EndlessAttach attach = endlessLogic.getAttach(playerId);
			EndlessCfg eCfg = endlessLogic.getConfig();
			int multiple = (int)((attach.getCurrLayer() / eCfg.sectionLayer + 1) * eCfg.sectionMultiple);
			
			for (GoodsEntry g : items) {
				g.count *= multiple;
			}
		}else if(cfg.type == CopyInstance.TYPE_TRAVERSING) {
			
			List<Reward> affixReward = traversingService.takeReward(playerId, playerId, copy.getTraverseMap());
			if(affixReward != null){
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

		if(cfg.type==CopyInstance.TYPE_COMMON){
			CopyRank rank = updateCopyRank(playerId, copyId, result.time);
			result.passTime = rank.getPassTime();
			result.name = rank.getName();
		}
		return result;
	}

	public List<GoodsEntry> calculateCopyReward(int playerId, int copyId, int star) {
		Player player = playerService.getPlayer(playerId);
		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
		Map<Integer, Integer> totalRewards = new HashMap<Integer, Integer>();
		// 构造奖励
		List<GoodsEntry> items = new ArrayList<GoodsEntry>();

		if(cfg.type == CopyInstance.TYPE_LEADAWAY ||
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
			int index = RandomUtil.getRandomIndex(cfg.randomRates);
			int id = cfg.randomRewards[index][0];
			int count = cfg.randomRewards[index][1];
			if(ConfigData.getConfig(GoodsConfig.class, id) == null) {
				ServerLogger.warn("goods don't exist id = " + id);
			}
			//int vocation = ConfigData.getConfig(GoodsConfig.class, id).vocation;
			//if (vocation == 0 || vocation == player.getVocation()) { //去掉职业限制
			if (id > 0 && count > 0) {
				addItem(totalRewards, id, count);
			}
			//}
		}
		// 3星奖励
		if(cfg.starRewards!=null){
			int[][]starRewards = cfg.starRewards.get(star);
			if(starRewards!=null){
				for(int i=0;i<starRewards.length;i++){
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
			}
		}

		//声望加成
		PlayerData data = playerService.getPlayerData(playerId);
		boolean bCamp = (cfg.camp != 0 && cfg.camp == data.getActivityCamp());
		if (bCamp) { //代表阵营==当前阵营，加成
			float rate = ConfigData.globalParam().fameAddRate;
			for (GoodsEntry g : items) {
				GoodsConfig conf = ConfigData.getConfig(GoodsConfig.class, g.id);
				if(conf == null) {
					ServerLogger.warn("goods don't exist id = " + g.id);
				}
				if (conf.type == Goods.FAME) {
					g.count = Math.round((1 + rate) * g.count);

					for (int techId : data.getTechnologys()) {
						GangScienceCfg config = ConfigData.getConfig(GangScienceCfg.class, techId);
						if (config.type == 9) { //科技声望加成
							g.count = Math.round(g.count * (1 + config.param / 100.0f));
						}
					}
				}
			}
		}

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
				}
			}
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
		
		if (cfg.count > 0) {
			Integer count = playerData.getCopyTimes().get(copyId);
			if (count == null) {
				count = 0;
			}
			count++;
			playerData.getCopyTimes().put(copyId, count);
		}
		if(cfg.type == CopyInstance.TYPE_ENDLESS){
			endlessLogic.updateLayer(playerId, result);
		}else if(cfg.type == CopyInstance.TYPE_TREASURE){
			treasureLogic.updateCopy(playerId, result);
		}else if(cfg.type == CopyInstance.TYPE_EXPERIENCE){
			experienceLogic.updateCopy(playerId, result);
		}else if(cfg.type == CopyInstance.TYPE_LEADAWAY) {
			leadAwayLogic.updateCopy(playerId,result);
		}else if(cfg.type == CopyInstance.TYPE_GOLD) {
			catchGoldLogic.updateCopy(playerId,result);
		}
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
		taskService.doTask(playerId, Task.FINISH_TRANSIT, copyId, cfg.type, result.star, 1);
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

		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, passId);
		if(cfg.type != CopyInstance.TYPE_LADDER) {
			for (int i = 0; i < cfg.scenes.length; i++) {
				int sceneId = cfg.scenes[i];
				Map<Integer, SMonsterVo> monsters = new ConcurrentHashMap<Integer, SMonsterVo>();
				Map<Integer, MonsterRefreshConfig> _monsters = null;
				if(cfg.type == CopyInstance.TYPE_ENDLESS){
					_monsters = endlessLogic.getSceneMonster(playerId, copyId, i + 1);
				}else {
					_monsters = ConfigData.getSceneMonster(passId, i + 1);

				}
				if(_monsters == null){
					throw new RuntimeException(String.format("can not found the monster, copyid=%d,group=%d", copyId, i + 1));
				}
				for (MonsterRefreshConfig m : _monsters.values()) {
					int monsterId = m.monsterId;
					MonsterConfig monsterCfg = ConfigData.getConfig(MonsterConfig.class, monsterId);
					SMonsterVo vo = new SMonsterVo();
					if (monsterCfg == null) {
						ServerLogger.warn("Err MonsterRefresh:" + m.id);
					}
					if(cfg.type == CopyInstance.TYPE_ENDLESS){
						EndlessCfg eCfg = endlessLogic.getConfig();
						EndlessAttach attach = endlessLogic.getAttach(playerId);
						int fight = Math.round(eCfg.baseData + (eCfg.baseData * (attach.getCurrLayer() - 1) * eCfg.growRatio
								+ eCfg.baseData * (attach.getCurrLayer() / eCfg.sectionLayer) * eCfg.sectionMultiple* eCfg.scetionRatio));
						vo.curHp = vo.hp = Math.round(fight * 3.32f);
						vo.attack = Math.round(fight * 0.18f);
						vo.crit = Math.round(fight * 0.13f);
						vo.defense = Math.round(fight * 0.05f);
						vo.symptom = Math.round(fight * 0.1f);
						vo.fu = Math.round(fight * 0.1f);
						//vo.curHp = vo.hp = (int)(attach.getCurrLayer() * eCfg.growRatio * eCfg.baseData * eCfg.hp * (attach.getCurrLayer() / eCfg.sectionLayer + 1) * eCfg.scetionRatio);
					}else{
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
				instance.getMonsters().put(sceneId, monsters);
			}
		}
		int instanceId = uniId.incrementAndGet();
		if(cfg.type == CopyInstance.TYPE_GROUP) {
			instanceId = groupService.onEnterBattle(playerId,copyId);
			ServerLogger.warn("------------" + instanceId);
		} else if(cfg.type == CopyInstance.TYPE_TRAVERSING){
			instanceId = teamService.onEnterBattle(playerId);
		}
		Player player = playerService.getPlayer(playerId);
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
			if(copyIns != null && copyIns.getMembers().decrementAndGet() == 0){				
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
		dropReward.x = m.x;
		dropReward.z = m.z;

		

		Player player = playerService.getPlayer(playerId);

		CopyInstance copy = getCopyInstance(player.getCopyId());
		if (copy == null) {
			return dropReward;
		}
		SMonsterVo monster = copy.getMonsters().get(player.getSceneId()).remove(id);

		if (monster == null) {
			return dropReward;
		}
		MonsterConfig monsterCfg = GameData.getConfig(MonsterConfig.class, monster.monsterId);
		taskService.doTask(playerId, Task.FINISH_KILL, monsterCfg.type, monster.monsterId, 1);
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
			if(goodsCfg == null) {
				ServerLogger.warn("goods don't exist id = " + reward.id);
			}
			if (goodsCfg.type != Goods.BOTTLE) {
				// 加入缓存
				addItem(copy.getDrops(), reward.id, reward.count);
			}
		}

		int copyId = copy.getCopyId();
		PlayerData playerData = playerService.getPlayerData(playerId);
		Copy myCopy = playerData.getCopys().get(copyId);
		if (myCopy == null) {
			myCopy = new Copy();
			playerData.getCopys().put(copyId, myCopy);
		}
		return dropReward;
	}

	// 检查副本结果,简单防一下时间
	public boolean checkCopyResult(int playerId,CopyInstance copy, CopyResult result) {
		if (SysConfig.debug) {
			return true;
		}
		long now = System.currentTimeMillis();
		long pass = (now - copy.getCreateTime()) / TimeUtil.ONE_SECOND;
		if (pass <= 1) {
			ServerLogger.warn("Err Copy", result.id, result.star, result.time, result.combo, result.hp,
					copy.getCopyId());
			return false;
		}
		result.time = (int) pass;
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
		for(int[] item:copyCfg.reviveCost){
			cost.add(new GoodsEntry(item[0], item[1]*count));
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
		result.reward = new ArrayList<RewardList>();

		CopyConfig cfg = ConfigData.getConfig(CopyConfig.class, copyId);
		Player player = playerService.getPlayer(playerId);
		PlayerData playerData = playerService.getPlayerData(playerId);
		for (int i = 0; i < times; i++) {
			// 检查次数
			if (cfg.count > 0) {// 普通副本
				Integer count = playerData.getCopyTimes().get(copyId);
				if (count == null) {
					count = 0;
				}
				if (count >= cfg.count) {
					result.code = Response.NO_TODAY_TIMES;
					refreshCopyInfo(playerId, copyId, playerData);
					return result;
				}
			}
			
			if (cfg.needEnergy>0 && player.getEnergy() < cfg.needEnergy) {
				result.code = Response.NO_ENERGY;
				return result;
			}
			
			if(goodsService.decConsume(playerId, ConfigData.globalParam().sweepNeedGoods, LogConsume.SWEEP_COPY) > 0){
				result.code = Response.NO_MATERIAL;
				return result;
			}
			// 活动副本扣除体力
			if (cfg.needEnergy>0) {
				if (!playerService.decEnergy(playerId, cfg.needEnergy, LogConsume.COPY_ENERGY, copyId)) {
					result.code = Response.NO_ENERGY;
					refreshCopyInfo(playerId, copyId, playerData);
					return result;
				}
			}
			
			boolean show = shopService.triggerMysteryShop(playerId, copyId, null);
			if(show){
				result.showMystery = true;
			}

			RewardList list = new RewardList();
			list.rewards = swipeCopyInner(playerId, copyId);
			result.reward.add(list);

			// 更新副本次数
			if (cfg.count >0) {
				Integer count = playerData.getCopyTimes().get(copyId);
				if (count == null) {
					count = 0;
				}
				count++;
				playerData.getCopyTimes().put(copyId, count);
			}
		}

		refreshCopyInfo(playerId, copyId, playerData);
		return result;
	}
	

	// 扫荡副本
	public List<Reward> swipeCopyInner(int playerId, int copyId) {
		createCopyInstance(playerId, copyId, copyId);
		int star = 1;
		Copy copy = playerService.getPlayerData(playerId).getCopys().get(copyId);
		CopyConfig cfg = GameData.getConfig(CopyConfig.class, copyId);
		if(copy != null){
			star = copy.getState();
		}else if(cfg.type != CopyInstance.TYPE_TREASURE && cfg.type != CopyInstance.TYPE_EXPERIENCE
				&& cfg.type != CopyInstance.TYPE_LEADAWAY){
			return null;
		}
		List<GoodsEntry> copyRewards = calculateCopyReward(playerId, copyId, star);

		goodsService.addRewards(playerId, copyRewards, LogConsume.COPY_REWARD, copyId);
		
		List<Reward> rewards= new ArrayList<Reward>(copyRewards.size());
		for (GoodsEntry item : copyRewards) {
			Reward r = new Reward();
			r.id = item.id;
			r.count = item.count;
			rewards.add(r);
		}
		taskService.doTask(playerId, Task.FINISH_TRANSIT, copyId, cfg.type, star, 1);
		removeCopy(playerId);
		return rewards;
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
	public int get3starReward(int playerId, int id) {
		// 有无领取过
		PlayerData data = playerService.getPlayerData(playerId);
		if (data.getThreeStars().contains(id)) {
			return Response.SYS_ERR;
		}
		// 验证条件
		// 设置已经领取
		data.getThreeStars().add(id);
		// 加物品
		ThreeStarRewardCfg cfg = ConfigData.getConfig(ThreeStarRewardCfg.class, id);
		// 更新
		goodsService.addRewards(playerId, cfg.rewards, LogConsume.THREE_STAR);
		CopyInfo info = getCopyInfo(playerId);
		SessionManager.getInstance().sendMsg(CopyExtension.CMD_REFRESH, info, playerId);
		return Response.SUCCESS;
	}
	
	
}
