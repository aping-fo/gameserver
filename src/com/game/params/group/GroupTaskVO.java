package com.game.params.group;

import com.game.params.*;

//团队副本任务(工具自动生成，请勿手动修改！）
public class GroupTaskVO implements IProtocol {
	public int id;//任务ID
	public int count;//完成次数


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.count = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.count);
	}
}
