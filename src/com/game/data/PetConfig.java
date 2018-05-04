package com.game.data;

/**
* c宠物表.xlsx(自动生成，请勿编辑！)
*/
public class PetConfig {
	public int id;//key
	public int type;//类型
	public int quality;//品质
	public String name;//名字
	public int property;//属性
	public int monsterId;//怪物ID
	public int petId;//对应宠物ID
	public int materialId;//对应碎片ID
	public int nextQualityId;//下一品质id
	public int[][] upgradeCost;//进化消耗材料
	public int upgradeRate;//进化成功率
	public int gainNeedMaterialCount;//召唤宠物需要碎片数量
	public int passiveSkillId;//被动技能id
	public int hp;//生命加成
	public int hpFix;//生命固定加成
	public int attack;//威力加成
	public int attackFix;//威力固定加成
	public int defense;//防御加成
	public int defenseFix;//防御固定加成
	public int crit;//暴击加成
	public int critFix;//暴击固定加成
	public int symptom;//异常加成
	public int symptomFix;//异常固定加成
	public int fu;//抗性加成
	public int fuFix;//抗性固定加成
	public float scale;//缩放比例
	public float rotate;//旋转角度
}