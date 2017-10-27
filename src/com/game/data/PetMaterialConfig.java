package com.game.data;

/**
* c宠物碎片表.xlsx(自动生成，请勿编辑！)
*/
public class PetMaterialConfig {
	public int id;//key
	public int quality;//品质
	public String name;//名字
	public int type;//属性
	public int petId;//对应宠物ID
	public int nextQualityPieceId;//下一品质碎片id
	public int nextQualityPieceCount;//下一品质碎片合成所需数量
	public int[][] decomposeGoods;//分解产生的物品id和数量
	public int goodsId;//物品id
}