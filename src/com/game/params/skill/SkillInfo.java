package com.game.params.skill;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//技能信息(工具自动生成，请勿手动修改！）
public class SkillInfo implements IProtocol {
	public List<Integer> skills;//所有技能id
	public List<Integer> curSkills;//当前使用的技能id
	public List<SkillCardVo> skillCards;//技能卡
	public SkillCardGroupInfo cardGroupInfo;//技能卡组信息


	public void decode(BufferBuilder bb) {
		this.skills = bb.getIntList();
		this.curSkills = bb.getIntList();
		
        if (bb.getNullFlag())
            this.skillCards = null;
        else {
            int length = bb.getInt();
            this.skillCards = new ArrayList<SkillCardVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.skillCards.add(null);
                }
                else
                {
                    SkillCardVo instance = new SkillCardVo();
                    instance.decode(bb);
                    this.skillCards.add(instance);
                }

            }
        }
		
        if(bb.getNullFlag())
            this.cardGroupInfo = null;
        else
        {
            this.cardGroupInfo = new SkillCardGroupInfo();
            this.cardGroupInfo.decode(bb);
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putIntList(this.skills);
		bb.putIntList(this.curSkills);
		bb.putProtocolVoList(this.skillCards);
		bb.putProtocolVo(this.cardGroupInfo);
	}
}
