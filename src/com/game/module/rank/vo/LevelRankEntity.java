package com.game.module.rank.vo;

import com.game.module.rank.AbstractRankCA;

public class LevelRankEntity extends AbstractRankCA<LevelRankEntity> {

	private int level;
	private int exp;

	public LevelRankEntity() {
	}

	public LevelRankEntity(int level, int exp) {
		this.level = level;
		this.exp = exp;
	}

	@Override
	public int compareTo(LevelRankEntity t) {
		if(t.level == level){
			return t.exp - exp;
		}
		return t.level - level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
