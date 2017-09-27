package com.game.params.group;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//团队副本进度信息(工具自动生成，请勿手动修改！）
public class GroupStageVO implements IProtocol {
	public List<Integer> passCopy;//已经通过的副本
	public List<Int2Param> fightCopy;//正在打的副本
	public List<GroupTaskVO> tasks;//任务


	public void decode(BufferBuilder bb) {
		this.passCopy = bb.getIntList();
		
        if (bb.getNullFlag())
            this.fightCopy = null;
        else {
            int length = bb.getInt();
            this.fightCopy = new ArrayList<Int2Param>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.fightCopy.add(null);
                }
                else
                {
                    Int2Param instance = new Int2Param();
                    instance.decode(bb);
                    this.fightCopy.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.tasks = null;
        else {
            int length = bb.getInt();
            this.tasks = new ArrayList<GroupTaskVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.tasks.add(null);
                }
                else
                {
                    GroupTaskVO instance = new GroupTaskVO();
                    instance.decode(bb);
                    this.tasks.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putIntList(this.passCopy);
		bb.putProtocolVoList(this.fightCopy);
		bb.putProtocolVoList(this.tasks);
	}
}
