package com.game.params.group;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//团队信息(工具自动生成，请勿手动修改！）
public class GroupVO implements IProtocol {
	public int id;//团体ID
	public int leaderId;//团长ID
	public int level;//等级
	public int stage;//阶段
	public List<GroupTeamVO> teams;//队伍列表
	public List<GroupTaskVO> tasks;//任务


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.leaderId = bb.getInt();
		this.level = bb.getInt();
		this.stage = bb.getInt();
		
        if (bb.getNullFlag())
            this.teams = null;
        else {
            int length = bb.getInt();
            this.teams = new ArrayList<GroupTeamVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.teams.add(null);
                }
                else
                {
                    GroupTeamVO instance = new GroupTeamVO();
                    instance.decode(bb);
                    this.teams.add(instance);
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
		bb.putInt(this.id);
		bb.putInt(this.leaderId);
		bb.putInt(this.level);
		bb.putInt(this.stage);
		bb.putProtocolVoList(this.teams);
		bb.putProtocolVoList(this.tasks);
	}
}
