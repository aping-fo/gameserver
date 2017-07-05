package com.game.event;

import com.game.module.attach.arena.ArenaLogic;
import com.game.module.attach.training.trainingLogic;
import com.game.module.gang.GangService;
import com.game.module.robot.RobotService;
import com.game.util.BeanManager;
import com.game.util.ServerTimer;


public class StartHandler {

	public static void dispose() {
	}

	/**
	 * 一些公共的初始化操作可以放在这里，server启动的时候会调用 在所有service的init完毕后调用
	 * @throws Exception 
	 */
	public static void start() throws Exception {
		BeanManager.getBean(ServerTimer.class).start();
		//机器人
		BeanManager.getBean(RobotService.class).addRobot();
		//竞技场机器人
		BeanManager.getBean(ArenaLogic.class).autoArenaRobot();
		//英雄试炼
		BeanManager.getBean(trainingLogic.class).resetOpponent();
		//公会任务
		BeanManager.getBean(GangService.class).dailyReset();
	}
}
