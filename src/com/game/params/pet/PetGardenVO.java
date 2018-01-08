package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//宠物家园信息(工具自动生成，请勿手动修改！）
public class PetGardenVO implements IProtocol {
	public List<Int2Param> activityCount;//[id,count]
	public List<PetActivityVO> activityList;//活动列表


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.activityCount = null;
        else {
            int length = bb.getInt();
            this.activityCount = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.activityCount.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.activityCount.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.activityList = null;
        else {
            int length = bb.getInt();
            this.activityList = new ArrayList<PetActivityVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.activityList.add(null);
                }
                else
                {
                    PetActivityVO instance = new PetActivityVO();
                    instance.decode(bb);
                    this.activityList.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.activityCount);
		bb.putProtocolVoList(this.activityList);
	}
}
