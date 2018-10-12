package com.game.params.player;

import com.game.params.*;

//请求角色列表(工具自动生成，请勿手动修改！）
public class CRegRoleListVo implements IProtocol {
	public String userId;//用户ID
	public String accName;//账号名称
	public String deviceId;//设备ID
	public String ipAddress;//IP地址


	public void decode(BufferBuilder bb) {
		this.userId = bb.getString();
		this.accName = bb.getString();
		this.deviceId = bb.getString();
		this.ipAddress = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.userId);
		bb.putString(this.accName);
		bb.putString(this.deviceId);
		bb.putString(this.ipAddress);
	}
}
