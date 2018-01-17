package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//宠物玩法请求参数(工具自动生成，请勿手动修改！）
public class StartPetActivityVO implements IProtocol {
	public List<PetPlayData> petActivitys;//开始活动列表[活动ID，宠物ID]


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.petActivitys = null;
        else {
            int length = bb.getInt();
            this.petActivitys = new ArrayList<PetPlayData>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.petActivitys.add(null);
                }
                else
                {
                    PetPlayData instance = new PetPlayData();
                    instance.decode(bb);
                    this.petActivitys.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.petActivitys);
	}
}
