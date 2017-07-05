package com.game.module.skill;
/**  
 * 技能卡
 */
public class SkillCard {

	public static final int SPECIAL = 99;
	
	private int cardId;
	private int exp;
	private int lev;
	
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getLev() {
		return lev;
	}
	public void setLev(int lev) {
		this.lev = lev;
	}
	public int getCardId() {
		return cardId;
	}
	public void setCardId(int cardId) {
		this.cardId = cardId;
	}
}
