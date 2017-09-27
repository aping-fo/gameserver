package com.game.params.scene;

import com.game.params.*;

//场景玩家信息(工具自动生成，请勿手动修改！）
public class SScenePlayerVo implements IProtocol {
	public int playerId;//玩家id
	public String name;//角色名
	public int lev;//等级
	public float x;//坐标点x
	public float z;//坐标点z
	public int hMoveDir;//水平移动方向
	public int vMoveDir;//垂直移动方向
	public int sex;//性别
	public int vocation;//职业
	public int curHp;//当前血量
	public int hp;//总血量
	public int fashionId;//时装id
	public int weapon;//武器
	public int fight;//战斗力
	public int vip;//vip
	public int title;//称号
	public int head;//头部
	public String gang;//公会
	public int roomTeam;//组队玩法队伍ID


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.lev = bb.getInt();
		this.x = bb.getFloat();
		this.z = bb.getFloat();
		this.hMoveDir = bb.getInt();
		this.vMoveDir = bb.getInt();
		this.sex = bb.getInt();
		this.vocation = bb.getInt();
		this.curHp = bb.getInt();
		this.hp = bb.getInt();
		this.fashionId = bb.getInt();
		this.weapon = bb.getInt();
		this.fight = bb.getInt();
		this.vip = bb.getInt();
		this.title = bb.getInt();
		this.head = bb.getInt();
		this.gang = bb.getString();
		this.roomTeam = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.lev);
		bb.putFloat(this.x);
		bb.putFloat(this.z);
		bb.putInt(this.hMoveDir);
		bb.putInt(this.vMoveDir);
		bb.putInt(this.sex);
		bb.putInt(this.vocation);
		bb.putInt(this.curHp);
		bb.putInt(this.hp);
		bb.putInt(this.fashionId);
		bb.putInt(this.weapon);
		bb.putInt(this.fight);
		bb.putInt(this.vip);
		bb.putInt(this.title);
		bb.putInt(this.head);
		bb.putString(this.gang);
		bb.putInt(this.roomTeam);
	}
}
