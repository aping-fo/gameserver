package com.game.module.gang;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.module.gang.GangService.CloseTask;

public class GTRoom {

	private int id;
	private long createTime;
	private int max;//参与人数的历史最高记录
	@JsonIgnore
	private CloseTask closeTask;

	public GTRoom(){
		
	}
	
	public GTRoom(int id){
		this.id = id;
		this.createTime = System.currentTimeMillis();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public void addMax(){
		this.max++;
	}

	public CloseTask getCloseTask() {
		return closeTask;
	}

	public void setCloseTask(CloseTask closeTask) {
		this.closeTask = closeTask;
	}
}
