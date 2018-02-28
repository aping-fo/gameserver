package com.game.module.attach.training;

import java.util.List;

public class TrainOpponent {

	private int playerId;
	private String name;
	private int level;
	private int exp;
	private int vip;
	private int vipExp;
	private String gang;
	private int vocation;
	private int fashionId;
	private int weaponId;
	private int fight;
	private int title;
	private List<Integer> curSkills;// 当前技能
	private List<Integer> curCards;// 当前技能卡id

	public int getFight() {
		return fight;
	}

	public void setFight(int fight) {
		this.fight = fight;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getVocation() {
		return vocation;
	}

	public void setVocation(int vocation) {
		this.vocation = vocation;
	}

	public int getFashionId() {
		return fashionId;
	}

	public void setFashionId(int fashionId) {
		this.fashionId = fashionId;
	}

	public int getWeaponId() {
		return weaponId;
	}

	public void setWeaponId(int weaponId) {
		this.weaponId = weaponId;
	}

	public List<Integer> getCurSkills() {
		return curSkills;
	}

	public void setCurSkills(List<Integer> curSkills) {
		this.curSkills = curSkills;
	}

	public List<Integer> getCurCards() {
		return curCards;
	}

	public void setCurCards(List<Integer> curCards) {
		this.curCards = curCards;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getVipExp() {
		return vipExp;
	}

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}

	public String getGang() {
		return gang;
	}

	public void setGang(String gang) {
		this.gang = gang;
	}

}
