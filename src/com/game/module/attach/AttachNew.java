package com.game.module.attach;

import com.game.util.JsonUtils;

public class AttachNew  {
	private int playerId;
	private byte type;
	private String extraInfo;

	final Attach wrap(Class<? extends Attach> clazz){
		Attach attach = JsonUtils.string2Object(extraInfo, clazz);
		attach.setPlayerId(this.playerId);
		attach.setType(this.type);
		return attach;
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

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
