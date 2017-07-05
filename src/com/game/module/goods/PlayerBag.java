package com.game.module.goods;

import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlayerBag {
	
	private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private ConcurrentHashMap<Long, Goods> allGoods = new ConcurrentHashMap<Long, Goods>();
	
	public void setAllGoods(ConcurrentHashMap<Long, Goods> allGoods) {
		this.allGoods = allGoods;
	}

	public ConcurrentHashMap<Long, Goods> getAllGoods() {
		return allGoods;
	}
	
	@JsonIgnore
	public synchronized long nextId(){
		id++;
		return id;
	}
}
