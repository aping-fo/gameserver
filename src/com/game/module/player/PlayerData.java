package com.game.module.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.module.activity.ActivityTask;
import com.game.module.activity.WelfareCard;
import com.game.module.copy.Copy;
import com.game.module.copy.TraverseMap;
import com.game.module.fashion.Fashion;
import com.game.module.sct.Train;
import com.game.module.skill.SkillCard;
import com.game.module.title.Title;
import com.game.params.training.TrainOpponentVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本对象存储一些业务系统的数据 最终将系列化成json字节，压缩后存入数据库
 */
public class PlayerData {

    private int playerId;
    private List<Integer> fashions = new ArrayList<Integer>();
    private ConcurrentHashMap<Integer, Long> tempFashions = new ConcurrentHashMap<Integer, Long>();

    private Map<Integer, Fashion> fashionMap = new HashMap<>();
    private int curHead;//头部的时装
    private int sign; //签到
    private int signFlag; //签到标识

    private int[] blankGrids;
    private long dailyTime;
    private long weeklyTime;
    private int guideId;
    private ConcurrentHashMap<Integer, Integer> dailyData = new ConcurrentHashMap<Integer, Integer>();
    private int loginDays = 1;
    private int monthCard;
    private long monthCardEnd;
    private ConcurrentHashMap<Integer, Integer> vipReward = new ConcurrentHashMap<Integer, Integer>();// vip礼包领取记录
    private ArrayList<Integer> charges = new ArrayList<Integer>();// 充值记录
    private ArrayList<Integer> funds = new ArrayList<Integer>();// 基金领取记录
    private int fundActive;// 基金激活

    private ConcurrentHashMap<Integer, Boolean> friends = new ConcurrentHashMap<Integer, Boolean>();// 好友
    private LinkedHashMap<Integer, Boolean> recentContacters = new LinkedHashMap<Integer, Boolean>(20, 0.5f) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Entry<Integer, Boolean> eldest) {
            return size() > 20;
        }

    };// 陌生人
    private ConcurrentHashMap<Integer, Boolean> black = new ConcurrentHashMap<Integer, Boolean>();// 黑名单

    private ConcurrentHashMap<Integer, Copy> copys = new ConcurrentHashMap<Integer, Copy>();//副本
    private ConcurrentHashMap<Integer, Integer> copyTimes = new ConcurrentHashMap<Integer, Integer>();//副本次数ResetCopy
    private Map<Integer, Integer> copyBuyTimes = Maps.newHashMap(); //副本购买次数
    private ConcurrentHashMap<Integer, Integer> resetCopy = new ConcurrentHashMap<Integer, Integer>();//副本重置次数
    private List<Integer> threeStars = new ArrayList<Integer>();//三星奖励

    private int equipMaterial;//装备分解活动的材料
    private ConcurrentHashMap<Integer, Integer> strengths = new ConcurrentHashMap<Integer, Integer>();//装备位强化等级
    private ConcurrentHashMap<Integer, Integer> stars = new ConcurrentHashMap<Integer, Integer>();//装备位星等级

    private Map<Integer, Jewel> jewels = new ConcurrentHashMap<Integer, Jewel>();//装备位强化等级

    private List<Integer> skills = new ArrayList<Integer>();// 开通的技能
    private List<Integer> curSkills = new ArrayList<Integer>(4);// 当前技能
    private int curCardId = 0;//当前技能卡组ID
    private List<List<Integer>> skillCardSets = new ArrayList<List<Integer>>();//技能卡组列表
    private int maxSkillCardId = 1;//技能卡id
    private ConcurrentHashMap<Integer, SkillCard> skillCards = new ConcurrentHashMap<Integer, SkillCard>();//技能卡

    // 技能卡未抽中次数
    private ConcurrentHashMap<Integer, Integer> skillCardTimes = new ConcurrentHashMap<>();

    //声望
    private ConcurrentHashMap<Integer, Upgrade> fames = new ConcurrentHashMap<Integer, Upgrade>();
    //private int activityCamp = 0; //声望激活阵营代表
    //神器
    private ConcurrentHashMap<Integer, int[]> artifacts = new ConcurrentHashMap<Integer, int[]>();
    //神器升阶
    private Map<Integer, Integer> artifactsLevelUp = new ConcurrentHashMap<>();
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

    private List<Integer> modules = new ArrayList<>();// 开通的模块
    private Set<Integer> hitModules = Sets.newHashSet();// 点击过的
    private Map<Integer, Integer> hitModulesState = new ConcurrentHashMap<>();
    private Set<Integer> actionModules = Sets.newHashSet();//
    //公会科技
    private Set<Integer> technologys = new HashSet<>();

    private int buyEnergyTimes;

    private int buyCoinTimes;

    private int newHandleStep;

    private Set<Integer> guideSteps = new HashSet<>();

    //<taskId,>
    private Map<Integer, ActivityTask> activityTasks = Maps.newConcurrentMap();

    //首充标识
    private boolean firstRechargeFlag;

    private WelfareCard welfareCard = new WelfareCard();
    private Train train = new Train();
    private int sevenDays = 1; // 7次领奖活动
    private int dailyRecharge = 1; // 每日充值
    //所有称号
    private Map<Integer, Map<Integer, Title>> titleTypeMap = Maps.newHashMap();
    //拥有的称号
    private Set<Integer> titles = new HashSet<>();
    private int arenaWins; //竞技场连胜次数
    //商城累计购买次数
    private int buyCount;
    //任务累计完成
    private int finishTaskCount;
    //累计签到次数
    private int signTotal;
    //体力购买次数
    private int energyCount;
    //称号状态
    private Map<Integer, Integer> titleRead = Maps.newHashMap();
    //套装
    private Map<Integer, Set<Integer>> suitMap = Maps.newHashMap();

    private String serverId;
    private String serverName;

    private String thirdChannel; //第三方渠道
    private String thirdUserId; //第三方渠道user id

    private boolean robotFlag;
    //在线时长
    private int onlineTime;

    private int roleId;

    //是否领取激活码礼包
    private boolean receiveGiftBag;
    private Set<String> giftBagSet = new HashSet<>();

    private Set<Long> globalMailIDSet = new HashSet<>();

    private int cpId; //订单号自增ID
    private Set<Long> CpIdSet = new HashSet<>(); //生成订单号
    private Set<Long> dealCpIdSet = new HashSet<>(); //已经处理过的订单号

    private int dramaOrder; //剧情进度
    private Map<Integer, Integer> fastestRecordMap = new ConcurrentHashMap<>();//副本最快通关记录
    private int ladderRecordsTime = 0;//排位赛获取奖励次数
    private int addUpRechargeDiamondsTimes = 0;//累计充值钻石达成最高档的次数
    private Map<Integer, Integer> activityDropTimeMap = new ConcurrentHashMap<>();//获取活动物品的副本和次数
    private int twoDays = 1; // 2次领奖活动
    private int hurt; // 副本伤害
    private int singleAndMulti;//单人或多人
    private Map<Integer, Integer> awakeningSkillMap = new ConcurrentHashMap<>();//觉醒技能
    private Integer maxArenaRanking; // 竞技场最高排名
    private Set<Integer> fashionRankSet = new HashSet<>();//时装阶级
    private int glamour; // 魅力值
    private boolean gm;//是否gm
    private Set<Integer> delFriends = new HashSet<>();// 删除过的好友
    private int refreshDestinyCardTimes;//命运卡牌刷新次数
    private Set<Integer> cardRewardIdxSet = new HashSet<>(); //卡牌奖励
    private List<int[]> cardRewards = new ArrayList<>();

    //累计充值
    private float totalCharge;
    private int loginContinueDays; //连续登陆
    private int maxLoginContinueDays; //连续登陆
    private Map<Integer, Integer> shopBuyAllMap = new ConcurrentHashMap<>();//商店全部购买
    private int challengeTimes; //公会副本挑战次数
    private float highestFighting; //最高战力，用于计算英雄试炼
    private byte[] data;
    private Set<Integer> guildAwardsSet = new HashSet<>();//公会奖励

    public Set<Integer> getGuildAwardsSet() {
        return guildAwardsSet;
    }

    public void setGuildAwardsSet(Set<Integer> guildAwardsSet) {
        this.guildAwardsSet = guildAwardsSet;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public float getHighestFighting() {
        return highestFighting;
    }

    public void setHighestFighting(float highestFighting) {
        this.highestFighting = highestFighting;
    }

    public int getChallengeTimes() {
        return challengeTimes;
    }

    public void setChallengeTimes(int challengeTimes) {
        this.challengeTimes = challengeTimes;
    }

    public ConcurrentHashMap<Integer, Integer> getStars() {
        return stars;
    }

    public void setStars(ConcurrentHashMap<Integer, Integer> stars) {
        this.stars = stars;
    }

    public Map<Integer, Integer> getShopBuyAllMap() {
        return shopBuyAllMap;
    }

    public void setShopBuyAllMap(Map<Integer, Integer> shopBuyAllMap) {
        this.shopBuyAllMap = shopBuyAllMap;
    }

    public int getMaxLoginContinueDays() {
        return maxLoginContinueDays;
    }

    public void setMaxLoginContinueDays(int maxLoginContinueDays) {
        this.maxLoginContinueDays = maxLoginContinueDays;
    }

    public float getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(float totalCharge) {
        this.totalCharge = totalCharge;
    }

    public int getLoginContinueDays() {
        return loginContinueDays;
    }

    public void setLoginContinueDays(int loginContinueDays) {
        this.loginContinueDays = loginContinueDays;
    }

    public List<int[]> getCardRewards() {
        return cardRewards;
    }

    public void setCardRewards(List<int[]> cardRewards) {
        this.cardRewards = cardRewards;
    }

    public Set<Integer> getCardRewardIdxSet() {
        return cardRewardIdxSet;
    }

    public void setCardRewardIdxSet(Set<Integer> cardRewardIdxSet) {
        this.cardRewardIdxSet = cardRewardIdxSet;
    }

    public int getRefreshDestinyCardTimes() {
        return refreshDestinyCardTimes;
    }

    public void setRefreshDestinyCardTimes(int refreshDestinyCardTimes) {
        this.refreshDestinyCardTimes = refreshDestinyCardTimes;
    }

    public Set<Integer> getDelFriends() {
        return delFriends;
    }

    public void setDelFriends(Set<Integer> delFriends) {
        this.delFriends = delFriends;
    }

    @JsonIgnore
    public boolean isGm() {
        return gm;
    }

    @JsonIgnore
    public void setGm(boolean gm) {
        this.gm = gm;
    }

    public int getGlamour() {
        return glamour;
    }

    public void setGlamour(int glamour) {
        this.glamour = glamour;
    }

    public Set<Integer> getFashionRankSet() {
        return fashionRankSet;
    }

    public void setFashionRankSet(Set<Integer> fashionRankSet) {
        this.fashionRankSet = fashionRankSet;
    }

    public Integer getMaxArenaRanking() {
        return maxArenaRanking;
    }

    public void setMaxArenaRanking(Integer maxArenaRanking) {
        this.maxArenaRanking = maxArenaRanking;
    }

    @JsonIgnore
    public int getSingleAndMulti() {
        return singleAndMulti;
    }

    @JsonIgnore
    public void setSingleAndMulti(int singleAndMulti) {
        this.singleAndMulti = singleAndMulti;
    }

    @JsonIgnore
    public int getHurt() {
        return hurt;
    }

    @JsonIgnore
    public void setHurt(int hurt) {
        this.hurt = hurt;
    }

    public int getTwoDays() {
        return twoDays;
    }

    public void setTwoDays(int twoDays) {
        this.twoDays = twoDays;
    }

    public Map<Integer, Integer> getActivityDropTimeMap() {
        return activityDropTimeMap;
    }

    public void setActivityDropTimeMap(Map<Integer, Integer> activityDropTimeMap) {
        this.activityDropTimeMap = activityDropTimeMap;
    }

    public int getAddUpRechargeDiamondsTimes() {
        return addUpRechargeDiamondsTimes;
    }

    public void setAddUpRechargeDiamondsTimes(int addUpRechargeDiamondsTimes) {
        this.addUpRechargeDiamondsTimes = addUpRechargeDiamondsTimes;
    }

    public int getLadderRecordsTime() {
        return ladderRecordsTime;
    }

    public void setLadderRecordsTime(int ladderRecordsTime) {
        this.ladderRecordsTime = ladderRecordsTime;
    }

    public Map<Integer, Integer> getFastestRecordMap() {
        return fastestRecordMap;
    }

    public void setFastestRecordMap(Map<Integer, Integer> fastestRecordMap) {
        this.fastestRecordMap = fastestRecordMap;
    }

    public int getDailyRecharge() {
        return dailyRecharge;
    }

    public void setDailyRecharge(int dailyRecharge) {
        this.dailyRecharge = dailyRecharge;
    }

    public Map<Integer, Integer> getCopyBuyTimes() {
        return copyBuyTimes;
    }

    public void setCopyBuyTimes(Map<Integer, Integer> copyBuyTimes) {
        this.copyBuyTimes = copyBuyTimes;
    }

    public Set<Long> getGlobalMailIDSet() {
        return globalMailIDSet;
    }

    public Set<Long> getCpIdSet() {
        return CpIdSet;
    }

    public void setCpIdSet(Set<Long> cpIdSet) {
        CpIdSet = cpIdSet;
    }

    public int getCpId() {
        return cpId;
    }

    public void setCpId(int cpId) {
        this.cpId = cpId;
    }

    public Set<Long> getDealCpIdSet() {
        return dealCpIdSet;
    }

    public void setDealCpIdSet(Set<Long> dealCpIdSet) {
        this.dealCpIdSet = dealCpIdSet;
    }

    public void setGlobalMailIDSet(Set<Long> globalMailIDSet) {
        this.globalMailIDSet = globalMailIDSet;
    }

    public Map<Integer, Integer> getHitModulesState() {
        return hitModulesState;
    }

    public void setHitModulesState(Map<Integer, Integer> hitModulesState) {
        this.hitModulesState = hitModulesState;
    }

    public Set<Integer> getActionModules() {
        return actionModules;
    }

    public void setActionModules(Set<Integer> actionModules) {
        this.actionModules = actionModules;
    }

    public Map<Integer, Integer> getAwakeningSkillMap() {
        return awakeningSkillMap;
    }

    public void setAwakeningSkillMap(Map<Integer, Integer> awakeningSkillMap) {
        this.awakeningSkillMap = awakeningSkillMap;
    }

    public Set<String> getGiftBagSet() {
        return giftBagSet;
    }

    public void setGiftBagSet(Set<String> giftBagSet) {
        this.giftBagSet = giftBagSet;
    }

    public boolean isReceiveGiftBag() {
        return receiveGiftBag;
    }

    public void setReceiveGiftBag(boolean receiveGiftBag) {
        this.receiveGiftBag = receiveGiftBag;
    }

    public PlayerData() {
        dailyTime = System.currentTimeMillis();
        weeklyTime = System.currentTimeMillis();
    }

    public Set<Integer> getHitModules() {
        return hitModules;
    }

    public void setHitModules(Set<Integer> hitModules) {
        this.hitModules = hitModules;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(int onlineTime) {
        this.onlineTime = onlineTime;
    }

    public boolean isRobotFlag() {
        return robotFlag;
    }

    public void setRobotFlag(boolean robotFlag) {
        this.robotFlag = robotFlag;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getThirdChannel() {
        return thirdChannel;
    }

    public void setThirdChannel(String thirdChannel) {
        this.thirdChannel = thirdChannel;
    }

    public String getThirdUserId() {
        return thirdUserId;
    }

    public void setThirdUserId(String thirdUserId) {
        this.thirdUserId = thirdUserId;
    }

    public Map<Integer, Map<Integer, Title>> getTitleTypeMap() {
        return titleTypeMap;
    }

    public void setTitleTypeMap(Map<Integer, Map<Integer, Title>> titleTypeMap) {
        this.titleTypeMap = titleTypeMap;
    }

    public Map<Integer, Set<Integer>> getSuitMap() {
        return suitMap;
    }

    public void setSuitMap(Map<Integer, Set<Integer>> suitMap) {
        this.suitMap = suitMap;
    }

    public Map<Integer, Integer> getTitleRead() {
        return titleRead;
    }

    public void setTitleRead(Map<Integer, Integer> titleRead) {
        this.titleRead = titleRead;
    }

    public int getEnergyCount() {
        return energyCount;
    }

    public void setEnergyCount(int energyCount) {
        this.energyCount = energyCount;
    }

    public int getSignTotal() {
        return signTotal;
    }

    public void setSignTotal(int signTotal) {
        this.signTotal = signTotal;
    }

    public int getFinishTaskCount() {
        return finishTaskCount;
    }

    public void setFinishTaskCount(int finishTaskCount) {
        this.finishTaskCount = finishTaskCount;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getArenaWins() {
        return arenaWins;
    }

    public void setArenaWins(int arenaWins) {
        this.arenaWins = arenaWins;
    }

    public Set<Integer> getTitles() {
        return titles;
    }

    public void setTitles(Set<Integer> titles) {
        this.titles = titles;
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

    @JsonIgnore
    public Collection<ActivityTask> getAllActivityTasks() {
        return activityTasks.values();
    }

    @JsonIgnore
    public ActivityTask getActivityTask(int taskId) {
        return activityTasks.get(taskId);
    }

    @JsonIgnore
    public boolean hasActivityTask(int taskId) {
        return activityTasks.containsKey(taskId);
    }

    @JsonIgnore
    public void addActivityTask(int taskId, ActivityTask activityTask) {
        if (activityTask == null)
            return;

        activityTasks.put(taskId, activityTask);
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
        while (curSkills.size() < 4) {
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
        if (skillCardSets.isEmpty()) {
            curCardId = 0;
            List<Integer> first = Arrays.asList(0, 0, 0, 0);
            skillCardSets.add(first);
        }

        if (curCardId > 0 && skillCardSets.size() == 1) {
            curCardId = 0;
        }
        return skillCardSets;
    }

    @JsonIgnore
    public List<Integer> getCurrCard() {
        List<Integer> set = getSkillCardSets().get(curCardId);
        if (set == null) {
            if (skillCardSets.isEmpty()) {
                List<Integer> first = Arrays.asList(0, 0, 0, 0);
                skillCardSets.add(first);
            }
            set = skillCardSets.get(0);
        }
        return set;
    }

    @JsonIgnore
    public List<Integer> getCurrCardIds() {
        List<Integer> set = getSkillCardSets().get(curCardId);
        if (set == null)
            set = skillCardSets.get(0);

        List<Integer> list = Lists.newArrayList();
        for (int id : set) {
            SkillCard card = skillCards.get(id);
            if (card != null) {
                list.add(card.getCardId());
            } else {
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

//	public int getActivityCamp() {
//		return activityCamp;
//	}
//
//	public void setActivityCamp(int activityCamp) {
//		this.activityCamp = activityCamp;
//	}

    public int getDramaOrder() {
        return this.dramaOrder;
    }

    public void setDramaOrder(int dramaOrder) {
        this.dramaOrder = dramaOrder;
    }
}
