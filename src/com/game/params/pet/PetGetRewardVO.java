package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//宠物活动领奖信息(工具自动生成，请勿手动修改！）
public class PetGetRewardVO implements IProtocol {
	public int errCode;//错误码
	public List<Reward> rewards;//奖励


	public void decode(BufferBuilder bb) {
		this.errCode = bb.getInt();
		
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
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.errCode);
		bb.putProtocolVoList(this.rewards);
	}
}
