package com.game.data;

/**
* g怪物刷新表.xlsx(自动生成，请勿编辑！)
*/
public class MonsterRefreshConfig {
	public int id;//key
	public int copyId;//副本id
	public int difficulty;//难度模式
	public int group;//小关卡数
	public int wave;//批次
	public int monsterId;//怪物id
	public float[] beginPoint;//起始坐标
	public float[] targetPoint;//目标坐标
	public boolean hideBlood;//是否隐藏血条
	public boolean hideShadow;//是否隐藏阴影
	public boolean containAI;//是否带有简单AI
	public String animation;//出场动画
	public int appearType;//出场方式
	public float[] param;//参数
	public int showUpEffect;//出场特效
	public int showUpSound;//出场音效
	public int showUpDirectionType;//出场朝向类型
	public int showUpDirection;//出场朝向
	public int showUpSkill;//出场技能
	public boolean stayWaiting;//出场后是否保持等待
	public int[] bornBuff;//出生buff
	public int[] runEffect;//行走特效
}