package com.game.module.vip;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.SysConfig;
import com.game.data.ChargeConfig;
import com.game.module.daily.DailyService;
import com.game.module.player.PlayerService;
import com.game.params.IntParam;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class VipExtension {
	
	@Autowired
	private VipService vipService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private DailyService dailyService;
	
	//领取vip礼包
	@Command(2201)
	public Object getReward(int playerId,IntParam param){
		int vipLev = param.param;
		int code = vipService.getVipReward(playerId, vipLev);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}
	
	//领取vip每日福利
	@Command(2202)
	public Object getVipDailyReward(int playerId,Object param){
		int code = vipService.getVipDailyReward(playerId);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}
	
	//领取月卡
	@Command(2203)
	public Object getMonthCard(int playerId,Object param){
		int code = vipService.getMonthCardReward(playerId);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	//充值结果
	public static final int CHARGE = 2204;
	
	//测试充值接口
	@Command(2204)
	public Object testCharge(int playerId,IntParam charge){
		if(!SysConfig.debug){
			return null;
		}
		ChargeConfig cfg = ConfigData.getConfig(ChargeConfig.class, charge.param);
		vipService.addCharge(playerId, charge.param,cfg.total);
		return null;
	}

	public static final int GET_DAILY_INFO = 2205;
	//每日信息
	@Command(2205)
	public Object getDailyInfo(int playerId,Object param){
		return dailyService.getDailyInfo(playerId);
	}
	
	//消费日志
	public static final int CONSUME_LOG = 2206;
	
	//领取基金
	@Command(2207)
	public Object takeFund(int playerId,IntParam lev){
		IntParam result = new IntParam();
		result.param = vipService.takeFund(playerId, lev.param);
		return result;
	}
	
	//凌晨更新
	public static final int UPDATE_ZERO_CLOCK = 2208;
}
