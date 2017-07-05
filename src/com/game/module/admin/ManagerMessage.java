package com.game.module.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.game.event.InitHandler;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

@Component
public class ManagerMessage implements InitHandler {
	private static Socket socket;
	public static Integer port;
	public static String server;
	public static boolean isOpen;

	public void initServer() {
		try {
			socket = new Socket(server, port);
			socket.setKeepAlive(true);
			socket.setSoTimeout(10000);
			socket.setTcpNoDelay(false);
		} catch (Exception e) {
			ServerLogger.warn("init msg server err!");
		}
	}

	// 断开远程服务器
	public void downServer() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			ServerLogger.err(e,"Error tearing down socket connection!");
		}
	}

	private void sendMsg(String msg) {
		try {
			OutputStream socketOut = socket.getOutputStream();
			socketOut.write(msg.getBytes());
			socketOut.flush();
		} catch (Exception e) {
			ServerLogger.err(e, "send monitor msg err!");
		}
	}

	private int limit = 20000;
	private static ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();

	private static final ScheduledExecutorService scheduExec = Executors
			.newSingleThreadScheduledExecutor(new MyTheadFactory("MessageMonitor"));

	public boolean checkConnect() {
		try {
			socket.sendUrgentData(0xFF);
			return true;
		} catch (Exception ex) {
			return reconnect();
		}
	}

	public boolean reconnect() {
		downServer();
		initServer();
		try {
			socket.sendUrgentData(0xFF);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public void handleInit() {
		if(!isOpen){
			return;
		}
		initServer();
		scheduExec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					//万一消息服务器down了，也把消息移除，以免队列堆积
					boolean connected = checkConnect();
					
					for(int i=0;i<limit;i++){
						String msg = messages.poll();
						if(msg==null){
							break;
						}
						if(!connected){
							continue;
						}
						sendMsg(msg);
					}
				} catch (Exception e) {
					ServerLogger.err(e, "handle msg monitor send err!");
				}
			}
		}, 20, 20, TimeUnit.SECONDS);
	}
	
	public static void addMsgToQueue(String msg){
		messages.add(msg);
	}

}
