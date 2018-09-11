package com.game.module.attach.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.module.attach.Attach;
import com.game.params.arena.ArenaReportVO;

public class ArenaAttach extends Attach {

	private int uniqueId;// 在竞技场中的唯一ID
	private int challenge;// 剩余挑战次数
	private int buyCount;// 购买次数
	private int record;// 战绩，大于0表示连胜，小于0表示连败
	private boolean isRevenge; //是否复仇
	@JsonIgnore
	private List<ArenaReportVO> report = new ArrayList<ArenaReportVO>();// 战报
	@JsonIgnore
	private int opponent;//对手的唯一ID
	@JsonIgnore
	private AtomicInteger page = new AtomicInteger(0);//查找页

	public ArenaAttach() {
		
	}

	public ArenaAttach(int playerId, byte type) {
		super(playerId, type);
	}

	public int getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
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

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	
	public void alterBuyCount(int value)
	{
		this.buyCount += value;
	}
	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public List<ArenaReportVO> getReport() {
		return report;
	}

	public void setReport(List<ArenaReportVO> report) {
		this.report = report;
	}

	public int getOpponent() {
		return opponent;
	}

	public void setOpponent(int opponent) {
		this.opponent = opponent;
	}

	public AtomicInteger getPage() {
		return page;
	}

	public boolean getIsRevenge() {
		return isRevenge;
	}

	public void setIsRevenge(boolean isRevenge) {
		this.isRevenge = isRevenge;
	}
}
