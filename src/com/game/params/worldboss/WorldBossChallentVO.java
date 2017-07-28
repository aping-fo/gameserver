package com.game.params.worldboss;

import com.game.params.*;

//世界boss挑战信息(工具自动生成，请勿手动修改！）
public class WorldBossChallentVO implements IProtocol {
	public int code;//错误码
	public int deadTime;//上次死亡时间
	public int attackBuyCount;//临时攻击力的购买次数
	public int copyId;//副本id
	public BossVo boss;//boss信息


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.deadTime = bb.getInt();
		this.attackBuyCount = bb.getInt();
		this.copyId = bb.getInt();
		
        if(bb.getNullFlag())
            this.boss = null;
        else
        {
            this.boss = new BossVo();
            this.boss.decode(bb);
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.deadTime);
		bb.putInt(this.attackBuyCount);
		bb.putInt(this.copyId);
		bb.putProtocolVo(this.boss);
	}
}
