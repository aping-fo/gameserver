package com.game.params.ladder;

import java.util.List;
import com.game.params.*;

//排位赛成员信息(工具自动生成，请勿手动修改！）
public class LadderMemberVO implements IProtocol {
	public String name;//名称
	public int vocation;//职业
	public int level;//等级
	public int ladderLevel;//排位等级
	public int team;//队伍
	public int playerId;//玩家ID
	public int isRobot;//是否是机器人，0：不是，1：是
	public int attack;//威力
	public int defense;//坚韧
	public int crit;//精准
	public int symptom;//症状
	public int fu;//符能
	public int hp;//血量
	public int lv;//等级
	public int head;//时装头
	public int fashionId;//时装衣服
	public int weapon;//时装武器
	public List<Integer> curSkills;//当前装载的技能[技能id,技能id,技能id,技能id]技能id为0表示该位置没有技能
	public List<Integer> curCards;//当前装载的技能卡[技能卡配置表id,技能id,技能id,技能id]技能id为0表示该位置没有技能卡
	public List<Integer> bufferList;//套装buffer列表


	public void decode(BufferBuilder bb) {
		this.name = bb.getString();
		this.vocation = bb.getInt();
		this.level = bb.getInt();
		this.ladderLevel = bb.getInt();
		this.team = bb.getInt();
		this.playerId = bb.getInt();
		this.isRobot = bb.getInt();
		this.attack = bb.getInt();
		this.defense = bb.getInt();
		this.crit = bb.getInt();
		this.symptom = bb.getInt();
		this.fu = bb.getInt();
		this.hp = bb.getInt();
		this.lv = bb.getInt();
		this.head = bb.getInt();
		this.fashionId = bb.getInt();
		this.weapon = bb.getInt();
		this.curSkills = bb.getIntList();
		this.curCards = bb.getIntList();
		this.bufferList = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.name);
		bb.putInt(this.vocation);
		bb.putInt(this.level);
		bb.putInt(this.ladderLevel);
		bb.putInt(this.team);
		bb.putInt(this.playerId);
		bb.putInt(this.isRobot);
		bb.putInt(this.attack);
		bb.putInt(this.defense);
		bb.putInt(this.crit);
		bb.putInt(this.symptom);
		bb.putInt(this.fu);
		bb.putInt(this.hp);
		bb.putInt(this.lv);
		bb.putInt(this.head);
		bb.putInt(this.fashionId);
		bb.putInt(this.weapon);
		bb.putIntList(this.curSkills);
		bb.putIntList(this.curCards);
		bb.putIntList(this.bufferList);
	}
}
