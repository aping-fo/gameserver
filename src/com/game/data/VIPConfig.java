package com.game.data;
import java.util.Map;
/**
* VIP表.xlsx(自动生成，请勿编辑！)
*/
public class VIPConfig {
	public int id;//VIP等级
	public int charge;//需要充值钻石数量
	public int price;//VIP礼包购买价格
	public int returnValue;//VIP显示价格
	public int[][] rewards;//VIP礼包
	public int[][] dailyRewards;//每日福利
	public int buyEnergy;//购买体力次数
	public int buyCoin;//购买金币的次数
	public int resetCopy;//重置困难副本次数
	public Map<Integer,Integer> shopRefreshTimes;//商店刷新次数
	public int mysteryProperty;//神秘商店出现概率加成百分值
	public int addBuy;//加购倍数
	public int arenaChallenge;//竞技场挑战次数购买
	public int buyTreasureCopy;//扭曲失控（金币）副本可购买次数
	public int buyLeadawayCopy;//顺手牵羊副本可购买次数
	public int buyExtremeEvasionCopy;//虚无空间（经验）副本可购买次数
	public int buyGoldCopy;//金币副本可购买次数
	public int traveringEnergy;//穿越仪能量上限
	public Map<Integer,Integer> vipGift;//礼包
	public int giftPrice;//礼包原价钱
	public int buyMainCopy;//困难噩梦副本可购买次数
}