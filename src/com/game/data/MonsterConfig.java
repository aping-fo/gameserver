package com.game.data;
import java.util.Map;
/**
* g怪物表.xlsx(自动生成，请勿编辑！)
*/
public class MonsterConfig {
	public int id;//key
	public int model;//模型
	public int type;//类型
	public int AI;//主机ai
	public int AppointAI;//托管ai
	public String icon;//图标
	public String name;//名称
	public String desc;//描述
	public float scale;//放大倍数
	public int lev;//等级
	public boolean showName;//在多人场景中是否显示名字
	public int hp;//体力
	public int physicAttack;//威力
	public int physicDefense;//坚韧
	public int crit;//精准
	public int symptom;//症状
	public Map<Integer,float[]> resistanceProps;//抵御抗性
	public float logicBaseSpeed;//基础逻辑速度
	public int[] dropGoods;//掉落包
	public boolean banStrickenFly;//能否被击飞
	public int lyingDownBuffId;//倒地后触发保护buff id
	public boolean forbitCameraShake;//攻击目标是否禁用镜头震动
	public boolean hasDeadSkill;//是否拥有死亡技能
	public int deadRocketID;//死亡技能导弹ID
	public int duelCameraAnimID;//死亡镜头动画ID
	public float[] hurtProtectParam;//血量保护参数
	public float[] controlProtectParam;//控制保护参数
	public int[] skillId;//技能列表
	public int[] skillIdEx;//技能连招列表
	public int[] behitAction;//受击
	public int[] flyAction;//击飞
	public int[] downAction;//击落
	public int[] getupAction;//起身
	public float[] hudOffset;//hud偏移值
	public int[] bornBuff;//出生buff列表
	public Map<Integer,Integer> skillBuff;//技能附带buff
	public int frozonBuff;//冰冻触发buff
	public int giddyBuff;//眩晕触发buff
	public int fossilBuff;//石化触发buff
	public int poisonBuff;//中毒触发buff
	public int flareBuff;//灼烧触发buff
	public int bloodedBuff;//流血触发buff
	public int[] leadawayParam;//娃娃机专用参数
	public int isPosAboveGround;//是否限制位置在地面以上
}