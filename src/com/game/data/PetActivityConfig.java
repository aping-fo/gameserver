package com.game.data;

/**
* c宠物玩法表.xlsx(自动生成，请勿编辑！)
*/
public class PetActivityConfig {
	public int id;//id
	public int type;//类型
	public String title;//标题
	public int level;//等级
	public int count;//该等级最多并行开启活动数
	public int finishSec;//完成时间（秒）
	public int plotNum;//槽数
	public int[] plotAcclerate;//槽数加速属性
	public int levelUpCondition;//升级条件
	public int[] finishCostPerMins;//立刻完成每分钟消耗
	public int[][] rewards;//固定奖励
	public int dropId;//随机奖励
	public int dropCount;//随机次数
	public int[] rate;//获取随机奖励权重
}