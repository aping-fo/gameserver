package com.test;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.game.SysConfig;
import com.game.event.ShutdownHandler;
import com.game.event.StartHandler;
import com.game.util.BeanManager;
import com.game.util.ConfigData;
import com.server.util.GameData;
import com.server.util.ServerLogger;

public class BaseTest {

	public static void init() {
		try {
			SysConfig.init();

			DOMConfigurator.configure("config/log4j.xml");
			ServerLogger.info("begin init server...");

			ServerLogger.info("load game config...");
			GameData.loadConfigData();
			
			ConfigData.init();

			ServerLogger.info("load spring cfg...");
			ApplicationContext ctx = new FileSystemXmlApplicationContext("config/application.xml");
			BeanManager.init(ctx);
			BeanManager.handleInit();// 加载一些bean的初始化操作

			Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
			StartHandler.start(); // 公共的初始化
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
