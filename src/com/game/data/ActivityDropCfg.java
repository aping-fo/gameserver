package com.game.data;

/**
* h活动掉落表.xlsx(自动生成，请勿编辑！)
*/
public class ActivityDropCfg {
	public int id;//ID
	public int[] type;//副本类型
	public int TimeType;//开启时间类型
	public String BeginTime;//开始时间
	public String EndTime;//结束时间
	public int[] lev;//等级
	public int VipLev;//Vip等级
	public int count;//每日掉落次数上限
	public int[][] randomRewards;//随机奖励物品
	public int[] randomRates;//随机奖励概率
}