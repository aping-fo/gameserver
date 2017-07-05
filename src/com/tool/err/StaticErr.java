package com.tool.err;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticErr {

	private static Map<String, Integer> errs = new ConcurrentHashMap<String, Integer>();
	private static final String filePath = "E:\\s1_log\\logs\\err\\err.log.2016-05-12";
	
	public static void main(String[] args) throws Exception {
		
		StringBuilder content = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line = reader.readLine();
		while(line!=null){
			content.append(line).append("\r\n");
			line = reader.readLine();
		}
		reader.close();
		
		Pattern p = Pattern.compile("Caused by:.*\r\n(.*at.*\r\n)+");  
		Matcher m = p.matcher(content.toString());  
		while(m.find()){  
		    String exception = m.group();  
		    Integer count = errs.get(exception);
		    if(count==null){
		    	count = 0;
		    }
		    count++;
		    errs.put(exception, count);
		}
		
		p = Pattern.compile("\\[WARN \\] .*: (.*)\r\n");  
		m = p.matcher(content.toString());  
		while(m.find()){  
		    String exception = m.group(1);  
		    Integer count = errs.get(exception);
		    if(count==null){
		    	count = 0;
		    }
		    count++;
		    errs.put(exception, count);
		}
		
		for(Entry<String, Integer> item:errs.entrySet()){
			System.out.println(item.getKey()+"-----"+item.getValue());
		}
		
	}

}
