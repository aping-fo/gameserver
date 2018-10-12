package com.game.params.chat;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//聊天信息列表(工具自动生成，请勿手动修改！）
public class ChatInfoList implements IProtocol {
	public List<ChatVo> chatInfoVoList;//聊天信息列表
	public int errorCode;//错误码


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.chatInfoVoList = null;
        else {
            int length = bb.getInt();
            this.chatInfoVoList = new ArrayList<ChatVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.chatInfoVoList.add(null);
                }
                else
                {
                    ChatVo instance = new ChatVo();
                    instance.decode(bb);
                    this.chatInfoVoList.add(instance);
                }

            }
        }
		this.errorCode = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.chatInfoVoList);
		bb.putInt(this.errorCode);
	}
}
