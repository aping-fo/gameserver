package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//宠物背包差异更新(工具自动生成，请勿手动修改！）
public class UpdatePetBagVO implements IProtocol {
	public List<PetVO> pets;//宠物列表，有则更新，无则新增
	public List<Int2Param> updateIds;//增加(你背包里没有的),删除(数量为0)，更新 宠物或者碎片ID列表


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.pets = null;
        else {
            int length = bb.getInt();
            this.pets = new ArrayList<PetVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.pets.add(null);
                }
                else
                {
                    PetVO instance = new PetVO();
                    instance.decode(bb);
                    this.pets.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.updateIds = null;
        else {
            int length = bb.getInt();
            this.updateIds = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.updateIds.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.updateIds.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.pets);
		bb.putProtocolVoList(this.updateIds);
	}
}
