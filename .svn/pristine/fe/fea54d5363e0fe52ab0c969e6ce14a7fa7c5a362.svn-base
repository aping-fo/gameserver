package com.game.params.team;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//我的队伍信息(工具自动生成，请勿手动修改！）
public class MyTeamVO implements IProtocol {
	public int leader;//队长id
	public List<TeamMemberVO> member;//队员


	public void decode(BufferBuilder bb) {
		this.leader = bb.getInt();
		
        if (bb.getNullFlag())
            this.member = null;
        else {
            int length = bb.getInt();
            this.member = new ArrayList<TeamMemberVO>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.member.add(null);
                }
                else
                {
                    TeamMemberVO instance = new TeamMemberVO();
                    instance.decode(bb);
                    this.member.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.leader);
		bb.putProtocolVoList(this.member);
	}
}
