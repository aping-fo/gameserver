package com.game.module.attach;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.util.BeanManager;
import com.game.util.JsonUtils;
import com.server.syncdb.SyncObject;

public class Attach extends SyncObject {

	@JsonIgnore
	private int playerId;
	@JsonIgnore
	private byte type;
	@JsonIgnore
	private String extraInfo;
	@JsonIgnore
	private boolean insert;//是否需要插入到数据库中

	public Attach(){
		
	}
	
	public Attach(int playerId, byte type) {
		this();
		this.playerId = playerId;
		this.type = type;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}


	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}
	
	public final String getExtraInfo() {
		//System.out.println(JsonUtils.object2String(this));
		return  JsonUtils.object2String(this);
	}

	public final void setExtraInfo(String value) {
		this.extraInfo = value;
	}
	
	public final void setInsert(boolean value){
		insert = value;
	}

	final Attach wrap(Class<? extends Attach> clazz){
		Attach attach = JsonUtils.string2Object(extraInfo, clazz);
		attach.playerId = this.playerId;
		attach.type = this.type;
		return attach;
	}

	@Override
	protected boolean setCommit(boolean isCommit) {
		// TODO Auto-generated method stub
		return super.setCommit(isCommit);
	}

	@Override
	public void saveDb() {
		if(insert){
			BeanManager.getBean(AttachDao.class).insert(this);
			insert = false;
		}else{
			BeanManager.getBean(AttachDao.class).update(this);
		}
	}
	
	
}
