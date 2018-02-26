package com.game.params.pet;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//宠物背包(工具自动生成，请勿手动修改！）
public class PetBagVO implements IProtocol {
	public List<PetVO> pets;//宠物列表
	public List<Int2Param> materials;//随便列表(id，数量)
	public int fightPetId;//出战宠物ID
	public int showPetId;//展示宠物ID
	public int showPetConfigId;//展示宠物ConfigId


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
            this.materials = null;
        else {
            int length = bb.getInt();
            this.materials = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.materials.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.materials.add(instance);
                }

            }
        }
		this.fightPetId = bb.getInt();
		this.showPetId = bb.getInt();
		this.showPetConfigId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.pets);
		bb.putProtocolVoList(this.materials);
		bb.putInt(this.fightPetId);
		bb.putInt(this.showPetId);
		bb.putInt(this.showPetConfigId);
	}
}
