package com.game.params;

import java.util.List;
import java.util.ArrayList;

//时装列表信息(工具自动生成，请勿手动修改！）
public class FashionInfo implements IProtocol {
	public List<FashionVO> fashions;//所有时装
	public int cloth;//当前穿戴的衣服
	public int weapon;//武器
	public int head;//头部


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.fashions = null;
        else {
            int length = bb.getInt();
            this.fashions = new ArrayList<FashionVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.fashions.add(null);
                }
                else
                {
                    FashionVO instance = new FashionVO();
                    instance.decode(bb);
                    this.fashions.add(instance);
                }

            }
        }
		this.cloth = bb.getInt();
		this.weapon = bb.getInt();
		this.head = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.fashions);
		bb.putInt(this.cloth);
		bb.putInt(this.weapon);
		bb.putInt(this.head);
	}
}
