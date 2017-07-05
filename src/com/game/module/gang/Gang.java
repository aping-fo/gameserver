package com.game.module.gang;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.util.BeanManager;

public class Gang {

	public static final int MAIN_BUILD = 1;// 主建筑

	public static final int ADMIN = 1;
	public static final int VICE_ADMIN = 2;
	public static final int MEMBER = 0;
	
	private int id;// id
	private String name;// 名称
	private int ownerId;// 帮主id
	private Set<Integer> admins = new HashSet<Integer>();
	

	private String notice;// 公告
	private int maxNum;// 最高人数

	private Map<Integer, GMember> members = new ConcurrentHashMap<Integer, GMember>();// 成员

	private AtomicInteger asset = new AtomicInteger(0);// 公会资金
	private int totalAsset = 0;// 总资金
	private boolean autoJoin = true;// 自动加入
	private boolean limitLev;
	private int levLimit;// 等级限制
	private boolean limitFight;
	private int fightLimit;// 战斗力限制

	private Map<Integer, Long> applys = new ConcurrentHashMap<Integer, Long>();// 请求
	private Map<Integer, Integer> buildings = new ConcurrentHashMap<Integer, Integer>();// 建筑等级
	
	private boolean contribute;
	private Calendar createDate;
	
	private Map<Integer, Task> tasks = new HashMap<Integer, Task>();//公会任务
	private GTRoom gtRoom;//当前练功房

	// 临时
	@JsonIgnore
	private int totalFight;// 总战力
	@JsonIgnore
	private volatile boolean updated = false;// 是否更新
	@JsonIgnore
	private int rank;
	
	public Gang(){
		
	}
	
	public Gang(int id){
		this.id = id;
		this.createDate = Calendar.getInstance();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public int getTotalFight() {
		return totalFight;
	}

	public void setTotalFight(int totalFight) {
		this.totalFight = totalFight;
	}

	public int getLev() {
		return buildings.get(MAIN_BUILD);
	}

	public void setLev(int lev) {
		buildings.put(MAIN_BUILD, lev);
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public int getAsset() {
		return asset.get();
	}

	public void setAsset(int asset) {
		this.asset.set(asset);
	}

	public void alterAsset(int count) {
		this.asset.getAndAdd(count);
	}

	public int getTotalAsset() {
		return totalAsset;
	}

	public void setTotalAsset(int totalAsset) {
		this.totalAsset = totalAsset;
	}
	
	public void alterTotalAsset(int value){
		this.totalAsset += value;
	}

	public boolean getAutoJoin() {
		return autoJoin;
	}

	public void setAutoJoin(boolean autoJoin) {
		this.autoJoin = autoJoin;
	}

	

	public boolean isLimitLev() {
		return limitLev;
	}

	public void setLimitLev(boolean limitLev) {
		this.limitLev = limitLev;
	}

	public int getLevLimit() {
		return levLimit;
	}

	public void setLevLimit(int levLimit) {
		this.levLimit = levLimit;
	}

	public boolean isLimitFight() {
		return limitFight;
	}

	public void setLimitFight(boolean limitFight) {
		this.limitFight = limitFight;
	}

	public int getFightLimit() {
		return fightLimit;
	}

	public void setFightLimit(int fightLimit) {
		this.fightLimit = fightLimit;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public Set<Integer> getAdmins() {
		return admins;
	}

	public void setAdmins(Set<Integer> admins) {
		this.admins = admins;
	}

	public Map<Integer, GMember> getMembers() {
		return members;
	}

	public void setMembers(Map<Integer, GMember> members) {
		this.members = members;
	}

	public Map<Integer, Long> getApplys() {
		return applys;
	}

	public void setApplys(Map<Integer, Long> applys) {
		this.applys = applys;
	}

	public Map<Integer, Integer> getBuildings() {
		return buildings;
	}

	public void setBuildings(Map<Integer, Integer> buildings) {
		this.buildings = buildings;
	}
	
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isContribute() {
		return contribute;
	}

	public void setContribute(boolean contribute) {
		this.contribute = contribute;
	}

	public Calendar getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	public void refreshFight(){
		int fight = 0;
		for (int playerId : members.keySet()) {
			fight += BeanManager.getBean(PlayerService.class).getPlayer(playerId).getFight();
		}
		setTotalFight(fight);
	}

	public Map<Integer, Task> getTasks() {
		return tasks;
	}

	public void setTasks(Map<Integer, Task> tasks) {
		this.tasks = tasks;
	}

	public GTRoom getGtRoom() {
		return gtRoom;
	}

	public void setGtRoom(GTRoom gtRoom) {
		this.gtRoom = gtRoom;
	}
}
