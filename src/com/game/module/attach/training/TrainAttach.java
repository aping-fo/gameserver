package com.game.module.attach.training;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.game.module.attach.Attach;

public class TrainAttach extends Attach {

	private int index;// 当前关卡索引,从0开始
	private int hp;// 剩余血量百分比
	private Set<Integer> treasureBox = new HashSet<Integer>();// 可以用的箱子的索引
	private List<Integer> opponents;// 所有关卡的对手

	public TrainAttach() {
		super();
	}

	public TrainAttach(int playerId, byte type) {
		super(playerId, type);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public Set<Integer> getTreasureBox() {
		return treasureBox;
	}

	public void setTreasureBox(Set<Integer> treasureBox) {
		this.treasureBox = treasureBox;
	}

	public List<Integer> getOpponents() {
		return opponents;
	}

	public void setOpponents(List<Integer> opponents) {
		this.opponents = opponents;
	}

}
