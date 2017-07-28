package com.game.params.task;

import java.util.List;
import java.util.ArrayList;
import com.game.params.*;

//任务列表(工具自动生成，请勿手动修改！）
public class TaskListInfo implements IProtocol {
	public List<STaskVo> task;//任务列表
	public List<SJointTaskVo> myJoint;//自己的合作任务
	public int jointedCount;//完成被邀请任务次数
	public SJointTaskVo currJointedPartner;//被邀请任务
	public List<SJointTaskVo> jointedList;//被邀请任务列表
	public int liveness;//当前活跃度
	public List<IntParam> livebox;//已领的奖励ID


	public void decode(BufferBuilder bb) {
		
        if (bb.getNullFlag())
            this.task = null;
        else {
            int length = bb.getInt();
            this.task = new ArrayList<STaskVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.task.add(null);
                }
                else
                {
                    STaskVo instance = new STaskVo();
                    instance.decode(bb);
                    this.task.add(instance);
                }

            }
        }
		
        if (bb.getNullFlag())
            this.myJoint = null;
        else {
            int length = bb.getInt();
            this.myJoint = new ArrayList<SJointTaskVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.myJoint.add(null);
                }
                else
                {
                    SJointTaskVo instance = new SJointTaskVo();
                    instance.decode(bb);
                    this.myJoint.add(instance);
                }

            }
        }
		this.jointedCount = bb.getInt();
		
        if(bb.getNullFlag())
            this.currJointedPartner = null;
        else
        {
            this.currJointedPartner = new SJointTaskVo();
            this.currJointedPartner.decode(bb);
        }
		
        if (bb.getNullFlag())
            this.jointedList = null;
        else {
            int length = bb.getInt();
            this.jointedList = new ArrayList<SJointTaskVo>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.jointedList.add(null);
                }
                else
                {
                    SJointTaskVo instance = new SJointTaskVo();
                    instance.decode(bb);
                    this.jointedList.add(instance);
                }

            }
        }
		this.liveness = bb.getInt();
		
        if (bb.getNullFlag())
            this.livebox = null;
        else {
            int length = bb.getInt();
            this.livebox = new ArrayList<IntParam>();
            for (int i = 0; i < length; i++)
            {
                //如果元素不够先创建一个，Java泛型创建对象，性能？
                boolean isNull = bb.getNullFlag();

                //如果不是null就解析
                if(isNull)
                {
                    this.livebox.add(null);
                }
                else
                {
                    IntParam instance = new IntParam();
                    instance.decode(bb);
                    this.livebox.add(instance);
                }

            }
        }
	}

	public void encode(BufferBuilder bb) {
		bb.putProtocolVoList(this.task);
		bb.putProtocolVoList(this.myJoint);
		bb.putInt(this.jointedCount);
		bb.putProtocolVo(this.currJointedPartner);
		bb.putProtocolVoList(this.jointedList);
		bb.putInt(this.liveness);
		bb.putProtocolVoList(this.livebox);
	}
}
