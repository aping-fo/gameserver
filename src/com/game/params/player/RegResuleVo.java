package com.game.params.player;

import com.game.params.*;

//角色注册回应信息(工具自动生成，请勿手动修改！）
public class RegResuleVo implements IProtocol {
	public int code;//错误码
	public String userName;//渠道唯一标志
	public String roleId;//角色ID
	public String roleName;//角色名
	public String serverId;//区服ID
	public String serverName;//区服名
	public String createTime;//创建时间


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.userName = bb.getString();
		this.roleId = bb.getString();
		this.roleName = bb.getString();
		this.serverId = bb.getString();
		this.serverName = bb.getString();
		this.createTime = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putString(this.userName);
		bb.putString(this.roleId);
		bb.putString(this.roleName);
		bb.putString(this.serverId);
		bb.putString(this.serverName);
		bb.putString(this.createTime);
	}
}
