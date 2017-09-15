package com.game.module.copy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.params.scene.SMonsterVo;

/**
 * 副本实例，包含怪物,玩家等信息
 */
public class CopyInstance {
	
	public static final int TYPE_COMMON = 1;
	public static final int TYPE_ENDLESS = 2;
	public static final int TYPE_TREASURE = 3;
	public static final int TYPE_EXPERIENCE = 7;
	public static final int TYPE_TRAVERSING = 8;
	public static final int TYPE_WORLD_BOSS = 9; //世界BOSS玩法
	public static final int TYPE_GROUP = 10; //团队副本玩法
	public static final int TYPE_LEADAWAY = 11; //顺手牵羊玩法
	public static final int TYPE_GOLD = 12; //金币玩法

	public static final int EASY = 1;
	public static final int HARD = 2;
	
	public static final int FIRST_ENDLESS = 500;

	private Map<Integer, Map<Integer, SMonsterVo>> monsters;
	private Map<Integer,Integer> drops;//掉落[id,数量]
	
	private Map<String,Integer> decDrops;

	private int copyId;
	private long createTime;
	private int passId;//通常情况下passId==copyId。活动副本特殊
	private volatile boolean over;
	
	private List<GoodsNotice> specGoods;
	
	private TraverseMap traverseMap;//特性副本地图ID
	private AtomicInteger members = new AtomicInteger();

	public CopyInstance() {
		createTime = System.currentTimeMillis();
		monsters = new ConcurrentHashMap<Integer, Map<Integer,SMonsterVo>>();
		drops = new ConcurrentHashMap<Integer, Integer>();
		specGoods = new ArrayList<GoodsNotice>(1);
		decDrops = new ConcurrentHashMap<String, Integer>();
	}
	
	public void addDecHpDrop(int id,int percent){
		String key = String.format("%d_%d", id,percent);
		Integer count = decDrops.get(key);
		if(count==null){
			count = 0;
		}
		count+=percent;
		decDrops.put(key, count);
	}
	
	public boolean checkDecDrops(int id,int percent){
		String key = String.format("%d_%d", id,percent);
		Integer count = decDrops.get(key);
		if(count==null){
			count = 0;
		}
		if(count>=100){
			return false;
		}else{
			return true;
		}
	}

	public Map<Integer, Map<Integer, SMonsterVo>> getMonsters() {
		return monsters;
	}

	public int getCopyId() {
		return copyId;
	}
	
	public void setCopyId(int copyId) {
		this.copyId = copyId;
	}

	public long getCreateTime() {
		return createTime;
	}
	
	public Map<Integer,Integer> getDrops(){
		return drops;
	}
	
	public List<GoodsNotice> getSpecReward(){
		return specGoods;
	}

	
	public TraverseMap getTraverseMap() {
		return traverseMap;
	}

	public void setTraverseMap(TraverseMap traverseMap) {
		this.traverseMap = traverseMap;
	}

	public int getPassId() {
		return passId;
	}

	public void setPassId(int passId) {
		this.passId = passId;
	}

	//是否结束
	public boolean isOver() {
		if(over){
			return false;
		}
		for(Map<Integer,SMonsterVo> group:monsters.values()){
			if(!group.isEmpty()){
				return false;
			}
		}
		return true;
	}
	
	public void setOver(boolean over){
		this.over = over;
	}
	
	public AtomicInteger getMembers(){
		return members;
	}
}
