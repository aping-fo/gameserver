package com.game.params.friend;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//好友信息(工具自动生成，请勿手动修改！）
public class FriendInfo implements IProtocol {
	public List<FriendVo> friends;//好友
	public List<FriendVo> recent;//最近联系人
	public List<FriendVo> blacks;//黑名单
	public List<FriendVo> requests;//好友申请


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.friends = null;
        else {
            int length = bb.getInt();
            this.friends = new ArrayList<FriendVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.friends.add(null);
                }
                else
                {
                    FriendVo instance = new FriendVo();
                    instance.decode(bb);
                    this.friends.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.recent = null;
        else {
            int length = bb.getInt();
            this.recent = new ArrayList<FriendVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.recent.add(null);
                }
                else
                {
                    FriendVo instance = new FriendVo();
                    instance.decode(bb);
                    this.recent.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.blacks = null;
        else {
            int length = bb.getInt();
            this.blacks = new ArrayList<FriendVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.blacks.add(null);
                }
                else
                {
                    FriendVo instance = new FriendVo();
                    instance.decode(bb);
                    this.blacks.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.requests = null;
        else {
            int length = bb.getInt();
            this.requests = new ArrayList<FriendVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.requests.add(null);
                }
                else
                {
                    FriendVo instance = new FriendVo();
                    instance.decode(bb);
                    this.requests.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.friends);
		bb.putProtocolVoList(this.recent);
		bb.putProtocolVoList(this.blacks);
		bb.putProtocolVoList(this.requests);
	}
}
