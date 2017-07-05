package com.game.params.copy;

import com.game.params.*;

//进入副本返回(工具自动生成，请勿手动修改！）
public class SEnterCopy implements IProtocol {
	public int code;//错误码
	public int copyId;//副本id
	public int sceneId;//场景id
	public int passId;//关卡id(通常跟copyId相等,活动副本特殊)


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.copyId = bb.getInt();
		this.sceneId = bb.getInt();
		this.passId = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.copyId);
		bb.putInt(this.sceneId);
		bb.putInt(this.passId);
	}
}
