package com.game.data;
import java.util.Map;
/**
* g公会副本.xlsx(自动生成，请勿编辑！)
*/
public class GangCopyCfg {
	public int id;//ID(第几层)
	public int copyId;//副本表中的ID
	public String name;//副本名字
	public int needCredit;//开启消耗
	public int[] hurtRewards;//损血奖励
	public Map<Integer,int[][]> progressRewards;//进度奖励
	public int[] progress;//进度
}