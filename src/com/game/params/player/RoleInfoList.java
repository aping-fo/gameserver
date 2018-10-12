package com.game.params.player;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//角色信息列表(工具自动生成，请勿手动修改！）
public class RoleInfoList implements IProtocol {
	public List<SRoleVo> roleInfoVoList;//角色信息列表
	public int errorCode;//错误码


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.roleInfoVoList = null;
        else {
            int length = bb.getInt();
            this.roleInfoVoList = new ArrayList<SRoleVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.roleInfoVoList.add(null);
                }
                else
                {
                    SRoleVo instance = new SRoleVo();
                    instance.decode(bb);
                    this.roleInfoVoList.add(instance);
                }

            }
        }
		this.errorCode = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.roleInfoVoList);
		bb.putInt(this.errorCode);
	}
}
