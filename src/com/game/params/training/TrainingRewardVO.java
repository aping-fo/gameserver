package com.game.params.training;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//英雄试炼奖励(工具自动生成，请勿手动修改！）
public class TrainingRewardVO implements IProtocol {
	public int code;//错误码
	public int index;//箱子的索引
	public List<Reward> rewards;//关卡怪列表


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.index = bb.getInt();
		
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
		bb.putInt(this.code);
		bb.putInt(this.index);
		bb.putProtocolVoList(this.rewards);
	}
}
