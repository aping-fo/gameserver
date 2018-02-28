package com.game.params.training;

import java.util.List;
import com.game.params.*;

//关卡怪信息(工具自动生成，请勿手动修改！）
public class TrainOpponentVO implements IProtocol {
	public int playerId;//玩家ID
	public String name;//玩家名称
	public int level;//等级
	public int exp;//经验
	public int vip;//VIP等级
	public int vipExp;//VIP经验
	public int vocation;//职业
	public int fashionId;//时装ID
	public int weapon;//武器
	public List<Integer> curSkills;//当前装载的技能[技能id,技能id,技能id,技能id]技能id为0表示该位置没有技能
	public List<Integer> curCards;//当前装载的技能卡[技能卡配置表id,技能id,技能id,技能id]技能id为0表示该位置没有技能卡
	public int head;//头部
	public String gang;//公会
	public int zhanli;//战斗力
	public int chenghao;//称号ID


	public void decode(BufferBuilder bb) {
		this.playerId = bb.getInt();
		this.name = bb.getString();
		this.level = bb.getInt();
		this.exp = bb.getInt();
		this.vip = bb.getInt();
		this.vipExp = bb.getInt();
		this.vocation = bb.getInt();
		this.fashionId = bb.getInt();
		this.weapon = bb.getInt();
		this.curSkills = bb.getIntList();
		this.curCards = bb.getIntList();
		this.head = bb.getInt();
		this.gang = bb.getString();
		this.zhanli = bb.getInt();
		this.chenghao = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.playerId);
		bb.putString(this.name);
		bb.putInt(this.level);
		bb.putInt(this.exp);
		bb.putInt(this.vip);
		bb.putInt(this.vipExp);
		bb.putInt(this.vocation);
		bb.putInt(this.fashionId);
		bb.putInt(this.weapon);
		bb.putIntList(this.curSkills);
		bb.putIntList(this.curCards);
		bb.putInt(this.head);
		bb.putString(this.gang);
		bb.putInt(this.zhanli);
		bb.putInt(this.chenghao);
	}
}
