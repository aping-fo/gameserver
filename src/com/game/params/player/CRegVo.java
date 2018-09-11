package com.game.params.player;

import com.game.params.*;

//角色注册信息(工具自动生成，请勿手动修改！）
public class CRegVo implements IProtocol {
	public int vocation;//职业
	public int sex;//性别
	public String name;//角色名
	public String accName;//账号名称
	public String channel;//渠道
	public int version;//某些平台用于版本检测
	public int clientType;//客户端类型，手游：1 ios；2安卓；3 WP；4其它
	public String clientMac;//客户端主机MAC地址
	public String hardwarSn1;//客户端主机硬件序列号，如含有英文字母请做大写转换
	public String hardwarSn2;//客户端主机硬件序列号，如含有英文字母请做大写转换
	public String uddi;//记录移动设备的唯一识别码,如含有英文字母请做大写转换,如不使用或无法获取时请不要设置
	public String modelVersion;//设备型号相关信息
	public String ldid;//蓝港sdk所计算的客户端所在设备的唯一编号,未接入蓝港sdk则传空
	public String token;//认证加密串
	public String un;//合作运营方用来标识其用户的唯一ID，一般情况下为第三方的用户ID
	public String serverId;//服ID
	public String serverName;//区服名
	public int userId;//平台唯一ID
	public String thirdChannel;//合作运营方用第三方登录时登录渠道
	public String thirdUserId;//合作运营方用第三方登录时登录用户ID


	public void decode(BufferBuilder bb) {
		this.vocation = bb.getInt();
		this.sex = bb.getInt();
		this.name = bb.getString();
		this.accName = bb.getString();
		this.channel = bb.getString();
		this.version = bb.getInt();
		this.clientType = bb.getInt();
		this.clientMac = bb.getString();
		this.hardwarSn1 = bb.getString();
		this.hardwarSn2 = bb.getString();
		this.uddi = bb.getString();
		this.modelVersion = bb.getString();
		this.ldid = bb.getString();
		this.token = bb.getString();
		this.un = bb.getString();
		this.serverId = bb.getString();
		this.serverName = bb.getString();
		this.userId = bb.getInt();
		this.thirdChannel = bb.getString();
		this.thirdUserId = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.vocation);
		bb.putInt(this.sex);
		bb.putString(this.name);
		bb.putString(this.accName);
		bb.putString(this.channel);
		bb.putInt(this.version);
		bb.putInt(this.clientType);
		bb.putString(this.clientMac);
		bb.putString(this.hardwarSn1);
		bb.putString(this.hardwarSn2);
		bb.putString(this.uddi);
		bb.putString(this.modelVersion);
		bb.putString(this.ldid);
		bb.putString(this.token);
		bb.putString(this.un);
		bb.putString(this.serverId);
		bb.putString(this.serverName);
		bb.putInt(this.userId);
		bb.putString(this.thirdChannel);
		bb.putString(this.thirdUserId);
	}
}
