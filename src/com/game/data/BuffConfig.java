package com.game.data;

/**
* Buff配置表.xlsx(自动生成，请勿编辑！)
*/
public class BuffConfig {
	public int id;//ID
	public int buffConflict;//关系表ID
	public int buffTick;//BuffTick类型
	public int buffType;//Buff类型
	public int buffSubType;//Buff子类型
	public String name;//名称
	public String description;//描述
	public float duration;//状态时间
	public float[] takeEffectParam;//生效参数
	public boolean showTips;//是否显示提示
	public int icon;//图标
	public int attachEffect;//附加特效
	public int overlayType;//叠加类型
	public int addMaxCount;//叠加最大限制次数
	public float[] addTimeParas;//时间叠加参数
	public float[] addEfectParas;//作用叠加参数
	public float[] conflictType;//互拆类型
	public int[] addEfectIndex;//作用叠加索引
	public int[][] extraEffectParam;//额外效果参数
}