package com.game.module.serial;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.module.attach.arena.ArenaPlayer;
import com.game.module.attach.training.TrainOpponent;
import com.game.module.copy.CopyRank;
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
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendRequests = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,Boolean>>();
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Boolean>> friendSendRequests = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,Boolean>>();
	
	private ConcurrentHashMap<Integer, CopyRank> copyRanks = new ConcurrentHashMap<Integer, CopyRank>();
	
	// 玩家刷出的商品数据<商店类型,<玩家id,[商品id]>
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> playerRefreshShops = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>>();
	
	//公会重置时间
	private long gangDailyReset = 0L;
	
	public SerialData(){
		
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
	
}
