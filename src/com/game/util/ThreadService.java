package com.game.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.game.SysConfig;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

/**
 * 线程池统一管理
 * 
 * @author luojian
 * 
 */
@Component
public class ThreadService {

	private final ExecutorService scheduExec;

	public ThreadService() {
		scheduExec = Executors.newFixedThreadPool(SysConfig.serverThread,
				new MyTheadFactory("ServerThread"));
	}

	/**
	 * 立即执行
	 * 
	 * @param command
	 */
	public void execute(final Runnable command) {
		scheduExec.execute(new Runnable() {
			@Override
			public void run() {
				try {
					command.run();
				} catch (Exception e) {
					ServerLogger.err(e, "handle thread err!");
				}
			}
		});
	}
	


	public void shutdown(){
		scheduExec.shutdown();
		try {
			scheduExec.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ServerLogger.err(e, "shutdown thread err!");
		}
	}
}
