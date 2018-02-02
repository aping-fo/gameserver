package com.game.module.rank.vo;

import com.game.module.rank.AbstractRankCA;

public class AchievementRankEntity extends AbstractRankCA<AchievementRankEntity> {

	private int achievement;

	public AchievementRankEntity(int achievement) {
		this.achievement = achievement;
	}

	public AchievementRankEntity() {
	}

	@Override
	public int compareTo(AchievementRankEntity t) {
		return t.achievement - achievement;
	}

	public int getAchievement() {
		return achievement;
	}

	public void setAchievement(int achievement) {
		this.achievement = achievement;
	}

}
