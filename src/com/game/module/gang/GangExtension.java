package com.game.module.gang;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.Response;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.String2Param;
import com.game.params.StringParam;
import com.game.params.gang.GTrainingVO;
import com.game.params.gang.GangApply;
import com.game.params.gang.GangLimit;
import com.game.params.gang.GangList;
import com.game.params.gang.GangMember;
import com.game.params.gang.MyGangInfo;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class GangExtension {

	@Autowired
	private GangService gangService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerCalculator playerCalculator;
	@Autowired
	private GangDungeonService gangDungeonService;

	// 帮派列表
	@Command(2501)
	public GangList getGangList(int playerId, IntParam param) {
		return gangService.getGangList(playerId, param.param);
	}

	// 创建帮派
	@Command(2502)
	public Object create(int playerId, String2Param param) {
		String name = param.param1;
		String notice = param.param2;

		int code = gangService.create(playerId, name, notice);
		Int2Param result = new Int2Param();
		result.param1 = code;

		if (code == Response.SUCCESS) {
			result.param2 = playerService.getPlayer(playerId).getGangId();
			MyGangInfo myGang = gangService.getMyGang(playerId);
			SessionManager.getInstance()
					.sendMsg(REFRESH_GANG, myGang, playerId);
			// gangService.donate(playerId, 1, 1000);
		}

		return result;
	}

	public static final int REFRESH_GANG = 2503;

	// 我的帮派信息
	@Command(2503)
	public MyGangInfo getMyGang(int playerId, Object param) {
		MyGangInfo myGang = gangService.getMyGang(playerId);
		if (myGang.basicInfo != null) {
			playerService.getPlayer(playerId).setRefresh(false);
			playerCalculator.calculate(playerId);
		}
		return myGang;
	}

	// 申请帮派
	@Command(2504)
	public Object apply(int playerId, IntParam param) {
		IntParam result = new IntParam();
		result.param = gangService.apply(playerId, param.param);
		return result;
	}

	public static final int JOIN_GANG_SUCESS = 2505;
 
	// 批准加入
	@Command(2506)
	public Object approve(int playerId, IntParam param) {
		int code = gangService.approve(playerId, param.param);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	// 拒绝加入
	@Command(2507)
	public Object refuse(int playerId, IntParam param) {
		int code = gangService.refuse(playerId, param.param);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	// 踢人
	@Command(2508)
	public Object kick(int playerId, IntParam param) {
		int code = gangService.kick(playerId, param.param);
		Int2Param result = new Int2Param();
		result.param1 = code;
		result.param2 = param.param;
		return result;
	}

	// 转让
	@Command(2509)
	public Object transfer(int playerId, IntParam param) {
		int code = gangService.transfer(playerId, param.param);
		Int2Param result = new Int2Param();
		result.param1 = code;
		result.param2 = param.param;
		return result;
	}

	// 设置副会长
	@Command(2510)
	public Object setViceOwner(int playerId, IntParam param) {
		int code = gangService.setViceOwner(playerId, param.param);
		Int2Param result = new Int2Param();
		result.param1 = code;
		result.param2 = param.param;
		return result;
	}

	// 解散
	@Command(2511)
	public Object dissolve(int playerId, Object param) {
		IntParam result = new IntParam();
		result.param = gangService.dissolve(playerId);
		return result;
	}

	// 更新公告
	@Command(2512)
	public Object changeNotice(int playerId, StringParam param) {
		IntParam result = new IntParam();
		result.param = gangService.udpateNotice(playerId, param.param);
		return result;
	}

	// 捐献
	@Command(2513)
	public Object donate(int playerId, IntParam param) {
		int code = gangService.donate(playerId, param.param);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	// 设置条件
	@Command(2514)
	public Object setLimit(int playerId, GangLimit limit) {
		int code = gangService.setLimit(playerId, limit);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	// 离开帮派
	@Command(2515)
	public Object quit(int playerId, Object param) {
		int code = gangService.quit(playerId);
		IntParam result = new IntParam();
		result.param = code;
		return result;
	}

	// 获取成员列表
	@Command(2516)
	public Object getMembers(int playerId, Object param) {
		ListParam<GangMember> members = new ListParam<GangMember>();
		members.params = gangService.getMembers(playerId);
		return members;
	}

	// 获取申请列表
	@Command(2517)
	public Object getApplys(int playerId, Object param) {
		ListParam<GangApply> applys = new ListParam<GangApply>();
		applys.params = gangService.getApplys(playerId);
		return applys;
	}

	// 设置副会长
	@Command(2519)
	public Object removeVice(int playerId, IntParam param) {
		int code = gangService.removeViceAdmin(playerId, param.param);
		Int2Param result = new Int2Param();
		result.param1 = code;
		result.param2 = param.param;
		return result;
	}
	
	//重命名
	@Command(2520)
	public Object rename(int playerId, StringParam param){
		IntParam result = new IntParam();
		result.param = gangService.rename(playerId, param.param);
		return result;
	}

	//广播
	@Command(2521)
	public Object brocast(int playerId, StringParam param){
		IntParam result = new IntParam();
		result.param = gangService.brocast(playerId, param.param);
		return result;
	}
	
	//捐献信息
	public static final int REFRESH_DONATION_INFO = 2522;
	@Command(2522)
	public Object getDonationInfo(int playerId, Object param){
		return gangService.getDonationInfo(playerId);
	}
	
	@Command(2523)
	public Object getTrainingInfo(int playerId, Object param){
		GTrainingVO vo = new GTrainingVO();
		Player player = playerService.getPlayer(playerId);
		Gang gang = gangService.getGang(player.getGangId());
		if(gang == null) {
			return null;
		}
		GTRoom room = gang.getGtRoom();
		if(room != null){
			vo.id = room.getId();
			vo.max = room.getMax();
			GMember member = gang.getMembers().get(playerId);
			vo.trainingTime = (int)member.getTrainingTime();
			vo.createTime = room.getCreateTime();
			vo.startTime = member.getStartTraining();
		}
		return vo;
	}
	
	@Command(2524)
	public Object launchTraining(int playerId, IntParam param){
		Int2Param result = new Int2Param();
		result.param1 = gangService.launchGTRoom(playerId, param.param);
		result.param2 = param.param;
		return result;
	}
	
	@Command(2525)
	public Object closeTraining(int playerId, Object param){
		IntParam result = new IntParam();
		result.param = gangService.stopTraining(playerId);
		return result;
	}
	
	@Command(2526)
	public Object startTraining(int playerId, Object param){
		IntParam result = new IntParam();
		result.param = gangService.startTraining(playerId);
		return result;
	}
	
	@Command(2527)
	public Object takeTrainingReward(int playerId, Object param){
		return gangService.takeTrainingReward(playerId);
	}

	@Command(2528)
	public Object getGangCopyInfo(int playerId, Object param){
		return gangDungeonService.getGangCopyInfo(playerId);
	}

	@Command(2529)
	public Object openChallenge(int playerId, Object param){
		return gangDungeonService.openChallenge(playerId);
	}

	@Command(2530)
	public Object startChallenge(int playerId, Object param){
		return gangDungeonService.startChallenge(playerId);
	}

	@Command(2532)
	public Object unLockTechnology(int playerId, IntParam param){
		return gangService.unLockTechnology(playerId,param.param);
	}

	@Command(2533)
	public Object upgradeTechnology(int playerId, IntParam param){
		return gangService.learnTechnology(playerId,param.param);
	}

	@Command(2534)
	public Object getTechnology(int playerId, Object param){
		return gangService.getTechnology(playerId);
	}

	@Command(2535)
	public Object getHurtRankList(int playerId, Object param){
		return gangDungeonService.getHurtRankList(playerId);
	}
}
