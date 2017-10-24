package com.game.params.gang;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//公会副本挑战信息(工具自动生成，请勿手动修改！）
public class GangCopyChallengeVO implements IProtocol {
	public int errCode;//错误码
	public List<MonsterVo> monsters;//怪物列表


	public void decode(BufferBuilder bb) {
		this.errCode = bb.getInt();
		
        if (bb.getNullFlag())
            this.monsters = null;
        else {
            int length = bb.getInt();
            this.monsters = new ArrayList<MonsterVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.monsters.add(null);
                }
                else
                {
                    MonsterVo instance = new MonsterVo();
                    instance.decode(bb);
                    this.monsters.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.errCode);
		bb.putProtocolVoList(this.monsters);
	}
}
