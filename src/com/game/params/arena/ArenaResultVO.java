package com.game.params.arena;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//挑战结算信息(工具自动生成，请勿手动修改！）
public class ArenaResultVO implements IProtocol {
	public int code;//错误码
	public int currRank;//当前名次
	public int record;//战绩，大于0表示连胜，小于0表示连败
	public List<Reward> rewards;//奖励


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.currRank = bb.getInt();
		this.record = bb.getInt();
		
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
		bb.putInt(this.currRank);
		bb.putInt(this.record);
		bb.putProtocolVoList(this.rewards);
	}
}
