package com.game.params;

import java.util.List;
import java.util.ArrayList;

//试练(工具自动生成，请勿手动修改！）
public class TrainVo implements IProtocol {
	public List<Integer> ids;//已经通关关卡ID列表
	public List<Int2Param> rewards;//类型，剩余次数


	public void decode(BufferBuilder bb) {
		this.ids = bb.getIntList();
		
        if (bb.getNullFlag())
            this.rewards = null;
        else {
            int length = bb.getInt();
            this.rewards = new ArrayList<Int2Param>();
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
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.rewards.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putIntList(this.ids);
		bb.putProtocolVoList(this.rewards);
	}
}
