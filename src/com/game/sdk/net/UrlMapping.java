package com.game.sdk.net;

public class UrlMapping {
	public static final String URI_NOTICE = "/admin/notice"; // 发送系统公告
	public static final String URI_CONFIG = "/admin/config"; // 更新系统配置
	public static final String URI_WB_ADD = "/admin/wb/add"; // 添加黑白名单设置
	public static final String URI_WB_DEL = "/admin/wb/del"; // 删除黑白名单设置
	public static final String URI_MAIL = "/admin/mail"; // 发送系统邮件
	public static final String URI_ONLINE = "/admin/online"; // 查看在线人数
	public static final String URI_ROLE_INFO = "/admin/role_info"; // 查看玩家详情
	public static final String URI_ROLE_BAG_INFO = "/admin/role_bag_info"; // 查看玩家背包信息
	public static final String URI_REAL_DATA = "/admin/real_data"; // 服务器当前的实时数据
	public static final String URI_ROLE_VIEW_LIST = "/admin/role_view_list"; // 查看所有玩家视图列表
	public static final String URI_ROLE_ONLINE_LIST = "/admin/role_online_list"; // 查看在线玩家列表
	public static final String URI_KICK = "/admin/kick"; // 踢人
	public static final String URI_MSG = "/admin/msg"; // 系统消息
	public static final String URI_CHANGE_USERTYPE = "/admin/change_usertype"; // 更改玩家类型:封号，禁言，GM....
	public static final String URI_BUG_FIX = "/admin/bug/fix"; // 修复bug
	public static final String URI_SEND_ITEM = "/admin/send_item"; // 发送道具接口

	public static final String URI_RECHARGE_CALLBACK = "/admin/recharge_callback"; // 充值回调接口
	public static final String URI_GM_RECHARGE = "/admin/gm/recharge"; // GM，后台，模拟充值

	public static final String URI_START_GAME = "/monitor/start"; // 开服
	public static final String URI_STOP_GAME = "/monitor/stop"; // 停服
	public static final String URI_UPDATE_GAME = "/monitor/update"; // 更新服务器

	/**
	 * 构建请求地址
	 * 
	 * @param host
	 * @param port
	 * @param uri
	 * @return
	 */
	public static String buildUrl(String host, String port, String uri) {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(host).append(":").append(port).append(uri);
		return sb.toString();
	}
}
