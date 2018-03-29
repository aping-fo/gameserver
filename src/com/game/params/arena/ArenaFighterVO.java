package com.game.params.arena;

import java.util.List;
import com.game.params.*;

//竞技场玩家对象(工具自动生成，请勿手动修改！）
public class ArenaFighterVO implements IProtocol {
	public int code;//错误码
	public int uniqueId;//在竞技场中的唯一ID
	public int attack;//威力
	public int defense;//坚韧
	public int crit;//精准
	public int symptom;//症状
	public int fu;//符能
	public int hp;//血量
	public int lv;//等级
	public List<Integer> curSkills;//当前装载的技能[技能id,技能id,技能id,技能id]技能id为0表示该位置没有技能
	public List<Integer> curCards;//当前装载的技能卡[技能卡配置表id,技能id,技能id,技能id]技能id为0表示该位置没有技能卡
	public String name;//玩家名称
	public int vocation;//职业
	public List<Integer> bufferList;//套装buffer列表


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.uniqueId = bb.getInt();
		this.attack = bb.getInt();
		this.defense = bb.getInt();
		this.crit = bb.getInt();
		this.symptom = bb.getInt();
		this.fu = bb.getInt();
		this.hp = bb.getInt();
		this.lv = bb.getInt();
		this.curSkills = bb.getIntList();
		this.curCards = bb.getIntList();
		this.name = bb.getString();
		this.vocation = bb.getInt();
		this.bufferList = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.uniqueId);
		bb.putInt(this.attack);
		bb.putInt(this.defense);
		bb.putInt(this.crit);
		bb.putInt(this.symptom);
		bb.putInt(this.fu);
		bb.putInt(this.hp);
		bb.putInt(this.lv);
		bb.putIntList(this.curSkills);
		bb.putIntList(this.curCards);
		bb.putString(this.name);
		bb.putInt(this.vocation);
		bb.putIntList(this.bufferList);
	}
}
