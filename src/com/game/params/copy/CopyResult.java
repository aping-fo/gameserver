package com.game.params.copy;

import com.game.params.BufferBuilder;
import com.game.params.IProtocol;
import com.game.params.Reward;

import java.util.ArrayList;
import java.util.List;

//副本通关结果(工具自动生成，请勿手动修改！）
public class CopyResult implements IProtocol {
	public int id;//副本id
	public int star;//星级
	public int hp;//剩余血量
	public int time;//通关时间
	public int combo;//连击
	public List<Reward> rewards;//奖励
	public int passTime;//最快通关时间
	public String name;//最快通关人
	public int code;//错误码
	public int hitCount;//受击
	public int score;//得分
	public boolean showMystery;//是否触发了神秘商店


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.star = bb.getInt();
		this.hp = bb.getInt();
		this.time = bb.getInt();
		this.combo = bb.getInt();
		
        if (bb.getNullFlag())
            this.rewards = null;
        else {
            int length = bb.getInt();
            this.rewards = new ArrayList<Reward>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.rewards.add(null);
                }
                else
                {
                    Reward instance = new Reward();
                    instance.decode(bb);
                    this.rewards.add(instance);
                }

            }
        }
		this.passTime = bb.getInt();
		this.name = bb.getString();
		this.code = bb.getInt();
		this.hitCount = bb.getInt();
		this.score = bb.getInt();
		this.showMystery = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.star);
		bb.putInt(this.hp);
		bb.putInt(this.time);
		bb.putInt(this.combo);
		bb.putProtocolVoList(this.rewards);
		bb.putInt(this.passTime);
		bb.putString(this.name);
		bb.putInt(this.code);
		bb.putInt(this.hitCount);
		bb.putInt(this.score);
		bb.putBoolean(this.showMystery);
	}
}
