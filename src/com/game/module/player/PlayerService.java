package com.game.module.player;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.game.data.*;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.group.GroupService;
import com.game.module.pet.PetService;
import com.game.module.team.TeamService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.IntParam;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.admin.MessageService;
import com.game.module.daily.DailyService;
import com.game.module.fashion.FashionService;
import com.game.module.gang.Gang;
import com.game.module.gang.GangService;
import com.game.module.goods.EquipService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailDao;
import com.game.module.mail.MailService;
import com.game.module.scene.Scene;
import com.game.module.serial.SerialDataService;
import com.game.module.skill.SkillCard;
import com.game.module.skill.SkillService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.vip.VipService;
import com.game.params.Int2Param;
import com.game.params.ListParam;
import com.game.params.player.CRegVo;
import com.game.params.player.PlayerVo;
import com.game.util.CompressUtil;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.game.util.JsonUtils;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import com.server.validate.AntiCheatService;

@Service
public class PlayerService implements InitHandler {

	@Autowired
	private PlayerDao playerDao;
	@Autowired
	private PlayerCalculator playerCalculator;
	@Autowired
	private TaskService taskService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private DailyService dailyService;
	@Autowired
	private GangService gangService;
	@Autowired
	private SerialDataService serialDataService;
	@Autowired
	private VipService vipService;
	@Autowired
	private EquipService equipService;
	@Autowired
	private MailDao mailDao;
	@Autowired
	private MailService mailService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private SkillService skillService;
	@Autowired
	private FashionService fashionService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private PetService petService;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private TitleService titleService;

	private static volatile int maxPlayerId = 0;

	private volatile Map<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();
	private volatile Map<Integer, PlayerData> playerDatas = new ConcurrentHashMap<Integer, PlayerData>();
	private volatile Map<String, Integer> nameCaches = new ConcurrentHashMap<String, Integer>();

	public static final String ROBOT = "sys";

	@Override
	public void handleInit() {
		Integer curMaxPlayerId = playerDao.selectMaxPlayerId();
		if (curMaxPlayerId == null) {
			curMaxPlayerId = 0;
		}
		maxPlayerId = (curMaxPlayerId / 1000);
	}

	// 获取一个新的UserId
	public synchronized int getNextPlayerId() {
		maxPlayerId++;
		return maxPlayerId * 1000 + SysConfig.serverId;
	}

	// 获取Player对象
	public Player getPlayer(int playerId) {
		Player player = players.get(playerId);
		if (player == null) {
			player = playerDao.select(playerId);
			if (player != null) {
				players.put(playerId, player);
			}
		}
		return player;
	}

	public void update(Player player) {
		playerDao.update(player);
	}
	
	public Collection<PlayerData> getAllPlayerData(){
		return playerDatas.values();
	}

	// 通过角色名字从数据库里面查询id
	public int getPlayerIdByName(String name) {
		Integer id = nameCaches.get(name);
		if(id != null){
			return id;
		}
		id = playerDao.selectIdByName(name);
		return id == null ? 0 : id;
	}

	// 获得账号下的所有角色
	public List<Player> getPlayersByAccName(String accName) {
		return playerDao.selectRoleList(accName);
	}

	// 增加新用户
	public Player addNewPlayer(String name, int sex, int vocation, String accName, String channel) {
		// 初始属性
		final int playerId = getNextPlayerId();
		final Player player = new Player();
		player.setPlayerId(playerId);
		player.setName(name);
		player.setAccName(accName);
		player.setSex(sex);
		player.setLev(1);
		player.setVocation(vocation);
		player.setRegTime(new Date());
		player.setServerId(SysConfig.serverId);
		player.setEnergyTime(System.currentTimeMillis());
		player.setRefresh(false);
		player.setChannel(channel);

		GlobalConfig globalParam = ConfigData.globalParam();
		player.setEnergy(globalParam.maxEnergy);

		// 出身场景
		player.setSceneId(globalParam.firstScene);
		player.setX(globalParam.defaultPos[0]);
		player.setY(globalParam.defaultPos[1]);
		player.setZ(globalParam.defaultPos[2]);

		// 新增用户
		players.put(playerId, player);
		// 数据库插入失败
		try {
			playerDao.insert(player);
		} catch (Exception e) {
			ServerLogger.err(e, "insert player err!");
			return null;
		}
		// 初始化用户数据
		PlayerData playerData = initPlayerData(playerId, false);
		// 初始化时装和武器
		int fashionId = globalParam.fashionId[player.getVocation() - 1];
		player.setFashionId(fashionId);
		playerData.getFashions().add(fashionId);

		int weaponId = globalParam.weaponId[player.getVocation() - 1];
		player.setWeaponId(weaponId);
		playerData.getFashions().add(weaponId);

		//头部
		int headId = globalParam.headId[player.getVocation() - 1];
		playerData.setCurHead(headId);
		playerData.getFashions().add(headId);
		playerData.setGroupTimes(ConfigData.globalParam().groupTimes);
		//初始化技能
		int[] skills = globalParam.playerDefaultSkill[player.getVocation()-1];
		for(int skill:skills){
			playerData.getSkills().add(skill);
			playerData.getCurSkills().add(skill);
	
		}

		for(int[] cardArr : globalParam.newbieskillCard) {
			addSkillCard(playerId,cardArr[0],cardArr[1]);
		}

		for(Object object : ConfigData.getConfigs(ModuleOpenCfg.class)) {
			ModuleOpenCfg cfg = (ModuleOpenCfg)object;
			if(cfg.openType == 1) {
				playerData.getModules().add(cfg.id);
			}
		}

		// 背包空格
		playerData.setBlankGrids(globalParam.bagSize);
		// 任务
		taskService.initTask(playerId);
		goodsService.initBag(playerId);
		titleService.doInit(playerId); //称号初始化
		if(globalParam.GuideEquip != null) {
			for (int[] itemArr : globalParam.GuideEquip) {
				if (vocation == itemArr[0]) {
					goodsService.addGoodsToBag(playerId, itemArr[1], 1, LogConsume.BAG_INIT);
					break;
				}
			}
		}
		petService.initBag(playerId);
		for(int[] itemArr : globalParam.InitPets) {
			petService.addPetMaterial(playerId,itemArr[0],itemArr[1],false);
		}

		// 计算属性
		playerCalculator.calculate(player);

		// 保存数据
		Context.getThreadService().execute(new Runnable() {
			@Override
			public void run() {
				updatePlayerData(playerId);
				update(player);
			}
		});
		return player;
	}

	public boolean checkVersion(int version) {
		if (SysConfig.checkVersion) {
			return (version == ConfigData.globalParam().version);
		} else {
			return true;
		}
	}

	// 验证注册的参数
	public boolean checkRegParam(CRegVo vo) {
		if (vo.vocation < 1 || vo.vocation > 3) {
			return false;
		}
		if (vo.sex < 1 || vo.sex > 2) {
			return false;
		}
		if (vo.name.length() < 2 || vo.name.length() > 20) {
			return false;
		}
		return true;
	}

	// 验证平台登陆
	public int auth() {
		return 0;
	}

	// 处理第一次登录
	public void handleFirstLogin(final int playerId) {
		Context.getThreadService().execute(new Runnable() {
			@Override
			public void run() {
				// 增加初始装备
				Player player = getPlayer(playerId);
				int[] equips = ConfigData.globalParam().initEquips[player.getVocation()-1];
				if (equips != null) {
					for (int equipId : equips) {
						Goods equip = PlayerService.this.goodsService.addNewGoods(playerId, equipId, 1, Goods.EQUIP);
						goodsService.addGoods(playerId,equip);
					}
					
					player.setRefresh(false);
					playerCalculator.calculate(player);
				}
				
				// 新手邮件
				List<GoodsEntry> newbieRewards = new ArrayList<GoodsEntry>();
				int[][] newbieMailReward = ConfigData.globalParam().newbieMailReward;
				if (newbieMailReward != null) {
					for (int i = 0; i < newbieMailReward.length; i++) {
						newbieRewards.add(new GoodsEntry(newbieMailReward[i][0], newbieMailReward[i][1]));
					}
				}
				/*Context.getTimerService().scheduleDelay(new Runnable() {
					@Override
					public void run() {*/
						//goodsService.addRewards(playerId, newbieRewards, LogConsume.GM);
				String mailTitle = ConfigData.getConfig(ErrCode.class, Response.WELCOME_MAIL_TITLE).tips;
				String mailContent = ConfigData.getConfig(ErrCode.class, Response.WELCOME_MAIL_CONTENT).tips;
				mailService.sendSysMail(mailTitle, mailContent, newbieRewards, playerId, LogConsume.GM);
					/*}
				}, 10, TimeUnit.SECONDS);*/

			}
		});
	}

	// 处理登录数据
	public void handleLogin(int playerId) {
		Player player = getPlayer(playerId);

		SceneConfig scene = ConfigData.getConfig(SceneConfig.class, player.getSceneId());
		if (scene != null && scene.type != Scene.CITY && player.getLastSceneId() > 0) {
			player.setSceneId(player.getLastSceneId());
			float[] pos = player.getLastPos();
			player.setX(pos[0]);
			player.setY(pos[1]);
			player.setZ(pos[2]);
		}

		player.setLastSaveTime(System.currentTimeMillis());

		// 每日次数
		PlayerData data = getPlayerData(playerId);

		if (!dailyService.isSameDate(data.getDailyTime())) {
			dailyService.resetDailyData(data);
		}
		
		if(!dailyService.isSameWeek(data.getWeeklyTime())){
			dailyService.resetWeeklyData(data);
		}
		fashionService.checkRemoveTimeoutFashions(playerId, true);
		// 刷新体力
		refreshEnergy(player);
		//刷新活力
		refreshTraversingEnergy(player);
		//更新货币
		updateCurrencyToClient(playerId);
		//活动检测
		activityService.onLogin(playerId);
		titleService.onLogin(playerId);
		nameCaches.putIfAbsent(player.getName(), player.getPlayerId());
	}

	public PlayerVo toSLoginVo(int playerId) {
		PlayerVo vo = new PlayerVo();
		Player player = getPlayer(playerId);
		if(player == null){
			vo.code = Response.ERR_PARAM;
			return vo;
		}
		if (player.getSceneId() == 0) {
			player.setSceneId(ConfigData.globalParam().firstScene);
		}
		PlayerData data = getPlayerData(playerId);

		vo.chargeDiamond = player.getChargeDiamond();
		vo.coin = player.getCoin();
		vo.diamond = player.getDiamond();
		vo.energy = player.getEnergy();
		vo.exp = player.getExp();
		vo.hp = player.getHp();
		vo.curHp = player.getHp();
		vo.lev = player.getLev();
		vo.crit = player.getCrit();
		vo.regTime = player.getRegTime().getTime();

		vo.name = player.getName();
		vo.playerId = player.getPlayerId();
		vo.sceneId = player.getSceneId();
		vo.serverId = player.getServerId();
		vo.sex = player.getSex();
		vo.x = player.getX();
		vo.y = player.getY();
		vo.vip = player.getVip();
		vo.vocation = player.getVocation();
		vo.fashionId = player.getFashionId();
		vo.weapon = player.getWeaponId();
		vo.fight = player.getFight();
		vo.gangId = player.getGangId();
		vo.head = data.getCurHead();
		vo.signDay = data.getSign();
		vo.signFlag = data.getSignFlag();

		if(player.getGangId() > 0){				
			Gang gang = gangService.getGang(player.getGangId());
			if(gang == null){
				player.setGangId(0);
				vo.gangId = 0;
			}
		}

		vo.fashions = new ArrayList<>();
		vo.fashions.addAll(data.getFashions());
		vo.serialNum = AntiCheatService.getInstance().getSerialNum(playerId).get();
		vo.key = AntiCheatService.getInstance().getPrivateKey(playerId);
		vo.guideId = data.getGuideId();
		vo.title = player.getTitle();
		vo.openDays = SysConfig.getOpenDays();

		vo.attack = player.getAttack();
		vo.defense = player.getDefense();
		vo.fu = player.getFu();
		vo.symptom = player.getSymptom();
		
		vo.curSkills = new ArrayList<>(data.getCurSkills());
		vo.curCards = new ArrayList<>(data.getCurrCard().size());
		for(int card:data.getCurrCard()){
			if(card==0){
				vo.curCards.add(0);
			}else{
				SkillCard skillCard = data.getSkillCards().get(card);
				if(skillCard != null) {
					vo.curCards.add(skillCard.getCardId());
				}
			}
		}

		//开启功能列表
		vo.modules = new ArrayList<>();
		for(int moduleId : data.getModules()) {
			vo.modules.add(moduleId);
		}

		vo.newHandleSteps = Lists.newArrayList();
		for(int step : data.getGuideSteps()) {
			vo.newHandleSteps.add(step);
		}
		return vo;
	}

	// 加钻石
	public boolean addDiamond(int playerId, int add, LogConsume actionType, Object... params) {
		if (add <= 0) {
			return false;
		}
		Player player = getPlayer(playerId);
		synchronized (player) {
			// 充值
			player.setDiamond(player.getDiamond() + add);
			update(player);
		}
		// 通知前端
		updateAttrsToClient(playerId, Player.DIAMOND,player.getDiamond());
		// 记录日志
		if (actionType == null) {
			actionType = LogConsume.GM;
		}

		Context.getLoggerService().logDiamond(playerId, add, actionType.actionId, true, params);
		taskService.doTask(playerId, Task.FINISH_CURRENCY, Goods.DIAMOND, add);
		return true;
	}

	// 减钻石
	public boolean decDiamond(int playerId, int dec, LogConsume actionType, Object... params) {
		final Player player = getPlayer(playerId);
		if (dec <= 0 || (player.getDiamond()) < dec) {
			return false;
		}
		synchronized (player) {
			player.setDiamond(player.getDiamond() - dec);
			player.setTotalDiamond(player.getTotalDiamond() + dec);
			update(player);
		}

		// 通知前端
		updateAttrsToClient(playerId, Player.DIAMOND,player.getDiamond());
		// 记录日志
		if (actionType == null) {
			actionType = LogConsume.GM;
		}
		Context.getLoggerService().logDiamond(playerId, dec, actionType.actionId, false, params);
		taskService.doTask(playerId, Task.FINISH_CONSUME, Goods.DIAMOND, dec);
		return true;
	}

	// 减金币
	public boolean decCoin(int playerId, int dec, LogConsume actionType, Object... params) {
		Player player = getPlayer(playerId);
		synchronized (player) {
			if (dec <= 0 || player.getCoin() < dec) {
				return false;
			}
			player.setCoin(player.getCoin() - dec);
			player.setTotalCoin(player.getTotalCoin() + dec);
		}

		// 通知前端
		updateAttrsToClient(playerId, Player.COIN,player.getCoin());
		// 记录日志
		Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), false, dec, actionType,
				Goods.COIN, Goods.CURRENCY, params);
		taskService.doTask(playerId, Task.FINISH_CONSUME, Goods.COIN, dec);
		return true;
	}

	// 加金币
	public boolean addCoin(int playerId, int add, LogConsume actionType, Object... params) {
		if (add <= 0) {
			return false;
		}
		Player player = getPlayer(playerId);
		synchronized (player) {
			player.setCoin(player.getCoin() + add);
			player.setTotalCoin(player.getTotalCoin() + add);
		}

		// 通知前端
		updateAttrsToClient(playerId, Player.COIN,player.getCoin());
		// 记录日志
		Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true, add, actionType,
				Goods.COIN,Goods.CURRENCY, params);
		taskService.doTask(playerId, Task.FINISH_CURRENCY, Goods.COIN, add);
		return true;
	}

	// dec Energy
	public boolean decEnergy(int playerId, int dec, LogConsume actionType, Object... params) {
		Player player = getPlayer(playerId);
		synchronized (player) {
			if (dec <= 0 || player.getEnergy() < dec) {
				return false;
			}
			player.setEnergy(player.getEnergy() - dec);
		}

		// 通知前端
		refreshPlayerToClient(playerId);
		// 记录日志
		Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), false, dec, actionType,
				0,Goods.ENERGY, params);
		return true;
	}
	
	// add energy
	public boolean addEnergy(int playerId, int add, LogConsume actionType, Object... params) {
		if (add <= 0) {
			return false;
		}
		Player player = getPlayer(playerId);
		synchronized (player) {
			player.setEnergy(player.getEnergy() + add);
		}
		// 通知前端
		updateAttrsToClient(playerId, Player.ENERGY,player.getEnergy());
		// 记录日志
		Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true, add, actionType,0,
				Goods.COIN, params);
		return true;
	}

	// 增加vip经验值
	public boolean addVipExp(int playerId, int add) {
		if (add <= 0) {
			return false;
		}
		Player player = getPlayer(playerId);
		VIPConfig oldCfg = ConfigData.getConfig(VIPConfig.class,player.getVip());
		synchronized (player) {
			player.setChargeDiamond(player.getChargeDiamond() + add);
			// 检查VIP是否升级
			int maxVip = ConfigData.getConfigs(VIPConfig.class).size() - 1;
			for (int i = maxVip; i >= 0; i--) {
				VIPConfig cfg = ConfigData.getConfig(VIPConfig.class, i);
				if (cfg == null || player.getChargeDiamond() < cfg.charge) {
					continue;
				}
				int newVIP = i;
				if (player.getVip() != newVIP) {
					player.setVip(newVIP);
					//VIP称号
					titleService.complete(playerId, TitleConsts.VIP,newVIP, ActivityConsts.UpdateType.T_VALUE);
				}
				break;
			}
		}
		VIPConfig newCfg = ConfigData.getConfig(VIPConfig.class,player.getVip());
		PlayerData data = getPlayerData(playerId);
		PlayerCurrency currency = data.getCurrency();
		currency.add(Goods.TRAVERSING_ENERGY, newCfg.traveringEnergy - oldCfg.traveringEnergy);
		long curEnergy = currency.get(Goods.TRAVERSING_ENERGY);
		updateCurrencyToClient(playerId, Goods.TRAVERSING_ENERGY,(int)curEnergy);
		// 更新客户端
		updateAttrsToClient(playerId, Player.VIP_EXP,player.getChargeDiamond(),Player.VIP_LEV,player.getVip());
		// 充值后续处理
		update(player);
		return true;
	}

	/**
	 * 增加玩家的经验
	 */
	public void addExp(int playerId, int exp, LogConsume actionType, Object... params) {
		if (exp <= 0) {
			return;
		}

		Player player = getPlayer(playerId);

		player.setExp(player.getExp() + exp);

		// 判断是否要升级或修改其它属性，同时判断是否会触发其它系统的任务或事件
		if (checkUpgrade(player)) {
			playerCalculator.calculate(player);
			// 升级触发相关事件，比如任务等
			if (player.getLev() >= 15 && player.getLev() <= 20) {
				// 广播的消息
				SessionManager.getInstance().setPlayerLev(playerId, player.getLev());
			}
			taskService.checkTaskWhenLevUp(playerId);

			activityService.completeActivityTask(playerId,
					ActivityConsts.ActivityTaskCondType.T_GROW_FUND,player.getLev(), ActivityConsts.UpdateType.T_VALUE,true);
			activityService.completeActivityTask(playerId,
					ActivityConsts.ActivityTaskCondType.T_LEVEL_UP,player.getLev(), ActivityConsts.UpdateType.T_VALUE,true);
			//等级称号
			titleService.complete(playerId, TitleConsts.LEVEL,player.getLev(), ActivityConsts.UpdateType.T_VALUE);
		} 
		// 发送到前端
		updateAttrsToClient(playerId, Player.EXP,player.getExp(),Player.LEV,player.getLev());
		groupService.updateAttr(playerId);
		teamService.updateAttr(playerId);
		Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true, exp, actionType,
				0,Goods.EXP, params);
	}

	// 检验是否升级
	private boolean checkUpgrade(Player player) {

		int curLev = player.getLev();
		int leftExp = player.getExp();
		boolean upgrade = false;
		while (true) {
			PlayerUpgradeCfg exp = GameData.getConfig(PlayerUpgradeCfg.class, curLev);
			if (exp == null || leftExp < exp.exp) {
				break;
			}
			upgrade = true;
			curLev++;
			leftExp -= exp.exp;
		}
		if (GameData.getConfig(PlayerUpgradeCfg.class, curLev) == null) {
			curLev--;
		}

		player.setLev(curLev);
		player.setExp(leftExp);

		return upgrade;
	}

	// 获取玩家数据
	public PlayerData getPlayerData(int playerId) {
		PlayerData data = playerDatas.get(playerId);
		if (data != null) {
			return data;
		}
		byte[] dbData = playerDao.selectPlayerData(playerId);
		if (dbData != null) {
			String jsonStr = new String(CompressUtil.decompressBytes(dbData), Charset.forName("utf-8"));
			data = JsonUtils.string2Object(jsonStr, PlayerData.class);
		}
		if (data == null) {
			ServerLogger.warn("player data deserial err:", playerId);
		} else {
			playerDatas.put(playerId, data);
		}

		return data;
	}

	// 更新玩家数据
	public void updatePlayerData(int playerId) {
		PlayerData data = getPlayerData(playerId);
		String jsonStr = JsonUtils.object2String(data);
		if (jsonStr == null) {
			ServerLogger.warn("player data serial err:", playerId);
			return;
		}
		byte[] byteData = jsonStr.getBytes(Charset.forName("utf-8"));
		byteData = CompressUtil.compressBytes(byteData);
		playerDao.updatePlayerData(playerId, byteData);
	}

	// 初始化玩家数据
	public PlayerData initPlayerData(final int playerId, boolean flush) {
		PlayerData data = new PlayerData();
		data.setPlayerId(playerId);
		playerDatas.put(playerId, data);

		String jsonStr = JsonUtils.object2String(data);
		if (jsonStr == null) {
			ServerLogger.warn("player data serial err:", playerId);
			return null;
		}
		byte[] byteData = jsonStr.getBytes(Charset.forName("utf-8"));
		final byte[] zipData = CompressUtil.compressBytes(byteData);
		if (flush) {
			playerDao.insertPlayerData(playerId, zipData);
		} else {
			Context.getThreadService().execute(new Runnable() {
				@Override
				public void run() {
					playerDao.insertPlayerData(playerId, zipData);
				}
			});
		}
		return data;
	}

	public void saveData(final int playerId) {
		final Player player = getPlayer(playerId);
		long timePass = System.currentTimeMillis() - player.getLastSaveTime();
		if (timePass >= TimeUtil.ONE_MIN * 3) {
			Context.getThreadService().execute(new Runnable() {
				@Override
				public void run() {
					refreshEnergy(player);
					refreshTraversingEnergy(player);
					fashionService.checkRemoveTimeoutFashions(playerId, true);
				}
			});
		}
		if (timePass >= TimeUtil.ONE_MIN * 5) {
			player.setLastSaveTime(System.currentTimeMillis());
			Context.getThreadService().execute(new Runnable() {
				@Override
				public void run() {
					update(player);
					updatePlayerData(playerId);
					taskService.updateTask(playerId);
					goodsService.updateBag(playerId);
					petService.updateBag(playerId);
				}
			});
		}
	}

	// 刷新体力
	public void refreshEnergy(Player player) {
		synchronized (player) {
			//VIPConfig vipCfg = ConfigData.getConfig(VIPConfig.class, player.getVip());
			int maxEnergy = ConfigData.globalParam().maxEnergy;
			if (player.getGangId() > 0) { //公会科技加成
				PlayerData data = getPlayerData(player.getPlayerId());
				for (int techID : data.getTechnologys()) {
					GangScienceCfg cfg = ConfigData.getConfig(GangScienceCfg.class, techID);
					if (cfg.type == 10) {
						maxEnergy += cfg.param;
						break;
					}
				}
			}
			if (player.getEnergy() >= maxEnergy) {
				player.setEnergyTime(System.currentTimeMillis());
				return;
			}
			long now = System.currentTimeMillis();
			long passTime = now - player.getEnergyTime();
			if (passTime >= 5 * TimeUtil.ONE_MIN) {
				int count = (int) (passTime / (5 * TimeUtil.ONE_MIN));
				int newEnergy = player.getEnergy() + count * ConfigData.globalParam().restoreEnergy;
				if (newEnergy > maxEnergy) {
					newEnergy = maxEnergy;
				}
				player.setEnergy(newEnergy);
				player.setEnergyTime(now);
				refreshPlayerToClient(player.getPlayerId());
			}
		}

	}
	
	//刷新穿越仪能量
	public void refreshTraversingEnergy(Player player){
		synchronized (player) {
			long now = System.currentTimeMillis();
			int playerId = player.getPlayerId();
			VIPConfig vipCfg = GameData.getConfig(VIPConfig.class, player.getVip());
			int maxEnergy = vipCfg.traveringEnergy;
			PlayerData data = getPlayerData(playerId);
			PlayerCurrency currency = data.getCurrency();
			long curEnergy = currency.get(Goods.TRAVERSING_ENERGY);
			if(curEnergy >= maxEnergy){
				data.setTraversingEnergyResetTime(now);
				return;
			}
			long passTime = now - data.getTraversingEnergyResetTime();
			int[] params = ConfigData.globalParam().restoreTraversingEnergy;
			if (passTime >= params[0] * TimeUtil.ONE_MIN) {
				int count = (int) (passTime / (params[0] * TimeUtil.ONE_MIN));
				long newEnergy = curEnergy + count * params[1];
				if (newEnergy > maxEnergy) {
					newEnergy = maxEnergy;
				}
				currency.add(Goods.TRAVERSING_ENERGY, newEnergy - curEnergy);
				// 通知前端
				updateCurrencyToClient(playerId, Goods.TRAVERSING_ENERGY,(int)newEnergy);
				data.setTraversingEnergyResetTime(now);
			}
		}
	}

	//更新前端数据
	public void refreshPlayerToClient(int playerId) {
		Player player = getPlayer(playerId);
		if (!player.isRefresh()) {
			player.setRefresh(true);
			return;
		}

		PlayerVo vo = toSLoginVo(playerId);
		SessionManager.getInstance().sendMsg(PlayerExtension.REFRESH_MY_VO, vo, playerId);
	}
	
	//更新部分数据
	public void updateAttrsToClient(int playerId,int... attrs){
		ListParam<Int2Param> result = new ListParam<Int2Param>();
		result.params = new ArrayList<Int2Param>(attrs.length/2);
		for(int i=0;i<attrs.length;i=i+2){
			Int2Param attr = new Int2Param();
			attr.param1 = attrs[i];
			attr.param2 = attrs[i+1];
			result.params.add(attr);
		}
		SessionManager.getInstance().sendMsg(PlayerExtension.UPDATE_ATTR, result, playerId);
	}
	
	public void updateCurrencyToClient(int playerId){
		PlayerCurrency currency = getPlayerData(playerId).getCurrency();
		ListParam<Int2Param> result = new ListParam<Int2Param>();
		result.params = new ArrayList<Int2Param>();
		for(Map.Entry<Integer, Long> entry : currency.getCurrencies().entrySet()){
			Int2Param attr = new Int2Param();
			attr.param1 = entry.getKey();
			long value = entry.getValue();
			attr.param2 = (int)value;
			result.params.add(attr);
		}
		SessionManager.getInstance().sendMsg(PlayerExtension.UPDATE_CURRENCY, result, playerId);
	}
	
	public void updateCurrencyToClient(int playerId,int... attrs){
		ListParam<Int2Param> result = new ListParam<Int2Param>();
		result.params = new ArrayList<Int2Param>(attrs.length/2);
		for(int i=0;i<attrs.length;i=i+2){
			Int2Param attr = new Int2Param();
			attr.param1 = attrs[i];
			attr.param2 = attrs[i+1];
			result.params.add(attr);
		}
		SessionManager.getInstance().sendMsg(PlayerExtension.UPDATE_CURRENCY, result, playerId);
	}
	
	// 添加技能卡
	public void addSkillCard(int playerId, int skillCardId, int count) {

		for (int i = 0; i < count; i++) {
			addSkillCard(playerId, skillCardId);
		}
		skillService.updateSkill2Client(playerId);
	}

	public SkillCard addSkillCard(int playerId, int skillCardId) {
		PlayerData data = getPlayerData(playerId);
		SkillCard card = new SkillCard();
		card.setLev(1);
		card.setCardId(skillCardId);
		synchronized (data) {
			int id = data.getMaxSkillCardId();
			data.setMaxSkillCardId(id + 1);
			data.getSkillCards().put(id, card);
		}
		return card;
	}
	
	public boolean verifyCurrency(int playerId, int type, long offset){
		PlayerCurrency currency = getPlayerData(playerId).getCurrency();
		return currency.verify(type, offset);
	}
	
	public boolean addCurrency(int playerId, int type, long offset, LogConsume actionType, Object ...params){
		Player player = getPlayer(playerId);
		PlayerCurrency currency = getPlayerData(playerId).getCurrency();
		if(type == Goods.TRAVERSING_ENERGY) {
			long value = currency.get(Goods.TRAVERSING_ENERGY);
			VIPConfig config = ConfigData.getConfig(VIPConfig.class,player.getVip());
			if(config.traveringEnergy < value + offset) {
				offset = config.traveringEnergy - value;
			}
		}
		if(currency.add(type, offset)){

			// 通知前端
			updateCurrencyToClient(playerId, type,(int)currency.get(type));
			// 记录日志
			Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true, (int)offset, actionType,
					type,Goods.CURRENCY, params);
			taskService.doTask(playerId, Task.FINISH_CURRENCY, type, (int)offset);
			return true;
		}
		return false;
	}
	
	public boolean decCurrency(int playerId, int type, long offset, LogConsume actionType, Object ...params){
		PlayerCurrency currency = getPlayerData(playerId).getCurrency();
		if(currency.dec(type, offset)){
			Player player = getPlayer(playerId);
			// 通知前端
			updateCurrencyToClient(playerId, type,(int)currency.get(type));
			// 记录日志
			Context.getLoggerService().logConsume(playerId, player.getLev(), player.getVip(), true, (int)offset, actionType,
					type,Goods.CURRENCY, params);
			taskService.doTask(playerId, Task.FINISH_CONSUME, type, (int)offset);
			return true;
		}
		return false;
	}

	public Map<Integer, Player> getPlayers() {
		return players;
	}

	public Map<Integer, PlayerData> getPlayerDatas() {
		return playerDatas;
	}
}
