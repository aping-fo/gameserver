package com.game.module.rank;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.game.util.BeanManager;
import com.game.util.CompressUtil;
import com.game.util.DelayUpdater;
import com.game.util.JsonUtils;
import com.server.util.ServerLogger;

public class RankingList<T extends IRankCA> {

	protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	protected final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	protected final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
	private final int type;
	private final int maxCapacity;
	private Map<Integer, T> keys;
	private TreeMap<T, RankEntity> entities;
	private List<RankEntity> orderList;
	private RankService service;
	private final DelayUpdater updater;

	public RankingList(int type, int maxCapacity, int updatePeriod){
		this.type = type;
		this.maxCapacity = maxCapacity;
		keys = new ConcurrentHashMap<Integer, T>();
		entities = new TreeMap<T, RankEntity>();
		service = BeanManager.getBean(RankService.class);
		updater = new DelayUpdater(updatePeriod) {
			@Override
			public boolean update() {
				return saveDb();
			}
		};
	}

	public void putEntity(int playerId, T entity){
		keys.put(playerId, entity);
		entity.setOwner(playerId);
		RankEntity rEntity = new RankEntity(type, playerId, entity);
		entities.put(entity, rEntity);
	}

	public void putAll(Map<Integer, T> keys){
		//this.keys.putAll(keys);
		for(Map.Entry<Integer, T> entry : keys.entrySet()){
			putEntity(entry.getKey(), entry.getValue());
			//RankEntity rEntity = new RankEntity(type, entry.getKey(), entry.getValue());
			//entities.put(entry.getValue(), rEntity);
		}
	}

	public T getEntity(int playerId){
		readLock.tryLock();
		try{
			return keys.get(playerId);
		}finally{
			readLock.unlock();
		}
	}

	public RankEntity getRankEntity(int rank){
		readLock.tryLock();
		try{
			List<RankEntity> list = getOrderList();
			if(list == null || list.isEmpty()){
				return null;
			}
			if(rank < 0 || rank >= list.size()){
				throw new IndexOutOfBoundsException("the rank is invalid, rank=" + rank);
			}
			return list.get(rank);
		}finally{
			readLock.unlock();
		}
	}


	public int getRank(int playerId){
		T me = keys.get(playerId);
		if(me == null){
			return -1;
		}
		readLock.tryLock();
		try{
			int rank = 0;
			List<RankEntity> list = getOrderList();
			for(RankEntity re : list) {
				if(re.getPlayerId() == playerId){
					return rank + 1;
				}
				rank ++;
			}
			return -1;
		}finally{
			readLock.unlock();
		}
	}

	public int getPosition(int playerId){
		T me = keys.get(playerId);
		if(me == null){
			return -1;
		}
		readLock.tryLock();
		try{
			List<RankEntity> list = getOrderList();
			int left = 0;
			int right = list.size();
			while(left <= right){
				int mid = (left + right) >> 1;
				int result = me.compareTo(list.get(mid).getCa());
				if(result == 0){
					return mid;
				}else if(result > 0){
					right = mid - 1;
				}else{
					left = mid + 1;
				}
			}
			return -1;
		}finally{
			readLock.unlock();
		}
	}

	public List<RankEntity> getOrderList(){
		readLock.tryLock();
		try{
			if(orderList == null){
				orderList = new ArrayList<RankEntity>(entities.values());
			}
			return orderList;
		}finally{
			readLock.unlock();
		}
	}


	public void updateEntity(int playerId, T entity){
		writeLock.tryLock();
		try{
			boolean reset = true;
			T key = keys.get(playerId);
			if(key == null){
				if(keys.size() < maxCapacity){
					putEntity(playerId, entity);
				}else{
					T last = entities.lastKey();
					if(entity.compareTo(last) <= 0){
						putEntity(playerId, entity);
						RankEntity rEntity = entities.remove(last);
						keys.remove(rEntity.getPlayerId());
					}else{
						reset = false;
					}
				}
			}else{
				RankEntity rEntity = entities.get(key);
				if(entity.compareTo(rEntity.getCa()) <= 0){
					entities.remove(key);
					putEntity(playerId, entity);
				}else{
					reset = false;
				}
			}
			if(reset){
				orderList = null;
				updater.submit();
			}
		}finally{
			writeLock.unlock();
		}
	}

	public void clear(){
		orderList = null;
		keys.clear();
		entities.clear();
		updater.submit();
	}

	public boolean saveDb() {
		try {
			service.saveDB(type, CompressUtil.compressBytes(JsonUtils.map2String(keys).getBytes("utf-8")));
			return true;
		} catch (UnsupportedEncodingException e) {
			ServerLogger.err(e, "save rankinglist data fail");
		}
		return false;
	}

	public boolean isDirty(){
		return updater.dirty();
	}
}
