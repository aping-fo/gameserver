package com.game.data;

/**
* qQTE配置表.xlsx(自动生成，请勿编辑！)
*/
public class QTEConfig {
	public int id;//id
	public String desc;//描述
	public int eventType;//事件类型
	public String icon;//图标
	public int[] bloodLimit;//血量条件限制
	public int[] comobLimit;//连击数条件限制
	public int[] rateLimit;//几率限制
	public int skillId;//技能Id参数
	public float vaildTime;//有效响应时间(秒)
	public float lockTime;//事件触发后的冷却时间(秒)
}