package com.game.util;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.server.util.ServerLogger;

public class TimerObject {

	private static final int ALL = -1;// *
	private static final int AMONG = -2;// ,
	private static final int ONE = -3;// 1
	private static final int RANGE = -4;// -
	private static final int INC = -5;// /

	private static final int[][] RANGES = { { 0, 59 }, { 0, 23 }, { 1, 31 },
			{ 1, 12 }, { 1, 7 }, { 2012, 2020 } };// 分,时,日,月,星期,年

	private int[][] crons = new int[6][13];
	private Object service;
	private Method method;

	public TimerObject(String cron, String serviceName, String methodName) {
		try {
			this.service = BeanManager.getApplicationCxt().getBean(serviceName);
			this.method = service.getClass().getMethod(methodName);
			if(method==null){
				ServerLogger.warn("ErrTimer:",serviceName,methodName);
			}
		} catch (Exception e) {
			ServerLogger.err(e, "invalid timer module!");
		}
		parseCron(cron);
	}

	public boolean check(int[] time) {
		for (int i = 0; i < time.length; i++) {
			int t = time[i];
			int[] data = crons[i];
			if (data[0] == ALL) {
				continue;
			} else if (data[0] == ONE) {
				if (t != data[1]) {
					return false;
				}
			} else if (data[0] == AMONG) {
				boolean find = false;
				for (int j = 1; j < data.length; j++) {
					if (data[j] == t) {
						find = true;
						break;
					}
				}
				if (!find) {
					return false;
				}
			} else if (data[0] == RANGE) {
				if (t < data[1] || t > data[2]) {
					return false;
				}
			} else if (data[0] == INC) {
				boolean find = false;
				int r[] = RANGES[i];
				for (int j = 0; true; j++) {
					int val = data[1] + j * data[2];
					if (val == t) {
						find = true;
						break;
					}
					if (val >= r[1]) {
						break;
					}
				}
				if (!find) {
					return false;
				}
			}
		}
		return true;
	}

	private void checkRange(int val, int[] ranges) {
		if (val <= ranges[1] && val >= ranges[0]) {
			return;
		}
		throw new RuntimeException("invalid timer config...");
	}

	public int[][] getCrons() {
		return crons;
	}

	public Method getMethod() {
		return method;
	}

	public Object getService() {
		return service;
	}

	private void parseCron(String cron) {
		String[] arr = cron.split(" ");
		for (int i = 0; i < arr.length; i++) {
			String c = arr[i];
			if (c.contains("*")) {
				crons[i][0] = ALL;
			} else if (c.contains("/")) {
				crons[i][0] = INC;
				String[] data = c.split("\\/");
				for (int j = 0; j < data.length; j++) {
					crons[i][j + 1] = Integer.valueOf(data[j]);
					checkRange(crons[i][j + 1], RANGES[i]);
				}
			} else if (c.contains("-")) {
				crons[i][0] = RANGE;
				String[] data = c.split("\\-");
				for (int j = 0; j < data.length; j++) {
					crons[i][j + 1] = Integer.valueOf(data[j]);
					checkRange(crons[i][j + 1], RANGES[i]);
				}
			} else if (c.contains(",")) {
				Arrays.fill(crons[i], -1);
				crons[i][0] = AMONG;
				String[] data = c.split("\\,");

				for (int j = 0; j < data.length; j++) {
					crons[i][j + 1] = Integer.valueOf(data[j]);
					checkRange(crons[i][j + 1], RANGES[i]);
				}
			} else {
				crons[i][0] = ONE;
				crons[i][1] = Integer.valueOf(c);
				checkRange(crons[i][1], RANGES[i]);
			}
		}
	}
}
