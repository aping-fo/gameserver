package com.game.module.rank.vo;

import com.game.module.rank.AbstractRankCA;

public class EndlessRankEntity extends AbstractRankCA<EndlessRankEntity> {

	private int layer;
	private int time;

	public EndlessRankEntity() {
	}

	public EndlessRankEntity(int layer, int time) {
		this.layer = layer;
		this.time = time;
	}

	@Override
	public int compareTo(EndlessRankEntity entity) {
		if (layer == entity.layer) {
			return time - entity.time;
		}
		return entity.layer - layer;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
