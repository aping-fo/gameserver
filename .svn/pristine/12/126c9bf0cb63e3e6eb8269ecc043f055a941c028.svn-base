package com.game.params.gang;

import com.game.params.*;

//入帮限制(工具自动生成，请勿手动修改！）
public class GangLimit implements IProtocol {
	public boolean autoJoin;//自动加入
	public boolean levLimit;//等级限制
	public int level;//等级限制
	public boolean fightLimit;//战斗力限制
	public int fihgt;//战斗力限制


	public void decode(BufferBuilder bb) {
		this.autoJoin = bb.getBoolean();
		this.levLimit = bb.getBoolean();
		this.level = bb.getInt();
		this.fightLimit = bb.getBoolean();
		this.fihgt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putBoolean(this.autoJoin);
		bb.putBoolean(this.levLimit);
		bb.putInt(this.level);
		bb.putBoolean(this.fightLimit);
		bb.putInt(this.fihgt);
	}
}
