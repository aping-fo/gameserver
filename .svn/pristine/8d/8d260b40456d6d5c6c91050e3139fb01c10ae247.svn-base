package com.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Properties;

import com.game.params.IntParam;
import com.server.util.Util;

/**
 * 关闭服务器
 */
public class GameStop {

	private Socket socket;
	private String host;
	private int port;

	 private static String[] CHECK_DEAD_WIN = new String[]{"cmd.exe", "/C", "netstat -ano|findstr port"};
		private static String[] CHECK_DEAD_LINUX = new String[] { "/bin/sh", "-c",
				"netstat -nltp|grep port" };

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File("config/sys.properties")));
		int port = Integer.valueOf(properties.getProperty("port"));

		new GameStop("localhost", port).stop();
		String os = System.getProperty("os.name");
		String[] cmdParam = os.toLowerCase().contains("win")?CHECK_DEAD_WIN:CHECK_DEAD_LINUX;
		cmdParam[2] = cmdParam[2].replace("port", String.valueOf(port));

		System.out.println("Begin to shut down...");
		int count = 0;
		while (true) {
			System.out.println("Server is stopping....");

			Process ps = Runtime.getRuntime().exec(cmdParam);
			ps.waitFor();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					ps.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String info = sb.toString().trim();
			System.out.println(info);
			if (info.isEmpty()) {
				System.out.println("Server has stop!");
				break;
			}
			count++;
			if (count > 20) {
				break;
			}
			Thread.sleep(2000);
		}
		if (count > 10) {
			System.out.println("Fail to stop server!");
		}
	}

	public GameStop(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void stop() throws Exception {
		this.socket = new Socket(host, port);
		
		IntParam param = new IntParam();
		param.param = 1024 * 1 + 9;
		Util.sendSocketData(socket, 9901, param,0,0);
	}

}
