package com.game.data;
import java.util.Map;
/**
* c充值表.xlsx(自动生成，请勿编辑！)
*/
public class ChargeConfig {
	public int id;//id
	public int type;//充值类型
	public int group;//充值组
	public int limit;//限购次数
	public float rmb;//充值金额(rmb)
	public float us;//充值金额(us)
	public int total;//获得充值礼包金额（钻石）
	public int add;//额外赠送金额（钻石）
	public int[][] weekMonthCard;//周卡月卡领取钻石
	public int platformType;//平台类型
	public String platformId;//平台id
	public Map<Integer,Integer> funds;//领取基金
	public String atlas;//图集
	public String icon;//图标
	public String name;//名字
	public String name_en;//名字
	public int[][] rewards;//奖励
	public int shopGoodsId;//商城物品id
	public int isShow;//是否显示
	public int platform;//平台
	public String chsId;//国内版物品id
	public String google_chsId;//谷歌中文版物品id
	public String google_engId;//谷歌英文版物品id
	public String ios_Id;//ios物品id
	public int activityId;//活动id
	public int vipExpAdd;//vip经验加成
}