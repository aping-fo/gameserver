package com.game.module.attach.arena;

public class ArenaPlayer {

	private int rank;
	private int uniqueId;
	private int playerId;

	public ArenaPlayer() {
	}

	public ArenaPlayer(int uniqueId, int playerId,int rank) {
		this.uniqueId = uniqueId;
		this.playerId = playerId;
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

}
