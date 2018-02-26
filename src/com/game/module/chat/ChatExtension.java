package com.game.module.chat;

import com.game.SysConfig;
import com.game.module.admin.ManagerService;
import com.game.module.admin.MessageService;
import com.game.module.admin.UserManager;
import com.game.module.gang.Gang;
import com.game.module.gang.GangService;
import com.game.module.gm.GmService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsService;
import com.game.module.group.GroupService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.ListParam;
import com.game.params.chat.ChatVo;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Extension
public class ChatExtension {
	public static final int WORLD = 1;
	public static final int PRIVATE = 2;
	public static final int GANG = 3;
	public static final int SYS = 4;
	public static final int TEAM =5;
	public static final int LABA =6;

	@Autowired
	private GmService gmService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private ManagerService managerService;
	@Autowired
	private GangService gangService;
	@Autowired
	private PlayerDao playerDao;
	@Autowired
	private TaskService taskService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private ChatService chatService;
	@Autowired
	private SceneService sceneService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private GroupService groupService;
	private Map<Integer, Long> talkTime = new ConcurrentHashMap<Integer, Long>();

	@Command(1501)
	public Object chat(int playerId,ChatVo vo){
		UserManager ban = managerService.getBanInfo(playerId);
		if(ban!=null&&ban.getBanChat()>0){
			return null;
		}
		
		String content = vo.content;
		if(content == null) {
			return null;
		}
//		if(vo.recordUrl==null){
//			return null;
//		}
		if(content.length()>300){
			return null;
		}
		Player sender = playerService.getPlayer(playerId);
		//处理GM
		if(content.startsWith("@")){
			if(SysConfig.gm){
				gmService.handle(playerId, vo.content);
				return null;
			}else{
				if(ConfigData.accountSet.contains(sender.getAccName())){
					gmService.handle(playerId, vo.content);
					return null;
				}
				//vo.content = content.replaceAll("@", "*");
			}
		}
		

		vo.sender = sender.getName();//防外挂
		vo.senderId = sender.getPlayerId();
		vo.senderVip = sender.getVip();
		if(SysConfig.isJapan()){
			vo.senderVip = 0;
		}
		vo.senderVocation = sender.getVocation();
		vo.fight = sender.getFight();
		int chatType = 0;
		//世界
		if(vo.channel==WORLD || vo.channel == SYS){
			if(vo.channel==WORLD && sender.getLev()<ConfigData.globalParam().worldChatLevel){
				return null;
			}
			if(vo.broadcast) {
				chatType = LABA;
				boolean ret = goodsService.decGoodsFromBag(playerId, Goods.HORN_ID,1, LogConsume.WORLD_HORN,Goods.HORN_ID);
				if(!ret) {
					return null;
				}
			}
			Long lastTime = talkTime.get(playerId);
			long now = System.currentTimeMillis();
			if(lastTime!=null&&now-lastTime<TimeUtil.ONE_SECOND*1){
				return null;
			}
			talkTime.put(playerId, now);
			messageService.addChatVo(vo);

			if(vo.channel == WORLD){
				chatType = WORLD;
			}
		//私聊
		}else{ 
			ListParam<ChatVo> result = new ListParam<ChatVo>();
			result.params = new ArrayList<ChatVo>();
			result.params.add(vo);
			if(vo.channel==PRIVATE){
				int receiveId = vo.receiveId;
				PlayerData receiverData = playerService.getPlayerData(receiveId);
				if(receiverData.getBlack().containsKey(playerId)){
					return null;
				}
				if(!receiverData.getFriends().containsKey(playerId) && sender.getLev() < ConfigData.globalParam().personChatLev){
					return null;
				}
				if(!SessionManager.getInstance().isActive(receiveId)){		
					vo.time = System.currentTimeMillis();
					chatService.addOffChat(receiveId, vo);
				}else{
					SessionManager.getInstance().sendMsg(CHAT, result, receiveId);				
				}
				LinkedHashMap<Integer, Boolean> tmp = receiverData.getRecentContacters();
				if(tmp.get(playerId) == null){
					tmp.put(playerId, true);
				}
				PlayerData senderData = playerService.getPlayerData(playerId);
				tmp = senderData.getRecentContacters();
				if(tmp.get(receiveId) == null){
					tmp.put(receiveId, true);
				}
				
			//帮派
			}else if(vo.channel==GANG){
				chatType = GANG;
				int gangId = sender.getGangId();
				if(gangId>0){
					Gang gang = gangService.getGang(gangId);
					
					for(int memberId:gang.getMembers().keySet()){
						SessionManager.getInstance().sendMsg(CHAT, result, memberId);
					}
				}
			}else if(vo.channel == TEAM){
				chatType = TEAM;
				if(sender.getTeamId() > 0){
					sceneService.brocastToSceneCurLine(sender, CHAT, result);
				}else if(sender.getGroupId() > 0) {
					groupService.chat(sender,CHAT,result);
				}
			}
		}
		taskService.doTask(playerId, Task.TYPE_CHAT,chatType,1);
		return null;
	}
	
	public static final int CHAT = 1501;//聊天
	public static final int SYS_NOTICE=1502;//公告
	public static final int CHECK_MSG = 1504;//检测消息
}
