package com.game;

import com.game.module.rank.RankService;
import com.game.sdk.SdkServer;
import com.game.util.Context;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.game.event.ShutdownHandler;
import com.game.event.StartHandler;
import com.game.module.admin.ManagerServer;
import com.game.util.BeanManager;
import com.game.util.ConfigData;
import com.server.codec.LogicHandler;
import com.server.socket.Server;
import com.server.util.GameData;
import com.server.util.ServerLogger;

public class Start {

    private static Start start = new Start();

    public static void main(String[] args) {
        start.init();
    }

    private void init() {
        try {
            SysConfig.init();
            DOMConfigurator.configure("config/log4j.xml");
            ServerLogger.info("begin init server...");

            ServerLogger.info("load game config...");
            GameData.loadConfigData();

            ConfigData.init();

            ServerLogger.info("load spring cfg...");
            ApplicationContext ctx = new FileSystemXmlApplicationContext(
                    "config/application.xml");
            BeanManager.init(ctx);
            BeanManager.handleInit();// 加载一些bean的初始化操作

            Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
            StartHandler.start(); // 公共的初始化

            //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
            if (SysConfig.debug) {
                ResourceLeakDetector.setLevel(Level.PARANOID);
            }

            //延迟一些
            Thread.sleep(3000l);

            new Server(SysConfig.port, LogicHandler.class).init();//启动SocketServer

            //管理后台的webservice
            ManagerServer.start();
            SdkServer.start();
            ServerLogger.warn("server init successfully...port:" + SysConfig.port);
//            BeanManager.getBean(RankService.class).removeInvalidEndlessRank();

            Context.getLoggerService().serverChange(1);//设置服务器开启
        } catch (Exception e) {
            Context.getLoggerService().serverChange(0);//设置服务器关闭
            ServerLogger.err(e, "start server err!");
            System.exit(-1);
        }
    }
}
