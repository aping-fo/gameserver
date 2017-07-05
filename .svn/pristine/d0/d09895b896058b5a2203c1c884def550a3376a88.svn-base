package com.game.params.task;

import com.game.params.*;

//任务数据(工具自动生成，请勿手动修改！）
public class STaskVo implements IProtocol {
	public int id;//任务id
	public int state;//状态1已接2完成3提交
	public int count;//完成的次数
	public boolean isJoint;//是否为被邀请任务


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.state = bb.getInt();
		this.count = bb.getInt();
		this.isJoint = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.state);
		bb.putInt(this.count);
		bb.putBoolean(this.isJoint);
	}
}
