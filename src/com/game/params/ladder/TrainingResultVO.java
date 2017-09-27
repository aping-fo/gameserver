package com.game.params.ladder;

import com.game.params.*;

//英雄试练结算VO(工具自动生成，请勿手动修改！）
public class TrainingResultVO implements IProtocol {
	public int index;//关卡索引
	public int hp;//血量百分比
	public boolean victory;//胜利失败


	public void decode(BufferBuilder bb) {
		this.index = bb.getInt();
		this.hp = bb.getInt();
		this.victory = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.index);
		bb.putInt(this.hp);
		bb.putBoolean(this.victory);
	}
}
