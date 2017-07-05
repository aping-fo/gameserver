package com.game.util;

/**
 * @author luojian 
 * 一些消息，纯粹由后端发起的命令 在这里定义一个交易码，定义的时候顺便详细说明一下返回参数
 */
public class SystemMsg {

	/**
	 * 已经登录过了，强制之前的退出
	 */
	public static final String FORCE_LOGOUT = "000001";
	// 关服报文
	public static final String CLOSE_SERVER = "000002";
	
	//有人进入场景
	public static final String ENTER_SCENE = "100001";
	//离开场景
	public static final String EXIT_SCENE = "100002";
	//场景移动
	public static final String WALK_SCENE = "100005";
	//场景瞬移
	public static final String MOVE_SCENE = "100008";
	//停止移动
	public static final String STOP_WALK = "100011";


/*	9		008	100008	玩家衣服更新广播	后端推送
	10		009	100009	玩家武器更新广播	后端推送
	13		013	100013	玩家头顶文字更新广播	后端推送
	14		014	100014	玩家血量更新广播	后端推送
	17		017 100017 	玩家状态推送	后端推送
	18		018	100018	玩家buff推送	后端推送*/
	
	public static final String SCENE_CLOTH = "100008";
	public static final String SCENE_WEAPON = "100009";
	public static final String SCENE_RIDING = "100012";
	public static final String SCENE_LABEL = "100013";
	public static final String SCENE_HPMP = "100014";
	public static final String SCENE_STATE = "100017";
	public static final String SCENE_BUFF = "100019";
	
}
