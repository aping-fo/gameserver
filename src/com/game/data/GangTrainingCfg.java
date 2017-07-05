package com.game.data;

/**
* g公会练功房.xlsx(自动生成，请勿编辑！)
*/
public class GangTrainingCfg {
	public int id;//练功房ID
	public String name;//练功房名字
	public int reqLev;//练功房开启等级
	public int assetConsume;//公会资金消耗
	public int[][] itemConsume;//开启者物品消耗
	public int maxTime;//练功房开放时长(分)
	public int[][] reward;//练功奖励（每小时）
	public int validTime;//有效时长（小时）
	public float[] rewardPlus;//多人加成
}