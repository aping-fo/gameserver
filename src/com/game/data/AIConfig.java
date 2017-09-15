package com.game.data;

/**
* AI配置表.xlsx(自动生成，请勿编辑！)
*/
public class AIConfig {
	public int id;//key
	public String url;//AI路径
	public int AiStateLogicId;//Ai逻辑类型
	public float[] AlertThreshold;//警戒范围
	public float[] BattleThreshold;//战斗范围
	public float InAlert_PursuitRage;//待机状态下警戒范围内触发战斗状态几率范围
	public float OutAlert_PursuitRage;//待机状态下警戒范围外触发战斗状态几率范围
}