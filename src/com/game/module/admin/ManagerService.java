package com.game.module.admin;

import com.game.SysConfig;
import com.game.data.ErrCode;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.chat.ChatExtension;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerService;
import com.game.module.vip.VipService;
import com.game.params.StringParam;
import com.game.params.player.PlayerVo;
import com.game.util.*;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManagerService implements InitHandler{
	
	public static final String RETURN_PARAM_ERROR = "param_error";// 参数错误
	public static final String RETURN_FAILED = "failed";// 发送消息失败，服务器异常
	public static final String RETURN_SUCCESS = "success";// 成功
	
	public static final String OK = "ok";
	public static final String REPEAT="repeat";
	public static final String BAN = "ban";
	public static final String FAIL = "fail";
	
	public static final int BAN_LOGIN = 1;//禁止登录
	public static final int BAN_CHAT = 2;//禁止聊天
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private ManagerDao managerDao;
	@Autowired
	private MailService mailService;
	@Autowired
	private VipService vipsService;	
	@Autowired
	private PlayerDao playerDao;


	private Map<Integer, UserManager> bans;
	
	@Override
	public void handleInit() {
		bans = new ConcurrentHashMap<>();
		for(UserManager u:managerDao.all()){
			bans.put(u.getPlayerId(), u);
		}
	}

	// //////////服务器转发/////////////////////////////////////////////////////////////
	/**
	 * 后台发过来的http格式：action=aaa&p1=11&p2=11 （具体跟后台系统的同事约定参数名） 返回 0 失败，返回1 成功
	 * 根据不同的action，处理后返回结果String 一个action，写一个函数处理返回结果
	 */
	public String handle(Map<String, String> params) throws SecurityException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String action = params.get("act");
		if ("xx".equals(action))
			return "xx";
		Method method;
		try {
			method = this.getClass().getMethod(
					String.format("%s_%s", "handle", action),Map.class);
			Object result = method.invoke(this, params);
			if (result != null)
				return (String) result;
		} catch (Exception e) {
			ServerLogger.err(e, "handle manager err!");
			return "command format err!";
		}
		return "command format err!";
	}
	
	/**
	 * 充值
	 */
	public String handle_pay(Map<String, String> params) {
		int playerId = Integer.parseInt(params.get("userid"));
		int chargeCount = Integer.parseInt(params.get("charge"));
		int id = Integer.parseInt(params.get("type"));
//		String orderId = params.get("order");
		//重新计算chargeId
		if(id==0){
			ServerLogger.warn("Err Charge id:",playerId,id,chargeCount);
			return RETURN_FAILED;
		}
		ServerLogger.info("后台充值 玩家id:"+playerId+" rechargeId:"+id+" 充值金额:"+chargeCount);
		//记录日志
		vipsService.addCharge(playerId, id,1,"test",SysConfig.currency,System.currentTimeMillis()+"",SysConfig.serverId);
		return RETURN_SUCCESS;
	}
	
	//加称号
	public String handle_add_title(Map<String,String> params){
		//int playerId = Integer.valueOf(params.get("playerId"));
		//int titleId = Integer.valueOf(params.get("title"));
		//titleService.addTitle(playerId, titleId);
		return RETURN_SUCCESS;
	}
	
	/**
	 * 发送邮件 
	 */
	public String handle_send_mail(Map<String, String> params){
		String title = params.get("title");
		String content = params.get("content");
		String rewards = params.get("rewards");
		String userIds = params.get("ids");
		String minLev = params.get("min_lev");
		String maxLev = params.get("max_lev");
		String vocation= params.get("vocation");
		
		//检查标题内容
		if(title==null||content==null){
			return RETURN_PARAM_ERROR;
		}
		//检查奖励id
		if(rewards!=null){
			Map<Integer, Integer> reward = StringUtil.str2map(rewards, ";", ":");
			for(int goodsId:reward.keySet()){
				if(ConfigData.getConfig(GoodsConfig.class, goodsId)==null){
					return RETURN_PARAM_ERROR;
				}
			}
		}

		//拼sql
		StringBuffer sql = new StringBuffer("SELECT playerId FROM player WHERE 1=1 ");
		if(userIds!=null&&!userIds.trim().equals("")&&!userIds.equals("null")){
			sql.append(" and playerId in (").append(userIds).append(") ");
		}
		if(minLev!=null&&!minLev.trim().equals("")&&!minLev.equals("null")){
			sql.append(" and lev>=").append(minLev);
		}
		if(maxLev!=null&&!maxLev.trim().equals("")&&!maxLev.equals("null")){
			sql.append(" and lev<=").append(maxLev);
		}
		if(vocation!=null&&!vocation.trim().equals("")&&!vocation.equals("0")){
			sql.append(" and vocation=").append(vocation);
		}
		List<Map<String, Object>> list = Context.getLoggerService().getDb().queryForList(sql.toString());
		if(list.isEmpty()){
			return RETURN_PARAM_ERROR;
		}
		List<Integer> playerIds = new ArrayList<Integer>(list.size());
		for(Map<String,Object> result:list){
			int playerId =  Integer.parseInt(String.valueOf(result.get("playerId")));
			playerIds.add(playerId);
		}



		//发邮件
		sendMail(title, content, rewards, playerIds);
		return RETURN_SUCCESS;
	}
	
	/**
	 * 封禁
	 */
	public String handle_ban(Map<String,String> params){
		int ban = Integer.parseInt(params.get("ban"));
		int playerId = Integer.parseInt(params.get("id"));
		int type = Integer.parseInt(params.get("type"));
		int time = Integer.parseInt(params.get("hour"));
		if(type==BAN_LOGIN){//登陆
			if(ban==0){//解封
				removeBan(BAN_LOGIN, playerId);
			}else{
				banLogin(playerId, time);
			}
		}else{//禁言
			if(ban==0){
				removeBan(BAN_CHAT, playerId);
				SessionManager.getInstance().kick(playerId);
			}else{
				banChat(playerId, time);
			}
		}
		return RETURN_SUCCESS;
	}
	
	//查询人物信息
	public String handle_getInfo(Map<String,String> params){
		String name = params.get("name");
		Integer playerId = null;
		if(name!=null){
			playerId = playerDao.selectIdByName(name);
			if(playerId == null){
				Player p = null;
				try{
					p = playerService.getPlayer(Integer.parseInt(name));
				}catch(Exception ex){
					
				}
				if(p==null){
					return RETURN_PARAM_ERROR;
				}
				playerId = p.getPlayerId();
			}
		}
		if(playerId==null){
			return RETURN_PARAM_ERROR;
		}
		PlayerVo vo = playerService.toSLoginVo(playerId);
		return JsonUtils.object2String(vo);
	}
	
	//发送系统消息
	public String handle_sendSysMsg(Map<String,String> params){
		StringParam param = new StringParam();
		param.param = params.get("msg");
		SessionManager.getInstance().sendMsgToAll(ChatExtension.SYS_NOTICE, param);
		return RETURN_SUCCESS;
	}

	//批量发系统邮件接口
	private void sendMail(String title, String content,
			String rewards, List<Integer> ids) {
		//发送者-系统
		String sysSender = ConfigData.getConfig(ErrCode.class, Response.SYS).tips;
		final List<Object[]> params = new ArrayList<Object[]>();
		for (int playerId : ids) {
			params.add(new Object[]{0,sysSender,playerId,title,content,rewards,rewards!=null&&!rewards.isEmpty()?1:0,LogConsume.GM.actionId});
		}
		mailService.sendBatchMail(params);
	}
	
	//批量发送邮件
	public void sendMail(String title,String content,int[][]rewards,List<Integer> ids){
		StringBuilder attach = new StringBuilder();
		if(rewards!=null){
			for(int i=0;i<rewards.length;i++){
				attach.append(rewards[i][0]).append(":");
				attach.append(rewards[i][1]);
				if(i!=rewards.length-1){
					attach.append(";");
				}
			}
		}
		sendMail(title, content, attach.toString(), ids);
	}
	
	public UserManager getBanInfo(int playerId){
		return bans.get(playerId);
	}
	
	//获取封禁信息
	public UserManager checkBanInfo(int playerId){
		UserManager u = bans.get(playerId);
		if(u!=null&&(u.getBanChat()>0||u.getBanLogin()>0)){
			Date now = new Date();
			if(u.getBanChat()>0){
				if(now.getTime()>u.getBanChatEnd().getTime()){
					u.setBanChat(0);
					managerDao.update(u);
				}
			}
			if(u.getBanLogin()>0){
				if(now.getTime()>u.getBanLoginEnd().getTime()){
					u.setBanLogin(0);
					managerDao.update(u);
				}
			}
		}
		return u;
	}
	
	//解除封禁
	private void removeBan(int type,int playerId){
		UserManager u = bans.get(playerId);
		if(type==BAN_LOGIN){
			u.setBanLogin(0);
		}else if(type==BAN_CHAT){
			u.setBanChat(0);
		}
		managerDao.update(u);
		bans.put(playerId, u);
	}
	
	//禁止登录
	public void banLogin(int playerId,int hour){
		UserManager u = bans.get(playerId);
		if(u==null){
			u = new UserManager();
			u.setPlayerId(playerId);
			managerDao.insert(u);
		}
		u.setBanLogin(hour);
		Date time = new Date();
		time.setTime(time.getTime()+TimeUtil.ONE_HOUR*hour);
		u.setBanLoginEnd(time);
		managerDao.update(u);
		
		bans.put(playerId, u);
		
		SessionManager.getInstance().kick(playerId);
	}
	
	//禁止聊天
	private void banChat(int playerId,int hour){
		UserManager u = bans.get(playerId);
		if(u==null){
			u = new UserManager();
			u.setPlayerId(playerId);
			managerDao.insert(u);
		}
		u.setBanChat(hour);
		Date time = new Date();
		time.setTime(time.getTime()+TimeUtil.ONE_HOUR*hour);
		u.setBanChatEnd(time);
		managerDao.update(u);
		
		bans.put(playerId, u);
		SessionManager.getInstance().kick(playerId);
	}

	//=====================================================



	/**
	 * 帐号信息
	 * @param params
	 * @return
	 */
	private String account(Map<String,String> params){
		return "";
	}

	/**
	 * 角色信息
	 * @param params
	 * @return
	 */
	private String role(Map<String,String> params){
		return "";
	}

	/**
	 * 玩家提问
	 * @param params
	 * @return
	 */
	private String question(Map<String,String> params){
		return "";
	}

	/**
	 * 公告发布
	 * @param params
	 * @return
	 */
	private String notice(Map<String,String> params){
		return "";
	}
}
