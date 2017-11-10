package com.game.module.player;

import com.game.module.fashion.FashionService;
import com.game.params.Int2Param;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.SysConfig;
import com.game.data.Response;
import com.game.event.DefaultLogoutHandler;
import com.game.event.LoginHandler;
import com.game.module.activity.ActivityService;
import com.game.module.admin.ManagerService;
import com.game.module.admin.UserManager;
import com.game.module.gang.Gang;
import com.game.module.gang.GangService;
import com.game.module.log.LoggerService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.StringParam;
import com.game.params.player.CLoginVo;
import com.game.params.player.CRegVo;
import com.game.params.player.PlayerVo;
import com.game.params.player.SRoleVo;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.anotation.UnLogin;
import com.server.util.ServerLogger;

@Extension
public class PlayerExtension {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private DefaultLogoutHandler logoutHandler;
	@Autowired
	private ManagerService managerService;
	@Autowired
	private LoggerService loggerService;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private LoginHandler loginHandler;
	@Autowired
	private GangService gangService;
	@Autowired
	private FashionService fashionService;

	private static final AttributeKey<String> CHANNEL = AttributeKey.valueOf("channel");

	@UnLogin
	@Command(1001)
	public Object getRoleList(int playerId, StringParam param, Channel channel) {
		String accName = param.param;
		channel.attr(CHANNEL).set(accName);
		List<Player> roleList = playerService.getPlayersByAccName(accName);

		ListParam<SRoleVo> vo = new ListParam<SRoleVo>();
		vo.params = new ArrayList<SRoleVo>(roleList.size());
		
		for(Player player:roleList){
			fashionService.checkRemoveTimeoutFashions(player.getPlayerId(),false);

			SRoleVo role = new SRoleVo();
			role.attack = player.getAttack();
			role.playerId = player.getPlayerId();
			role.crit = player.getAttack();
			role.defense = player.getDefense();
			role.exp = player.getExp();
			role.fashionId = player.getFashionId();
			role.fu = player.getFu();
			role.hp = player.getHp();
			role.lastLoginTime=player.getLastLoginTime()==null?0:player.getLastLoginTime().getTime();
			role.level = player.getLev();
			role.name = player.getName();
			role.sex = player.getSex();
			role.symptom = player.getSymptom();
			role.title = player.getTitle();
			role.vip = player.getVip();
			role.vocation = player.getVocation();
			role.weapon = player.getWeaponId();
			if(player.getGangId() > 0){				
				Gang gang = gangService.getGang(player.getGangId());
				if(gang == null){
					player.setGangId(0);
				}else{					
					role.gang = gang.getName();
				}
			}
			role.head = playerService.getPlayerData(player.getPlayerId()).getCurHead();
			vo.params.add(role);
		}
		return vo;
	}

	@UnLogin
	@Command(1002)
	public Object createRole(int playerId, CRegVo param, Channel channel) {
		// 连接数太多
		IntParam result = new IntParam();
		if (SessionManager.getInstance().getOnlineCount() > SysConfig.maxCon) {
			result.param = Response.TOO_MANY_CON;
			return result;
		}
		// 关闭注册
		if (!SysConfig.reg) {
			result.param = Response.CLOSE_REG;
			return result;
		}
		// 版本检测
		if (!playerService.checkVersion(param.version)) {
			result.param = Response.LOW_VERSION;
			return result;
		}

		// 登录验证
		int auth = playerService.auth();
		if (auth != 0) {
			result.param = auth;
			return result;
		}
		// 参数错误
		if (!playerService.checkRegParam(param)) {
			result.param = Response.ERR_PARAM;
			return result;
		}
		// 同名
		if (playerService.getPlayerIdByName(param.name) > 0) {
			result.param = Response.SAME_NAME;
			return result;
		}
		// 角色数量上限
		if (playerService.getPlayersByAccName(param.accName).size() >= ConfigData.globalParam().maxRoleCount) {
			result.param = Response.TOO_MANY_ROLE;
			return result;
		}
		Player player = playerService.addNewPlayer(param.name, param.sex, param.vocation, param.accName,param.channel);
		if (player == null) {
			result.param = Response.SAME_NAME;
			return result;
		}
		CLoginVo loginVo = new CLoginVo();
		loginVo.playerId = player.getPlayerId();
		loginVo.version = ConfigData.globalParam().version;// 版本
		// 直接返回登录结果
		PlayerVo loginResult = login(0, loginVo, channel);
		SessionManager.sendDataInner(channel, 1003, loginResult);
		return null;
	}

	@UnLogin
	@Command(1003)
	public PlayerVo login(int id, CLoginVo param, Channel channel) {
		PlayerVo result = new PlayerVo();
		if (param.playerId == 0) {

			result.code = Response.ERR_PARAM;
			return result;
		}
		// 版本检测
		if (!playerService.checkVersion(param.version)) {
			result.code = Response.LOW_VERSION;
			return result;
		}

		// 连接数太多
		if (SessionManager.getInstance().getOnlineCount() > SysConfig.maxCon) {
			result.code = Response.TOO_MANY_CON;
			return result;
		}
		UserManager ban = managerService.checkBanInfo(param.playerId);
		if (ban != null && ban.getBanLogin() > 0) {
			result.code = Response.BAN_LOGIN;
			return result;
		}
		// 登录验证
		int auth = playerService.auth();
		if (auth != 0) {
			result.code = auth;
			return result;
		}
		// 踢走旧的登录
		int playerId = param.playerId;
		Player player = playerService.getPlayer(playerId);
		String accName = channel.attr(CHANNEL).get();
		if(accName==null||!accName.equals(player.getAccName())){
			result.code = Response.ERR_PARAM;
			return result;
		}
		final Channel oldChannel = SessionManager.getInstance().getChannel(playerId);
		if (oldChannel != null) {
			ServerLogger.debug("duplicated login:", playerId);
			SessionManager.getInstance().removePlayerAttr(oldChannel);
			oldChannel.close();
			logoutHandler.logout(playerId);
		}
		
		player.setRefresh(false);
		player.setSubLine(0);
		SessionManager.getInstance().addSession(playerId, channel);
		// 第一次登录
		if (player.getLastLoginTime() == null) {
			playerService.handleFirstLogin(playerId);
		}
		player.setLastLoginTime(new Date());
		player.setIp(CommonUtil.getIp(channel.remoteAddress()));
		// 处理登录
		playerService.handleLogin(playerId);
		//其它子系统的登录处理
		loginHandler.playerLogin(playerId);

		result = playerService.toSLoginVo(playerId);
		if (ban != null && ban.getBanChat() > 0) {
			result.banChat = true;
		}
		
		player.setRefresh(true);
		// 设置session等级
		SessionManager.getInstance().setPlayerLev(playerId, player.getLev());
		return result;
	}

	// 更新玩家属性
	public static final int REFRESH_MY_VO = 1004;
	
	// 属性更新n条属性
	public static final int UPDATE_ATTR = 1005;
	
	//更新货币
	public static final int UPDATE_CURRENCY = 1006;
	
	@Command(1007)
	public Object getOtherPlayer(int playerId, IntParam param){
		return playerService.toSLoginVo(param.param);
	}

	@Command(1008)
	public Object openModule(int playerId, IntParam param){
		PlayerData playerData = playerService.getPlayerData(playerId);
		if(!playerData.getModules().contains(param.param)) {
			playerData.getModules().add(param.param);
		}
		Int2Param int2Param = new Int2Param();
		int2Param.param1 = Response.SUCCESS;
		int2Param.param2 = param.param;

		return int2Param;
	}

	@Command(1009)
	public Object newHandleStep(int playerId, IntParam param){
		PlayerData playerData = playerService.getPlayerData(playerId);
		playerData.getGuideSteps().add(param.param);
		IntParam intParam = new IntParam();
		intParam.param = param.param;
		return intParam;
	}
}
