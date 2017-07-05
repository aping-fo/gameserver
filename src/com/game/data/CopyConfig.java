package com.game.data;
import java.util.Map;
/**
* f副本表.xlsx(自动生成，请勿编辑！)
*/
public class CopyConfig {
	public int id;//key
	public int cityId;//主城
	public int chapterId;//章节
	public String chapterName;//章节名称
	public int group;//关卡序号
	public String name;//名称
	public String desc;//关卡描述
	public int preId;//前置副本
	public int[] scenes;//关卡场景id
	public int type;//副本类型
	public int difficulty;//难度
	public int passType;//通关类型
	public String groupName;//阵营
	public int timeLimit;//时间限制
	public int needEnergy;//需要体力
	public int recommendFight;//推荐战力
	public int lev;//等级
	public int count;//次数
	public int[][] firstReward;//首次通关必掉
	public int[][] rewards;//奖励
	public int[][] reviveCost;//复活消耗
	public Map<Integer,int[][]> starRewards;//星级奖励
	public int[] randomRates;//随机奖励概率
	public int[][] randomRewards;//随机奖励物品
	public int getstarbytime;//得星条件1-时间限制
	public int getstarbyhit;//得星条件2-被击数
	public int isStopTimeWhenPause;//暂停时是否停止时间记时
	public int monsterId;//怪兽ID
	public String monsterIcon;//怪兽立绘
	public int[] qualities;//特性
	public int[] weakness;//弱点
	public int isWinTimeOver;//时间结束是胜利还是失败
	public int isIgnoreSkillClick;//是否屏蔽技能点击
	public int isClearMonsterWhenOver;//结束是否清理怪物
	public int trickTask;//触发任务
}