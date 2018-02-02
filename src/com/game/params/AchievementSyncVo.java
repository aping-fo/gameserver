package com.game.params;

import java.util.List;

//称号同步(工具自动生成，请勿手动修改！）
public class AchievementSyncVo implements IProtocol {
	public int finishType;//完成类型
	public List<Integer> argsList;//参数


	public void decode(BufferBuilder bb) {
		this.finishType = bb.getInt();
		this.argsList = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.finishType);
		bb.putIntList(this.argsList);
	}
}
