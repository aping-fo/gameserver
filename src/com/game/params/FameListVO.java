package com.game.params;

import java.util.List;
import java.util.ArrayList;

//声望信息(工具自动生成，请勿手动修改！）
public class FameListVO implements IProtocol {
	public List<FameVo> fames;//声望信息列表


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.fames = null;
        else {
            int length = bb.getInt();
            this.fames = new ArrayList<FameVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.fames.add(null);
                }
                else
                {
                    FameVo instance = new FameVo();
                    instance.decode(bb);
                    this.fames.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.fames);
	}
}
