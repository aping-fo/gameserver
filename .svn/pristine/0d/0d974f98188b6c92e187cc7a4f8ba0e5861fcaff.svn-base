package com.game.module.friend;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntList;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.StringParam;
import com.game.params.friend.FriendVo;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class FriendExtension {
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private FriendService friendService;
	
	// 添加好友
	@Command(1601)
	public Object add(int playerId,IntList requests){
		IntParam result = new IntParam();
		result.param = friendService.addRequest(playerId, requests.iList);
		
		return result;
	}
	
	// 删除好友
	@Command(1602)
	public Object del(int playerId,IntParam param){
		Integer friend = param.param;
		friendService.del(playerId, friend);
		return new IntParam();
	}
	
	// 好友列表
	@Command(1603)
	public Object getList(int playerId,Object param){
		return friendService.getFriendInfo(playerId);
	}
	
	// 删除黑名单
	@Command(1604)
	public Object delBlack(int playerId,IntParam param){
		int friend =param.param;
		PlayerData playerData = playerService.getPlayerData(playerId);
		playerData.getBlack().remove(friend);
		playerData.getRecentContacters().put(friend, true);
		
		return param;
	}
	
	// 添加黑名单
	@Command(1605)
	public Object addBlack(int playerId,IntParam param){
		int friend = param.param;
		ConcurrentHashMap<Integer, Boolean> blacks = playerService.getPlayerData(playerId).getBlack();
		blacks.put(param.param, true);
		friendService.del(playerId, friend);
		PlayerData playerData2 = playerService.getPlayerData(friend);
		if(playerData2 != null){
			playerData2.getFriends().remove(playerId);
			IntParam msg = new IntParam();
			msg.param = playerId;
			SessionManager.getInstance().sendMsg(BE_BACKLIST, msg, friend);
		}
		return param;
	}
	
	// 同意
	@Command(1606)
	public Object approve(int playerId,IntList ids){
		Int2Param result = friendService.approve(playerId, ids.iList);
		return result;
	}
	
	// 拒绝
	@Command(1607)
	public Object refuse(int playerId,IntList ids){
		IntParam result = new IntParam();
		friendService.refuse(playerId, ids.iList);
		return result;
	}
	
	// 查找好友
	@Command(1610)
	public Object find(int playerId,StringParam name){
		return friendService.find(playerId, name.param);
	}
	
	// 获取在线玩家
	@Command(1611)
	public Object getOnline(int playerId,Object param){
		ListParam<FriendVo> friends = new ListParam<FriendVo>();
		friends.params = friendService.getOnlines(playerId);
		return friends;
	}

	public static final int AGREE_REQUEST = 1612;
	
	public static final int BE_REQUESTED = 1613;
	
	public static final int STATE_CHANGE = 1614;
	
	public static final int BE_BACKLIST = 1615;
}
