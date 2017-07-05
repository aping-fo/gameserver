package com.test;

import com.game.module.player.PlayerData;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;
import com.game.util.StopWatch;

public class JsonTest {

	public static void main(String[] args) {
		
		
		PlayerData data = new PlayerData();
		data.getDailyData().put(111, 111);
		JsonUtils.object2String(data);
		StopWatch.start();
		for(int i=0;i<=20;i++){
			String s = JsonUtils.object2String(data);
			byte[]data2 = CompressUtil.compressBytes(s.getBytes());
			data2 = CompressUtil.decompressBytes(data2);
			JsonUtils.string2Object(new String(data2), PlayerData.class);
		}
		StopWatch.stop("json:");
		
		StopWatch.start();
		for(int i=0;i<=1;i++){
			String s = JsonUtils.object2String(data);
			byte[]data2 = CompressUtil.compressBytes(s.getBytes());
			data2 = CompressUtil.decompressBytes(data2);
			JsonUtils.string2Object(new String(data2), PlayerData.class);
		}
		StopWatch.stop("json:");
		
		
		
		String s = "{\"a\":1,\"c\":\"aab\"}";
		Test a = JsonUtils.string2Object(s, Test.class);
		System.out.println(a);
		
		
	}
	
	static class Test{
		private int a;
		private int b;
		
		public int getA() {
			return a;
		}
		public void setA(int a) {
			this.a = a;
		}
		public int getB() {
			return b;
		}
		public void setB(int b) {
			this.b = b;
		}
		
	}

}
