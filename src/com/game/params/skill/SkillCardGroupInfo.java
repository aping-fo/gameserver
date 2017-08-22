package com.game.params.skill;

import java.util.List;
import com.game.params.*;

//技能卡组信息(工具自动生成，请勿手动修改！）
public class SkillCardGroupInfo implements IProtocol {
	public int curGroupId;//当前技能卡组ID,从0开始
	public List<Integer> curCards;//当前装载的技能卡id(是自增长id）


	public void decode(BufferBuilder bb) {
		this.curGroupId = bb.getInt();
		this.curCards = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.curGroupId);
		bb.putIntList(this.curCards);
	}
}
