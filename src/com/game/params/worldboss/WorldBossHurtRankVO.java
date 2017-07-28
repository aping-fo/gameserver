package com.game.params.worldboss;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//世界boss伤害排名(工具自动生成，请勿手动修改！）
public class WorldBossHurtRankVO implements IProtocol {
	public List<WorldBossHurtVO> rankList;//排名列表
	public int selfRank;//自己排名
	public int hurt;//自己伤害


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.rankList = null;
        else {
            int length = bb.getInt();
            this.rankList = new ArrayList<WorldBossHurtVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.rankList.add(null);
                }
                else
                {
                    WorldBossHurtVO instance = new WorldBossHurtVO();
                    instance.decode(bb);
                    this.rankList.add(instance);
                }

            }
        }
		this.selfRank = bb.getInt();
		this.hurt = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.rankList);
		bb.putInt(this.selfRank);
		bb.putInt(this.hurt);
	}
}
