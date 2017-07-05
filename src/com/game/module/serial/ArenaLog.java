package com.game.module.serial;
/**  
 * 竞技场日志
 */
public class ArenaLog {

	private String attacker;
	private String defender;
	private int rank;
	private int win;
	private int keep;
	private long time;
	
	public String getAttacker() {
		return attacker;
	}
	public void setAttacker(String attacker) {
		this.attacker = attacker;
	}
	public String getDefender() {
		return defender;
	}
	public void setDefender(String defender) {
		this.defender = defender;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getKeep() {
		return keep;
	}
	public void setKeep(int keep) {
		this.keep = keep;
	}
}
