package com.game.params.scene;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//技能伤害(工具自动生成，请勿手动修改！）
public class SkillHurtVO implements IProtocol {
	public int attackId;//攻击者id
	public int skillId;//技能id
	public List<HurtVO> hurts;//伤害效果


	public void decode(BufferBuilder bb) {
		this.attackId = bb.getInt();
		this.skillId = bb.getInt();
		
        if (bb.getNullFlag())
            this.hurts = null;
        else {
            int length = bb.getInt();
            this.hurts = new ArrayList<HurtVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.hurts.add(null);
                }
                else
                {
                    HurtVO instance = new HurtVO();
                    instance.decode(bb);
                    this.hurts.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.attackId);
		bb.putInt(this.skillId);
		bb.putProtocolVoList(this.hurts);
	}
}
