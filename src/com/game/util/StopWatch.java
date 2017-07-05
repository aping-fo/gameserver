package com.game.util;

public class StopWatch {
	
	private static long start;
	
	public static void start(){
		start = System.currentTimeMillis();
	}
	
	public static void stop(String msg){
		long useTime = System.currentTimeMillis()-start;
		System.out.println(msg+":"+useTime);
		start = System.currentTimeMillis();
	}

}
