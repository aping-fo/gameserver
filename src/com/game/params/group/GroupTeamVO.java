package com.game.params.group;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//团队队伍信息(工具自动生成，请勿手动修改！）
public class GroupTeamVO implements IProtocol {
	public int id;//队伍ID
	public int leaderId;//队长id
	public boolean fightFlag;//是否在副本中
	public List<GroupTeamMemberVO> members;//成员列表


	public void decode(BufferBuilder bb) {
		this.id = bb.getInt();
		this.leaderId = bb.getInt();
		this.fightFlag = bb.getBoolean();
		
        if (bb.getNullFlag())
            this.members = null;
        else {
            int length = bb.getInt();
            this.members = new ArrayList<GroupTeamMemberVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.members.add(null);
                }
                else
                {
                    GroupTeamMemberVO instance = new GroupTeamMemberVO();
                    instance.decode(bb);
                    this.members.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.id);
		bb.putInt(this.leaderId);
		bb.putBoolean(this.fightFlag);
		bb.putProtocolVoList(this.members);
	}
}
