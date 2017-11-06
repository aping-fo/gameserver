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
	public int mutateMonsterId;//变异后对应怪物ID
	public int petId;//对应宠物ID
	public int materialId;//对应碎片ID
	public int nextQualityId;//下一品质id
	public int gainNeedMaterialCount;//召唤宠物需要碎片数量
	public int[] nextQualityCost;//升品消耗货币
	public int nextQualityMaterialCount;//升品或合成需要碎片数量
	public int[][] decomposeGoods;//分解产生物品id和数量
	public int[] sameMaterial;//拥有同样属性品质宠物时对应的碎片id和数量
	public int activeSkillId;//主动技能ID
	public int goodsId;//物品id
}