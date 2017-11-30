package com.game.module.player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.module.activity.ActivityTask;
import com.game.module.activity.WelfareCard;
import com.game.module.copy.Copy;
import com.game.module.copy.TraverseMap;
import com.game.module.fashion.Fashion;
import com.game.module.sct.Train;
import com.game.module.skill.SkillCard;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 本对象存储一些业务系统的数据 最终将系列化成json字节，压缩后存入数据库
 */
public class PlayerData {

	private int playerId;
	private List<Integer> fashions = new ArrayList<Integer>();
	private ConcurrentHashMap<Integer, Long> tempFashions = new ConcurrentHashMap<Integer, Long>();

	private Map<Integer,Fashion> fashionMap = new HashMap<>();
	private int curHead;//头部的时装
	private int sign; //签到
	private int signFlag; //签到标识

	private int[] blankGrids;
	private long dailyTime;
	private long weeklyTime;
	private int guideId;
	private ConcurrentHashMap<Integer,Integer> dailyData = new ConcurrentHashMap<Integer, Integer>();
	private int loginDays=1;
	private int monthCard;
	private long monthCardEnd;
	private ConcurrentHashMap<Integer, Integer> vipReward=new ConcurrentHashMap<Integer, Integer>();// vip礼包领取记录
	private ArrayList<Integer> charges=new ArrayList<Integer>();// 充值记录
	private ArrayList<Integer> funds= new ArrayList<Integer>();// 基金领取记录
	private int fundActive;// 基金激活

	private ConcurrentHashMap<Integer, Boolean> friends = new ConcurrentHashMap<Integer, Boolean>();// 好友
	private LinkedHashMap<Integer, Boolean> recentContacters = new LinkedHashMap<Integer, Boolean>(20, 0.5f){
		private static final long serialVersionUID = 1L;
		@Override
		protected boolean removeEldestEntry(Entry<Integer, Boolean> eldest) {
			return size() > 20;
		}

	};// 陌生人
	private ConcurrentHashMap<Integer, Boolean> black = new ConcurrentHashMap<Integer, Boolean>();// 黑名单

	private ConcurrentHashMap<Integer, Copy> copys = new ConcurrentHashMap<Integer, Copy>();//副本
	private ConcurrentHashMap<Integer, Integer> copyTimes = new ConcurrentHashMap<Integer, Integer>();//副本次数ResetCopy
	private ConcurrentHashMap<Integer, Integer> resetCopy = new ConcurrentHashMap<Integer, Integer>();//副本重置次数
	private List<Integer> threeStars= new ArrayList<Integer>();//三星奖励

	private int equipMaterial;//装备分解活动的材料
	private ConcurrentHashMap<Integer, Integer> strengths = new ConcurrentHashMap<Integer, Integer>();//装备位强化等级

	private Map<Integer,Jewel> jewels = new ConcurrentHashMap<Integer,Jewel>();//装备位强化等级

	private List<Integer> skills=new ArrayList<Integer>();// 开通的技能
	private List<Integer> curSkills = new ArrayList<Integer>(4);// 当前技能
	private int curCardId = 0;//当前技能卡组ID
	private List<List<Integer>> skillCardSets = new ArrayList<List<Integer>>();//技能卡组列表
	private int maxSkillCardId=1;//技能卡id
	private ConcurrentHashMap<Integer,SkillCard> skillCards = new ConcurrentHashMap<Integer, SkillCard>();//技能卡

	// 技能卡未抽中次数
	private ConcurrentHashMap<Integer, Integer> skillCardTimes = new ConcurrentHashMap<>();

	//声望
	private ConcurrentHashMap<Integer, Upgrade> fames = new ConcurrentHashMap<Integer, Upgrade>();
	private int activityCamp = 0; //声望激活阵营代表
	//神器
	private ConcurrentHashMap<Integer, int[]> artifacts = new ConcurrentHashMap<Integer, int[]>();
	//神器升阶
	private Map<Integer,Integer> artifactsLevelUp = new ConcurrentHashMap<>();
	//地图ID
	private int maxTraverseId = 0;
	//副本地图
	private Map<Integer, TraverseMap> traverseMaps = new ConcurrentHashMap<Integer, TraverseMap>();

	//刷新商店的刷新次数<商店类型,刷新次数>
	private ConcurrentHashMap<Integer, Integer> shopRefreshCount = new ConcurrentHashMap<Integer, Integer>();
	//刷新商店的购买记录<商品id，购买次数>
	private ConcurrentHashMap<Integer, Integer> shopBuyRecords = new ConcurrentHashMap<Integer, Integer>();
	//神秘商店出现的时间
	private long mysteryShopTime = 0L;
	//消耗体力总数，会刷新，用于神秘商店
	private int power4Mystery = 0;
	//穿越仪能量刷新时间
	private long traversingEnergyResetTime = 0;

	private long lastQuitGang;

	private PlayerCurrency currency = new PlayerCurrency();
	//VIP礼包领取记录
	private List<Integer> vipGifts = new ArrayList<>();

	////////团队副本
	private int groupTimes; //团队副本次数
	//排位荣誉点
	private int honorPoint;

	private List<Integer> modules =new ArrayList<>();// 开通的模块
	//公会科技
	private Set<Integer> technologys = new HashSet<>();

	private int buyEnergyTimes;

	private int buyCoinTimes;

	private int newHandleStep;

	private Set<Integer> guideSteps = new HashSet<>();

	private Map<Integer,ActivityTask> activityTasks = Maps.newHashMap();

	//首充标识
	private boolean firstRechargeFlag;

	private WelfareCard welfareCard = new WelfareCard();
	private Train train = new Train();
	private int sevenDays = 1; // 7次领奖活动

	public PlayerData(){
		dailyTime = System.currentTimeMillis();
		weeklyTime = System.currentTimeMillis();
	}


	public Train getTrain() {
		return train;
	}

	public int getSevenDays() {
		return sevenDays;
	}

	public void setSevenDays(int sevenDays) {
		this.sevenDays = sevenDays;
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public WelfareCard getWelfareCard() {
		return welfareCard;
	}

	public void setWelfareCard(WelfareCard welfareCard) {
		this.welfareCard = welfareCard;
	}

	public boolean isFirstRechargeFlag() {
		return firstRechargeFlag;
	}

	public void setFirstRechargeFlag(boolean firstRechargeFlag) {
		this.firstRechargeFlag = firstRechargeFlag;
	}

	public Map<Integer, ActivityTask> getActivityTasks() {
		return activityTasks;
	}

	public void setActivityTasks(Map<Integer, ActivityTask> activityTasks) {
		this.activityTasks = activityTasks;
	}

	public int getNewHandleStep() {
		return newHandleStep;
	}

	public Set<Integer> getGuideSteps() {
		return guideSteps;
	}

	public void setGuideSteps(Set<Integer> guideSteps) {
		this.guideSteps = guideSteps;
	}

	public void setNewHandleStep(int newHandleStep) {
		this.newHandleStep = newHandleStep;
	}

	public int getBuyEnergyTimes() {
		return buyEnergyTimes;
	}

	public void setBuyEnergyTimes(int buyEnergyTimes) {
		this.buyEnergyTimes = buyEnergyTimes;
	}

	public int getBuyCoinTimes() {
		return buyCoinTimes;
	}

	public void setBuyCoinTimes(int buyCoinTimes) {
		this.buyCoinTimes = buyCoinTimes;
	}

	public Set<Integer> getTechnologys() {
		return technologys;
	}

	public void setTechnologys(Set<Integer> technologys) {
		this.technologys = technologys;
	}

	public List<Integer> getModules() {
		return modules;
	}

	public void setModules(List<Integer> modules) {
		this.modules = modules;
	}

	public int getHonorPoint() {
		return honorPoint;
	}

	public void setHonorPoint(int honorPoint) {
		this.honorPoint = honorPoint;
	}

	public int getGroupTimes() {
		return groupTimes;
	}

	public void setGroupTimes(int groupTimes) {
		this.groupTimes = groupTimes;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public List<Integer> getFashions() {
		return fashions;
	}

	public void setFashions(List<Integer> fashions) {
		this.fashions = fashions;
	}

	public int[] getBlankGrids() {
		return blankGrids;
	}

	public void setBlankGrids(int[] blankGrids) {
		this.blankGrids = blankGrids;
	}

	public long getDailyTime() {
		return dailyTime;
	}

	public long getWeeklyTime() {
		return weeklyTime;
	}

	public void setWeeklyTime(long weeklyTime) {
		this.weeklyTime = weeklyTime;
	}

	public void setDailyTime(long dailyTime) {
		this.dailyTime = dailyTime;
	}

	public int getGuideId() {
		return guideId;
	}

	public void setGuideId(int guideId) {
		this.guideId = guideId;
	}

	public ConcurrentHashMap<Integer, Integer> getDailyData() {
		return dailyData;
	}

	public void setDailyData(ConcurrentHashMap<Integer, Integer> dailyData) {
		this.dailyData = dailyData;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public int getMonthCard() {
		return monthCard;
	}

	public void setMonthCard(int monthCard) {
		this.monthCard = monthCard;
	}

	public long getMonthCardEnd() {
		return monthCardEnd;
	}

	public void setMonthCardEnd(long monthCardEnd) {
		this.monthCardEnd = monthCardEnd;
	}

	public ConcurrentHashMap<Integer, Integer> getVipReward() {
		return vipReward;
	}

	public void setVipReward(ConcurrentHashMap<Integer, Integer> vipReward) {
		this.vipReward = vipReward;
	}

	public ArrayList<Integer> getCharges() {
		return charges;
	}

	public void setCharges(ArrayList<Integer> charges) {
		this.charges = charges;
	}

	public ArrayList<Integer> getFunds() {
		return funds;
	}

	public void setFunds(ArrayList<Integer> funds) {
		this.funds = funds;
	}

	public int getFundActive() {
		return fundActive;
	}

	public void setFundActive(int fundActive) {
		this.fundActive = fundActive;
	}

	public ConcurrentHashMap<Integer, Boolean> getFriends() {
		return friends;
	}

	public void setFriends(ConcurrentHashMap<Integer, Boolean> friends) {
		this.friends = friends;
	}

	public LinkedHashMap<Integer, Boolean> getRecentContacters() {
		return recentContacters;
	}

	public void setRecentContacters(LinkedHashMap<Integer, Boolean> recentContacters) {
		this.recentContacters = recentContacters;
	}

	public ConcurrentHashMap<Integer, Boolean> getBlack() {
		return black;
	}

	public void setBlack(ConcurrentHashMap<Integer, Boolean> black) {
		this.black = black;
	}

	public long getLastQuitGang() {
		return lastQuitGang;
	}

	public void setLastQuitGang(long lastQuitGang) {
		this.lastQuitGang = lastQuitGang;
	}

	public ConcurrentHashMap<Integer, Copy> getCopys() {
		return copys;
	}

	public void setCopys(ConcurrentHashMap<Integer, Copy> copys) {
		this.copys = copys;
	}

	public ConcurrentHashMap<Integer, Integer> getCopyTimes() {
		return copyTimes;
	}

	public void setCopyTimes(ConcurrentHashMap<Integer, Integer> copyTimes) {
		this.copyTimes = copyTimes;
	}

	public ConcurrentHashMap<Integer, Integer> getResetCopy() {
		return resetCopy;
	}

	public void setResetCopy(ConcurrentHashMap<Integer, Integer> resetCopy) {
		this.resetCopy = resetCopy;
	}

	public List<Integer> getThreeStars() {
		return threeStars;
	}

	public void setThreeStars(List<Integer> threeStars) {
		this.threeStars = threeStars;
	}

	public int getEquipMaterial() {
		return equipMaterial;
	}

	public void setEquipMaterial(int equipMaterial) {
		this.equipMaterial = equipMaterial;
	}

	public ConcurrentHashMap<Integer, Integer> getStrengths() {
		return strengths;
	}

	public void setStrengths(ConcurrentHashMap<Integer, Integer> strengths) {
		this.strengths = strengths;
	}

	public Map<Integer, Jewel> getJewels() {
		return jewels;
	}

	public void setJewels(Map<Integer, Jewel> jewels) {
		this.jewels = jewels;
	}

	public List<Integer> getSkills() {
		return skills;
	}

	public void setSkills(List<Integer> skills) {
		this.skills = skills;
	}

	public List<Integer> getCurSkills() {
		return curSkills;
	}

	public void setCurSkills(List<Integer> curSkills) {
		while(curSkills.size()<4){
			curSkills.add(0);
		}
		this.curSkills = curSkills;
	}

	public int getMaxSkillCardId() {
		return maxSkillCardId;
	}

	public void setMaxSkillCardId(int maxSkillCardId) {
		this.maxSkillCardId = maxSkillCardId;
	}

	public ConcurrentHashMap<Integer, SkillCard> getSkillCards() {
		return skillCards;
	}

	public void setSkillCards(ConcurrentHashMap<Integer, SkillCard> skillCards) {
		this.skillCards = skillCards;
	}

	public int getCurCardId() {
		return curCardId;
	}

	public void setCurCardId(int curCardId) {
		this.curCardId = curCardId;
	}

	public List<List<Integer>> getSkillCardSets() {
		if(skillCardSets.isEmpty()){
			List<Integer> first = Arrays.asList(0,0,0,0);
			skillCardSets.add(first);
		}
		return skillCardSets;
	}

	@JsonIgnore
	public List<Integer> getCurrCard(){
		List<Integer> set = getSkillCardSets().get(curCardId);
		if(set == null)
			set = skillCardSets.get(0);
		return set;
	}

	@JsonIgnore
	public List<Integer> getCurrCardIds(){
		List<Integer> set = getSkillCardSets().get(curCardId);
		if(set == null)
			set = skillCardSets.get(0);

		List<Integer> list = Lists.newArrayList();
		for(int id : set) {
			SkillCard card = skillCards.get(id);
			if(card != null) {
				list.add(card.getCardId());
			}else {
				list.add(0);
			}
		}
		return list;
	}

	public void setSkillCardSets(List<List<Integer>> skillCardSets) {
		this.skillCardSets = skillCardSets;
	}

	public ConcurrentHashMap<Integer, Integer> getSkillCardTimes() {
		return skillCardTimes;
	}

	public void setSkillCardTimes(ConcurrentHashMap<Integer, Integer> skillCardTimes) {
		this.skillCardTimes = skillCardTimes;
	}

	public ConcurrentHashMap<Integer, Upgrade> getFames() {
		return fames;
	}

	public void setFames(ConcurrentHashMap<Integer, Upgrade> fames) {
		this.fames = fames;
	}

	public ConcurrentHashMap<Integer, int[]> getArtifacts() {
		return artifacts;
	}

	public int getMaxTraverseId() {
		return maxTraverseId;
	}

	public void setMaxTraverseId(int maxTraverseId) {
		this.maxTraverseId = maxTraverseId;
	}

	public Map<Integer, TraverseMap> getTraverseMaps() {
		return traverseMaps;
	}

	public void setTraverseMaps(Map<Integer, TraverseMap> traverseMaps) {
		this.traverseMaps = traverseMaps;
	}

	public void setArtifacts(ConcurrentHashMap<Integer, int[]> artifacts) {
		this.artifacts = artifacts;
	}

	public ConcurrentHashMap<Integer, Integer> getShopRefreshCount() {
		return shopRefreshCount;
	}

	public void setShopRefreshCount(ConcurrentHashMap<Integer, Integer> shopRefreshCount) {
		this.shopRefreshCount = shopRefreshCount;
	}

	public ConcurrentHashMap<Integer, Integer> getShopBuyRecords() {
		return shopBuyRecords;
	}

	public void setShopBuyRecords(ConcurrentHashMap<Integer, Integer> shopBuyRecords) {
		this.shopBuyRecords = shopBuyRecords;
	}

	public long getMysteryShopTime() {
		return mysteryShopTime;
	}

	public void setMysteryShopTime(long mysteryShopTime) {
		this.mysteryShopTime = mysteryShopTime;
	}

	public int getPower4Mystery() {
		return power4Mystery;
	}

	public void setPower4Mystery(int power4Mystery) {
		this.power4Mystery = power4Mystery;
	}

	public PlayerCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(PlayerCurrency currency) {
		this.currency = currency;
	}

	public long getTraversingEnergyResetTime() {
		return traversingEnergyResetTime;
	}

	public void setTraversingEnergyResetTime(long traversingEnergyResetTime) {
		this.traversingEnergyResetTime = traversingEnergyResetTime;
	}

	public ConcurrentHashMap<Integer, Long> getTempFashions() {
		return tempFashions;
	}

	public void setTempFashions(ConcurrentHashMap<Integer, Long> tempFashions) {
		this.tempFashions = tempFashions;
	}

	public int getCurHead() {
		return curHead;
	}

	public void setCurHead(int curHead) {
		this.curHead = curHead;
	}

	public Map<Integer, Fashion> getFashionMap() {
		return fashionMap;
	}

	public void setFashionMap(Map<Integer, Fashion> fashionMap) {
		this.fashionMap = fashionMap;
	}

	public int getSign() {
		return sign;
	}

	public void setSign(int sign) {
		this.sign = sign;
	}

	public int getSignFlag() {
		return signFlag;
	}

	public void setSignFlag(int signFlag) {
		this.signFlag = signFlag;
	}

	public List<Integer> getVipGifts() {
		return vipGifts;
	}

	public void setVipGifts(List<Integer> vipGifts) {
		this.vipGifts = vipGifts;
	}

	public Map<Integer, Integer> getArtifactsLevelUp() {
		return artifactsLevelUp;
	}

	public void setArtifactsLevelUp(Map<Integer, Integer> artifactsLevelUp) {
		this.artifactsLevelUp = artifactsLevelUp;
	}

	public int getActivityCamp() {
		return activityCamp;
	}

	public void setActivityCamp(int activityCamp) {
		this.activityCamp = activityCamp;
	}
}
