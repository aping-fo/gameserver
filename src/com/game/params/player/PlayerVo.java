package com.game.params.player;

import java.util.List;
import com.game.params.*;

//角色信息（后端返回)(工具自动生成，请勿手动修改！）
public class PlayerVo implements IProtocol {
	public int code;//错误码
	public int playerId;//角色id
	public String name;//角色名
	public int sceneId;//场景id
	public float x;//x坐标点
	public float y;//y坐标点
	public float z;//z坐标点
	public int attack;//攻击
	public int defense;//防守
	public int crit;//暴击
	public int symptom;//症状
	public int fu;//符能
	public int hp;//血量
	public int lev;//等级
	public int vocation;//职业
	public int sex;//性别
	public int curHp;//当前血量
	public int curMp;//当前魔法值
	public int exp;//经验值
	public int coin;//金币
	public int diamond;//钻石
	public int chargeDiamond;//已经充值的钻石
	public int vip;//vip等级
	public int energy;//体力
	public int serverId;//服务器id
	public int fight;//战斗力
	public int fashionId;//时装id
	public String gang;//所在公会名称
	public int gangId;//所在公会id
	public int weapon;//武器
	public boolean banChat;//禁言中
	public List<Integer> curSkills;//当前装载的技能[技能id,技能id,技能id,技能id]技能id为0表示该位置没有技能
	public List<Integer> curCards;//当前装载的技能卡[技能卡配置表id,技能id,技能id,技能id]技能id为0表示该位置没有技能卡
	public List<Integer> fashions;//人物拥有的时装
	public int serialNum;//序列号
	public int key;//密钥
	public int guideId;//指引id
	public int title;//当前称号
	public int openDays;//开服天数
	public long regTime;//创建时间
	public int sign;//签名
	public int head;//头部
	public int signDay;//签到天数
	public int signFlag;//当前天是否已签到,0表示未签到，1表示已签到
	public List<Integer> modules;//已经开启的功能id,相应功能若是在场景中特效是否播放（0播 1不播）
	public List<Integer> newHandleSteps;//引导步骤
	public String userName;//渠道唯一标志
	public String serverName;//区服名
	public int onlineTime;//在线时长(秒)
	public int gatewayId;//gatewayId


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.sceneId = bb.getInt();
		this.x = bb.getFloat();
		this.y = bb.getFloat();
		this.z = bb.getFloat();
		this.attack = bb.getInt();
		this.defense = bb.getInt();
		this.crit = bb.getInt();
		this.symptom = bb.getInt();
		this.fu = bb.getInt();
		this.hp = bb.getInt();
		this.lev = bb.getInt();
		this.vocation = bb.getInt();
		this.sex = bb.getInt();
		this.curHp = bb.getInt();
		this.curMp = bb.getInt();
		this.exp = bb.getInt();
		this.coin = bb.getInt();
		this.diamond = bb.getInt();
		this.chargeDiamond = bb.getInt();
		this.vip = bb.getInt();
		this.energy = bb.getInt();
		this.serverId = bb.getInt();
		this.fight = bb.getInt();
		this.fashionId = bb.getInt();
		this.gang = bb.getString();
		this.gangId = bb.getInt();
		this.weapon = bb.getInt();
		this.banChat = bb.getBoolean();
		this.curSkills = bb.getIntList();
		this.curCards = bb.getIntList();
		this.fashions = bb.getIntList();
		this.serialNum = bb.getInt();
		this.key = bb.getInt();
		this.guideId = bb.getInt();
		this.title = bb.getInt();
		this.openDays = bb.getInt();
		this.regTime = bb.getLong();
		this.sign = bb.getInt();
		this.head = bb.getInt();
		this.signDay = bb.getInt();
		this.signFlag = bb.getInt();
		this.modules = bb.getIntList();
		this.newHandleSteps = bb.getIntList();
		this.userName = bb.getString();
		this.serverName = bb.getString();
		this.onlineTime = bb.getInt();
		this.gatewayId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.sceneId);
		bb.putFloat(this.x);
		bb.putFloat(this.y);
		bb.putFloat(this.z);
		bb.putInt(this.attack);
		bb.putInt(this.defense);
		bb.putInt(this.crit);
		bb.putInt(this.symptom);
		bb.putInt(this.fu);
		bb.putInt(this.hp);
		bb.putInt(this.lev);
		bb.putInt(this.vocation);
		bb.putInt(this.sex);
		bb.putInt(this.curHp);
		bb.putInt(this.curMp);
		bb.putInt(this.exp);
		bb.putInt(this.coin);
		bb.putInt(this.diamond);
		bb.putInt(this.chargeDiamond);
		bb.putInt(this.vip);
		bb.putInt(this.energy);
		bb.putInt(this.serverId);
		bb.putInt(this.fight);
		bb.putInt(this.fashionId);
		bb.putString(this.gang);
		bb.putInt(this.gangId);
		bb.putInt(this.weapon);
		bb.putBoolean(this.banChat);
		bb.putIntList(this.curSkills);
		bb.putIntList(this.curCards);
		bb.putIntList(this.fashions);
		bb.putInt(this.serialNum);
		bb.putInt(this.key);
		bb.putInt(this.guideId);
		bb.putInt(this.title);
		bb.putInt(this.openDays);
		bb.putLong(this.regTime);
		bb.putInt(this.sign);
		bb.putInt(this.head);
		bb.putInt(this.signDay);
		bb.putInt(this.signFlag);
		bb.putIntList(this.modules);
		bb.putIntList(this.newHandleSteps);
		bb.putString(this.userName);
		bb.putString(this.serverName);
		bb.putInt(this.onlineTime);
		bb.putInt(this.gatewayId);
	}
}
