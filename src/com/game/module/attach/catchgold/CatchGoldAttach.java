package com.game.module.attach.catchgold;

import com.game.module.attach.Attach;

/**
 * 金币活动 结构
 */
public class CatchGoldAttach extends Attach {

	private int challenge;
	private int buyTime;
	private long lastChallengeTime;

	public CatchGoldAttach() {
	}

	public CatchGoldAttach(int playerId, byte type) {
		super(playerId, type);
	}

	public int getChallenge() {
		return challenge;
	}

	public void setChallenge(int challenge) {
		this.challenge = challenge;
	}
	
	public void alterChallenge(int value){
		this.challenge += value;
	}

	public int getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(int buyTime) {
		this.buyTime = buyTime;
	}
	
	public void addBuyTime(){
		this.buyTime++;
	}

	public long getLastChallengeTime() {
		return lastChallengeTime;
	}

	public void setLastChallengeTime(long lastChallengeTime) {
		this.lastChallengeTime = lastChallengeTime;
	}

}
