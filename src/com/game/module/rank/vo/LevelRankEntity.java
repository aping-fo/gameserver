package com.game.module.rank.vo;

import com.game.module.rank.AbstractRankCA;

public class LevelRankEntity extends AbstractRankCA<LevelRankEntity> {

	private int level;
	private int exp;
    private float coins;

	public LevelRankEntity() {
	}

    public LevelRankEntity(int level, int exp, float coins) {
        this.level = level;
        this.exp = exp;
        this.coins = coins;
    }

	public int compareTo(LevelRankEntity t) {
		if(t.coins == coins){
			return t.level - level;
		}
		return (int) (t.coins - coins);
	}

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public float getCoins() {
        return coins;
    }

    public void setCoins(float coins) {
        this.coins = coins;
    }

    public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
