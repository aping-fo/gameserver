package com.game.data;

/**
* h活动表.xlsx(自动生成，请勿编辑！)
*/
public class ActivityCfg {
	public int id;//活动ID
	public String name;//活动名称
	public int[][] Conds;//活动开启条件
	public int ActivityType;//活动类型
	public int OpenType;//开启类型
	public int TimeType;//时间类型
	public String BeginTime;//开始时间
	public String EndTime;//结束时间
	public int[] WeekTime;//周时间
	public int[] HourTime;//小时
}