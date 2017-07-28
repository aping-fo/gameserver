package com.game.module.admin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.SysNotice;
import com.game.event.InitHandler;
import com.game.module.chat.ChatExtension;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.params.ListParam;
import com.game.params.StringParam;
import com.game.params.chat.ChatVo;
import com.game.util.ConfigData;
import com.server.SessionManager;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

/**
 * 消息服务
 */
@Service
public class MessageService implements InitHandler{
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private SerialDataService serialDataService;
	
	// 每次出来的消息
	private static final int COUNT_PER_TIME = 15;
	// 最大消息量
	private static final int MAX_COUNT = 200; 
	// 丢弃的消息数量
	private static final int DROP_COUNT = 100;
	
	// 当前数量
	private AtomicInteger count = new AtomicInteger(1);
	private ConcurrentLinkedQueue<ChatVo> chats = new ConcurrentLinkedQueue<ChatVo>();
	private static final ScheduledExecutorService scheduExec = Executors.newSingleThreadScheduledExecutor(new MyTheadFactory("Chat"));
	
	public static final String[] COLORS = { "eeeeee", "68e601", "00b4ff", "8500ff", "ef6500", "ff0000", "fff600","fff600" };
	
	// 消息id定义
	public static final int SYS = 1;

	// 发送系统消息
	public String sendSysMsg(int id,Object...params){
		SysNotice notice = ConfigData.getConfig(SysNotice.class, id);
		StringParam param = new StringParam();
		if(notice != null) {
			param.param = MessageFormat.format(notice.msg, params);
			SessionManager.getInstance().sendMsgToAll(ChatExtension.SYS_NOTICE, param);
		}
		return param.param;
	}

	@Override
	public void handleInit() {
		scheduExec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try{
					handleChat();
				}catch(Exception e){
					ServerLogger.err(e, "handle chat queue err!");
				}
			}
		}, 20, 2, TimeUnit.SECONDS);
	}
	
	// 加入队列
	public void addChatVo(ChatVo vo){
		if(count.get()>=MAX_COUNT){
			// 丢弃
			for(int i=0;i<DROP_COUNT;i++){
				ChatVo v = chats.poll();
				if(v==null){
					break;
				}
				count.decrementAndGet();
			}
		}
		chats.add(vo);
		count.incrementAndGet();
	}
	
	// 处理聊天队列
	private void handleChat(){
		ListParam<ChatVo> data = new ListParam<ChatVo>();
		data.params = new ArrayList<ChatVo>();
		for(int i=0;i<COUNT_PER_TIME;i++){
			ChatVo vo = chats.poll();
			if(vo==null){
				break;
			}
			data.params.add(vo);
			count.decrementAndGet();
		}
		if(!data.params.isEmpty()){
			SessionManager.getInstance().sendMsgToAll(ChatExtension.CHAT, data);
		}
	}
}
