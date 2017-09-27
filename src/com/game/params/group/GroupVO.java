package com.game.params.group;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//团队信息(工具自动生成，请勿手动修改！）
public class GroupVO implements IProtocol {
	public int id;//团体ID
	public int leaderId;//团长ID
	public String groupName;//团队名字
	public int leaderVocation;//团长职业
	public boolean openFlag;//开放房间
	public int level;//等级
	public int stage;//阶段
	public int groupCopyId;//副本id
	public List<GroupTeamVO> teams;//队伍列表


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.leaderId = bb.getInt();
		this.groupName = bb.getString();
		this.leaderVocation = bb.getInt();
		this.openFlag = bb.getBoolean();
		this.level = bb.getInt();
		this.stage = bb.getInt();
		this.groupCopyId = bb.getInt();
		
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
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.leaderId);
		bb.putString(this.groupName);
		bb.putInt(this.leaderVocation);
		bb.putBoolean(this.openFlag);
		bb.putInt(this.level);
		bb.putInt(this.stage);
		bb.putInt(this.groupCopyId);
		bb.putProtocolVoList(this.teams);
	}
}
