package com.game.data;

/**
* w物品配置表.xlsx(自动生成，请勿编辑！)
*/
public class GoodsConfig {
	public int id;//物品id
	public String atlas;//图集
	public String name;//名称
	public int[][] decompose;//分解获得
	public int[] copys;//产出副本ID
	public int buyPrice;//购买价格
	public int sellPrice;//出售价格
	public int type;//物品类型
	public int level;//等级
	public int vocation;//职业要求
	public int color;//物品品质
	public int maxStack;//可叠加数量
	public int[] contentsRates;//获得物品权重
	public int[][] contents;//使用后获得物品
	public int hp;//生命
	public int attack;//威力
	public int defense;//坚韧
	public int symptom;//症状
	public int crit;//精准
	public int fu;//符能
	public int[] param1;//参数1
	public int[][] param2;//参数2
	public int rare;//稀缺
}