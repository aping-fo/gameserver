package com.game.params;

import java.util.ArrayList;
import java.util.List;

//副本扫荡奖励(工具自动生成，请勿手动修改！）
public class CopyReward implements IProtocol {
	public int code;//错误码
	public List<RewardList> reward;//副本奖励
	public boolean showMystery;//是否触发神秘商店


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		
        if (bb.getNullFlag())
            this.reward = null;
        else {
            int length = bb.getInt();
            this.reward = new ArrayList<RewardList>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.reward.add(null);
                }
                else
                {
                    RewardList instance = new RewardList();
                    instance.decode(bb);
                    this.reward.add(instance);
                }

            }
        }
		this.showMystery = bb.getBoolean();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putProtocolVoList(this.reward);
		bb.putBoolean(this.showMystery);
	}
}
