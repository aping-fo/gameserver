package com.game.params.scene;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//场景信息(工具自动生成，请勿手动修改！）
public class SSceneInfo implements IProtocol {
	public int code;//错误码
	public int sceneId;//场景id
	public List<SScenePlayerVo> players;//场景玩家
	public List<SMonsterVo> monsters;//场景怪物信息
	public List<Integer> affixs;//特性副本词缀
	public List<Integer> bufferList;//套装buffer列表


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.sceneId = bb.getInt();
		
        if (bb.getNullFlag())
            this.players = null;
        else {
            int length = bb.getInt();
            this.players = new ArrayList<SScenePlayerVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.players.add(null);
                }
                else
                {
                    SScenePlayerVo instance = new SScenePlayerVo();
                    instance.decode(bb);
                    this.players.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.monsters = null;
        else {
            int length = bb.getInt();
            this.monsters = new ArrayList<SMonsterVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.monsters.add(null);
                }
                else
                {
                    SMonsterVo instance = new SMonsterVo();
                    instance.decode(bb);
                    this.monsters.add(instance);
                }

            }
        }
		this.affixs = bb.getIntList();
		this.bufferList = bb.getIntList();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.sceneId);
		bb.putProtocolVoList(this.players);
		bb.putProtocolVoList(this.monsters);
		bb.putIntList(this.affixs);
		bb.putIntList(this.bufferList);
	}
}
