package com.game;

import java.util.concurrent.TimeUnit;

import com.game.util.Context;
import com.server.util.ServerLogger;
import com.test.BaseTest;

public class Patch {

	public static void main(String[] args) {
		BaseTest.init();
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			ServerLogger.err(e, "sleep err!");
		}
		ServerLogger.warn("handle begin!");

		ServerLogger.warn("handle over!");
		Context.getTimerService().scheduleDelay(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 10, TimeUnit.SECONDS);
	}

}
