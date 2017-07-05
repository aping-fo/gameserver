package com.game.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.game.SysConfig;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

/**
 * 定时器服务
 * 
 * @author luojian
 */
@Component
public class TimerService {

	private final ScheduledExecutorService scheduledService;

	public TimerService() {
		scheduledService = Executors.newScheduledThreadPool(
				SysConfig.scheduledThread, new MyTheadFactory("ScheThread"));
	}

	/**
	 * 以一定的频率执行
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable runnable,
			long initDelay, long period, TimeUnit timeUnit) {
		return scheduledService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					ServerLogger.err(e, "scheduled at fix rate err!");
				}

			}
		}, initDelay, period, timeUnit);
	}

	/**
	 * 延迟多少时间后执行
	 */
	public ScheduledFuture<?> scheduleDelay(final Runnable runnable,
			long delay, TimeUnit timeUnit) {
		return scheduledService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					ServerLogger.err(e, "scheduled err!");
				}
			}
		}, delay, timeUnit);
	}

	/**
	 * 每次执行完再间隔时间执行
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable runnable,
			int initDelay, long delay, TimeUnit unit) {
		return scheduledService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					ServerLogger.err(e, "scheduled with delay err!");
				}

			}
		}, initDelay, delay, unit);
	}

}
