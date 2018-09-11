package com.game.params.copy;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//进入副本返回(工具自动生成，请勿手动修改！）
public class SEnterCopy implements IProtocol {
	public int code;//错误码
	public int copyId;//副本id
	public int sceneId;//场景id
	public int passId;//关卡id(通常跟copyId相等,活动副本特殊)
	public List<RecordHolder> recordHolder;//记录保持者
	public int selfRecord;//自己的记录
	public int customPara;//自定义参数


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.copyId = bb.getInt();
		this.sceneId = bb.getInt();
		this.passId = bb.getInt();
		
        if (bb.getNullFlag())
            this.recordHolder = null;
        else {
            int length = bb.getInt();
            this.recordHolder = new ArrayList<RecordHolder>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.recordHolder.add(null);
                }
                else
                {
                    RecordHolder instance = new RecordHolder();
                    instance.decode(bb);
                    this.recordHolder.add(instance);
                }

            }
        }
		this.selfRecord = bb.getInt();
		this.customPara = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.copyId);
		bb.putInt(this.sceneId);
		bb.putInt(this.passId);
		bb.putProtocolVoList(this.recordHolder);
		bb.putInt(this.selfRecord);
		bb.putInt(this.customPara);
	}
}
