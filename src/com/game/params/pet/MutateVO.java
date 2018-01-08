package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//变异请求参数(工具自动生成，请勿手动修改！）
public class MutateVO implements IProtocol {
	public int mutateID;//变异宠物ID
	public List<Int2Param> consume;//消耗材料列表
	public int itemId;//加成道具ID


	public void decode(BufferBuilder bb) {
		this.mutateID = bb.getInt();
		
        if (bb.getNullFlag())
            this.consume = null;
        else {
            int length = bb.getInt();
            this.consume = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.consume.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.consume.add(instance);
                }

            }
        }
		this.itemId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.mutateID);
		bb.putProtocolVoList(this.consume);
		bb.putInt(this.itemId);
	}
}
