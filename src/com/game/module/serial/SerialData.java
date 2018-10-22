package com.game.module.serial;

import com.game.module.attach.arena.ArenaPlayer;
import com.game.module.attach.training.TrainOpponent;
import com.game.module.copy.Copy;
import com.game.module.copy.CopyRank;
import com.game.module.fashion.Fashion;
import com.game.module.gang.GangDungeon;
import com.game.module.ladder.Ladder;
import com.game.module.rank.StateRank;
import com.game.module.skill.SkillCard;
import com.game.params.Int2Param;
import com.game.params.rank.FashionCopyRankVO;
import com.game.params.rank.LevelRankVO;
import com.game.params.rank.NormalCopyRankVO;
import com.game.params.rank.SkillCardRankVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局的序列化数据
 */
public class SerialData {

    private boolean initArena;
    private boolean initRobot;
    private long trainingReset;
    private Map<Integer, TrainOpponent> opponents = new ConcurrentHashMap<Integer, TrainOpponent>();
    private Map<Integer, List<Integer>> sectionOpponents = new ConcurrentHashMap<Integer, List<Integer>>();
    private ConcurrentHashMap<Integer, ArenaPlayer> ranks = new ConcurrentHashMap<Integer, ArenaPlayer>();
    private ConcurrentHashMap<Integer, ArenaPlayer> playerRanks = new ConcurrentHashMap<Integer, ArenaPlayer>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendRequests = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendSendRequests = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>>();
    private ConcurrentHashMap<Integer, CopyRank> copyRanks = new ConcurrentHashMap<Integer, CopyRank>();

    // 玩家刷出的商品数据<商店类型,<玩家id,[商品id]>
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> playerRefreshShops = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>>();
    //排位赛
    private Map<Integer, Ladder> ladderMap = new ConcurrentHashMap<>();
    //公会BOSS
    private Map<Integer, GangDungeon> gangMap = new ConcurrentHashMap<>();
    //公会重置时间
    private long gangDailyReset = 0L;

    //玩家相关数据最高排名缓存
    private Map<Integer, PlayerView> playerViews = new ConcurrentHashMap<>();
    private Map<Integer, Integer> bossKillTimes = new ConcurrentHashMap<>();
    private Set<Integer> cdkSet = Sets.newHashSet();
    private List<StateRank> stateRanks = Lists.newArrayList();

    //facebook绑定id， <facebookId, <绑定角色id>>
    Map<String, Set<Integer>> facebookBindIds = new ConcurrentHashMap<>();
    //facebook邀请id,  <被邀请FacebookId,<邀请人角色id>>
    Map<String, Set<Integer>> facebookInviteIds = new ConcurrentHashMap<>();
    //facebook邀请成功的玩家id
    Map<Integer, Set<Integer>> facebookInviteSuccessIds = new ConcurrentHashMap<>();
    private Map<Integer, Int2Param> copyPassFastestTimeMap = new ConcurrentHashMap<>();//副本最快通关时间
    private Map<Integer, Map<Integer, Long>> laterDayRewardMap = new ConcurrentHashMap<>();//延时自然日发送奖励的活动
    private AtomicInteger fullServiceAttendance = new AtomicInteger(0);//全服登录人数
    private Map<Integer, Integer> AdventureBoxNumber = new ConcurrentHashMap<>();//奇遇宝箱全服购买次数

    private Map<Integer, FashionCopyRankVO> fashionRankingsMap = new ConcurrentSkipListMap<>();//时装排行
    private Map<Integer, NormalCopyRankVO> copyRankingsMap = new ConcurrentSkipListMap<>();//3星副本排行
    private Map<Integer, SkillCardRankVO> skillCardRankingsMap = new ConcurrentSkipListMap<>();//技能卡排行
    private Map<Integer, LevelRankVO> levelRankingsMap = new ConcurrentSkipListMap<>();//充值排行

    public SerialData() {

    }

    public Map<Integer, LevelRankVO> getLevelRankingsMap() {
        return levelRankingsMap;
    }

    public void setLevelRankingsMap(Map<Integer, LevelRankVO> levelRankingsMap) {
        this.levelRankingsMap = levelRankingsMap;
    }

    public Map<Integer, FashionCopyRankVO> getFashionRankingsMap() {
        return fashionRankingsMap;
    }

    public void setFashionRankingsMap(Map<Integer, FashionCopyRankVO> fashionRankingsMap) {
        this.fashionRankingsMap = fashionRankingsMap;
    }

    public Map<Integer, NormalCopyRankVO> getCopyRankingsMap() {
        return copyRankingsMap;
    }

    public void setCopyRankingsMap(Map<Integer, NormalCopyRankVO> copyRankingsMap) {
        this.copyRankingsMap = copyRankingsMap;
    }

    public Map<Integer, SkillCardRankVO> getSkillCardRankingsMap() {
        return skillCardRankingsMap;
    }

    public void setSkillCardRankingsMap(Map<Integer, SkillCardRankVO> skillCardRankingsMap) {
        this.skillCardRankingsMap = skillCardRankingsMap;
    }

    public Map<Integer, Integer> getAdventureBoxNumber() {
        return AdventureBoxNumber;
    }

    public void setAdventureBoxNumber(Map<Integer, Integer> adventureBoxNumber) {
        AdventureBoxNumber = adventureBoxNumber;
    }

    public AtomicInteger getFullServiceAttendance() {
        return fullServiceAttendance;
    }

    public void setFullServiceAttendance(AtomicInteger fullServiceAttendance) {
        this.fullServiceAttendance = fullServiceAttendance;
    }

    public Map<Integer, Map<Integer, Long>> getLaterDayRewardMap() {
        return laterDayRewardMap;
    }

    public void setLaterDayRewardMap(Map<Integer, Map<Integer, Long>> laterDayRewardMap) {
        this.laterDayRewardMap = laterDayRewardMap;
    }

    public Map<Integer, Int2Param> getCopyPassFastestTimeMap() {
        return copyPassFastestTimeMap;
    }

    public void setCopyPassFastestTimeMap(Map<Integer, Int2Param> copyPassFastestTimeMap) {
        this.copyPassFastestTimeMap = copyPassFastestTimeMap;
    }

    public List<StateRank> getStateRanks() {
        return stateRanks;
    }

    public void setStateRanks(List<StateRank> stateRanks) {
        this.stateRanks = stateRanks;
    }

    public Set<Integer> getCdkSet() {
        return cdkSet;
    }

    public void setCdkSet(Set<Integer> cdkSet) {
        this.cdkSet = cdkSet;
    }

    public Map<Integer, Integer> getBossKillTimes() {
        return bossKillTimes;
    }

    public void setBossKillTimes(Map<Integer, Integer> bossKillTimes) {
        this.bossKillTimes = bossKillTimes;
    }

    public Map<Integer, PlayerView> getPlayerViews() {
        return playerViews;
    }

    public void setPlayerViews(Map<Integer, PlayerView> playerViews) {
        this.playerViews = playerViews;
    }

    public PlayerView getPlayerView(int playerId) {
        PlayerView view = playerViews.get(playerId);
        if (view == null) {
            view = new PlayerView();
            playerViews.put(playerId, view);
        }
        return view;
    }

    public Map<Integer, GangDungeon> getGangMap() {
        return gangMap;
    }

    public void setGangMap(Map<Integer, GangDungeon> gangMap) {
        this.gangMap = gangMap;
    }

    public ConcurrentHashMap<Integer, ArenaPlayer> getRanks() {
        return ranks;
    }

    public void setRanks(ConcurrentHashMap<Integer, ArenaPlayer> ranks) {
        this.ranks = ranks;
    }

    public ConcurrentHashMap<Integer, ArenaPlayer> getPlayerRanks() {
        return playerRanks;
    }

    public void setPlayerRanks(ConcurrentHashMap<Integer, ArenaPlayer> playerRanks) {
        this.playerRanks = playerRanks;
    }

    public boolean getInitArena() {
        return initArena;
    }

    public void setInitArena(boolean initArena) {
        this.initArena = initArena;
    }

    public boolean isInitRobot() {
        return initRobot;
    }

    public void setInitRobot(boolean initRobot) {
        this.initRobot = initRobot;
    }

    public long getTrainingReset() {
        return trainingReset;
    }

    public void setTrainingReset(long trainingReset) {
        this.trainingReset = trainingReset;
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> getFriendSendRequests() {
        return friendSendRequests;
    }

    public void setFriendSendRequests(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendSendRequests) {
        this.friendSendRequests = friendSendRequests;
    }

    public ConcurrentHashMap<Integer, CopyRank> getCopyRanks() {
        return copyRanks;
    }

    public void setCopyRanks(ConcurrentHashMap<Integer, CopyRank> copyRanks) {
        this.copyRanks = copyRanks;
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> getPlayerRefreshShops() {
        return playerRefreshShops;
    }

    public void setPlayerRefreshShops(
            ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> playerRefreshShops) {
        this.playerRefreshShops = playerRefreshShops;
    }

    public long getGangDailyReset() {
        return gangDailyReset;
    }

    public void setGangDailyReset(long gangDailyReset) {
        this.gangDailyReset = gangDailyReset;
    }

    public Map<Integer, TrainOpponent> getOpponents() {
        return opponents;
    }

    public void setOpponents(Map<Integer, TrainOpponent> opponents) {
        this.opponents = opponents;
    }

    public Map<Integer, List<Integer>> getSectionOpponents() {
        return sectionOpponents;
    }

    public void setSectionOpponents(Map<Integer, List<Integer>> sectionOpponents) {
        this.sectionOpponents = sectionOpponents;
    }

    public Map<Integer, Ladder> getLadderMap() {
        return ladderMap;
    }

    public void setLadderMap(Map<Integer, Ladder> ladderMap) {
        this.ladderMap = ladderMap;
    }

    public Map<String, Set<Integer>> getFacebookBindIds() {
        return facebookBindIds;
    }

    public void setFacebookBindIds(Map<String, Set<Integer>> facebookBindIds) {
        this.facebookBindIds = facebookBindIds;
    }

    public Map<String, Set<Integer>> getFacebookInviteIds() {
        return facebookInviteIds;
    }

    public void setFacebookInviteIds(Map<String, Set<Integer>> facebookInviteIds) {
        this.facebookInviteIds = facebookInviteIds;
    }

    public Map<Integer, Set<Integer>> getFacebookInviteSuccessIds() {
        return facebookInviteSuccessIds;
    }

    public void setFacebookInviteSuccessIds(Map<Integer, Set<Integer>> facebookInviteSuccessIds) {
        this.facebookInviteSuccessIds = facebookInviteSuccessIds;
    }
}
