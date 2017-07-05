package com.game.module.mail;

import java.util.Date;
import java.util.Map;

import com.game.util.StringUtil;

public class Mail {

	public static final int UN_READ = 0;
	public static final int READED = 1;
	public static final int DEL = 2;

	private long id;
	private int senderId;
	private String senderName;
	private int receiveId;
	private String title;
	private String content;
	private Date sendTime;
	private int state;
	private int hasReward;
	private String rewards;
	private int type;

	private Map<Integer, Integer> rewardsMap;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public int getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(int receiveId) {
		this.receiveId = receiveId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		if (rewards == null || rewards.isEmpty()) {
			return;
		}
		this.rewardsMap = StringUtil.str2map(rewards, ";", ":");
		this.rewards = rewards;
	}

	public Map<Integer, Integer> getRewardsMap() {
		return rewardsMap;
	}

	public void setRewardsMap(Map<Integer, Integer> rewardsMap) {
		this.rewardsMap = rewardsMap;
	}

	public int getHasReward() {
		return hasReward;
	}

	public void setHasReward(int hasReward) {
		this.hasReward = hasReward;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
