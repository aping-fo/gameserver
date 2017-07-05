package com.game;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Properties;

import com.game.module.system.RunClassParam;
import com.server.util.Util;

/**
 * 动态执行某些东西，慎用
 */
public class HotSwap {

	public static void main(String[] args) throws Exception {
		if (args == null || args.length != 1) {
			System.out.println("err args...");
			return;
		}
		String className = args[0];
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File("config/sys.properties")));
		int port = Integer.valueOf(properties.getProperty("port"));

		new HotSwap("localhost", port).run(className);

		System.out.println("over...");

	}
	private Socket socket;
	private String host;

	private int port;

	public HotSwap(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run(String className) throws Exception {
		this.socket = new Socket(host, port);
		
		RunClassParam param = new RunClassParam();
		param.code = 1024 * 2 + 9;
		param.className = className;

		Util.sendSocketData(socket, 9902, param,0,0);
	}


}
