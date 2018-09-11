package com.game.module.attach.experience;

import com.game.module.attach.Attach;

public class ExperienceAttach extends Attach {

	private int challenge;
	private int buyTime;
	private long lastChallengeTime;

	public ExperienceAttach() {
	}

	public ExperienceAttach(int playerId, byte type) {
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
	
	public void addBuyTime(int value){
		this.buyTime += value;
	}

	public long getLastChallengeTime() {
		return lastChallengeTime;
	}

	public void setLastChallengeTime(long lastChallengeTime) {
		this.lastChallengeTime = lastChallengeTime;
	}

}
