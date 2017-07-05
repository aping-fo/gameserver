package com.test;

import java.nio.charset.Charset;

import com.game.module.serial.SerialData;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;

public class JasonMapTest {
	
	public static void main(String[] args) {
		BaseTest.init();
		SerialData data = new SerialData();
		
		String str = JsonUtils.object2String(data);
		byte[] dbData = str.getBytes(Charset.forName("utf-8"));
		byte[] d = CompressUtil.compressBytes(dbData);
		
		System.out.println(d.length);
		
//		String a = "{\"status\":\"ok\",\"data\":{\"id\":\"1888\"}}";
//		Map<String,Object> map = JsonUtils.string2Map(a);
		
	}

}
