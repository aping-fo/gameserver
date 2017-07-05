package com.game.module.attach.lottery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.module.attach.Attach;
import com.game.params.lottery.LotteryVO;

public class LotteryAttach extends Attach {

	private Map<Integer, LotteryVO> records = new ConcurrentHashMap<Integer, LotteryVO>();

	public LotteryAttach() {
	}

	public LotteryAttach(int playerId, byte type) {
		super(playerId, type);
	}

	public Map<Integer, LotteryVO> getRecords() {
		return records;
	}

	public void setRecords(Map<Integer, LotteryVO> records) {
		this.records = records;
	}
	
	
}
