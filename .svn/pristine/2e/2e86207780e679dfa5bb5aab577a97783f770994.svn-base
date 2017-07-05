package com.game.params.goods;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//装备信息(工具自动生成，请勿手动修改！）
public class EquipInfo implements IProtocol {
	public List<AttrItem> strengths;//部位强化信息[{部位,强化等级}}
	public List<Jewel> jewels;//宝石信息


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.strengths = null;
        else {
            int length = bb.getInt();
            this.strengths = new ArrayList<AttrItem>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.strengths.add(null);
                }
                else
                {
                    AttrItem instance = new AttrItem();
                    instance.decode(bb);
                    this.strengths.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.jewels = null;
        else {
            int length = bb.getInt();
            this.jewels = new ArrayList<Jewel>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.jewels.add(null);
                }
                else
                {
                    Jewel instance = new Jewel();
                    instance.decode(bb);
                    this.jewels.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.strengths);
		bb.putProtocolVoList(this.jewels);
	}
}
