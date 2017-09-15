package com.game.module.chat;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.event.ILogin;
import com.game.module.admin.MessageService;
import com.game.util.ThreadService;
import com.game.util.TimerService;

//测试使用
//@Component
public class PlayerLogin4Chat implements ILogin {

	@Autowired
	private MessageService msgService;
	@Autowired
	private ThreadService threadService;
	@Autowired
	private TimerService timerService;
	
	@Override
	public void playerLogined(int playerId) {
		timerService.scheduleDelay(new Runnable(){

			@Override
			public void run() {
				msgService.sendSysMsg(1, "欢迎进行超时空旅馆");
				msgService.sendSysMsg(1, "欢迎进行超时空旅馆");
			}
		}, 10, TimeUnit.SECONDS);
		
	}

	
}
