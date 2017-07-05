package com.game.module.rank.vo;

import com.game.module.rank.AbstractRankCA;

public class FightingRankEntity extends AbstractRankCA<FightingRankEntity> {

	private int fight;

	public FightingRankEntity(int fight) {
		this.fight = fight;
	}

	public FightingRankEntity() {
	}

	@Override
	public int compareTo(FightingRankEntity t) {
		return t.fight - fight;
	}

	public int getFight() {
		return fight;
	}

	public void setFight(int fight) {
		this.fight = fight;
	}

}
