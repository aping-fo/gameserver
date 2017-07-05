package com.game.params;

import java.util.List;
import java.util.ArrayList;

//商城数据(工具自动生成，请勿手动修改！）
public class ShopInfo implements IProtocol {
	public int type;//商店类型
	public List<Integer> refreshShopIds;//刷新出的商品id
	public int refreshCount;//当天刷新的次数
	public List<Int2Param> limitShops;//限购商品的购买记录List[{id，数量}]


	public void decode(BufferBuilder bb) {
		this.type = bb.getInt();
		this.refreshShopIds = bb.getIntList();
		this.refreshCount = bb.getInt();
		
        if (bb.getNullFlag())
            this.limitShops = null;
        else {
            int length = bb.getInt();
            this.limitShops = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.limitShops.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.limitShops.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.type);
		bb.putIntList(this.refreshShopIds);
		bb.putInt(this.refreshCount);
		bb.putProtocolVoList(this.limitShops);
	}
}
