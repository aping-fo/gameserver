package com.game.params.worldboss;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//世界BOSS奖励(工具自动生成，请勿手动修改！）
public class WorldBossReward implements IProtocol {
	public List<Reward> hurtReward;//伤害奖励
	public List<Reward> rankReward;//排名奖励


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.hurtReward = null;
        else {
            int length = bb.getInt();
            this.hurtReward = new ArrayList<Reward>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.hurtReward.add(null);
                }
                else
                {
                    Reward instance = new Reward();
                    instance.decode(bb);
                    this.hurtReward.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.rankReward = null;
        else {
            int length = bb.getInt();
            this.rankReward = new ArrayList<Reward>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.rankReward.add(null);
                }
                else
                {
                    Reward instance = new Reward();
                    instance.decode(bb);
                    this.rankReward.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.hurtReward);
		bb.putProtocolVoList(this.rankReward);
	}
}
