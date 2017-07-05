package com.tool.code;

public class Code {

	public String name;
	public String type;
	public String comment;
	
	//`rewardType` int(10) unsigned NOT NULL COMMENT '奖励类型，发奖励的时记个类型',
	public Code(String code){
		String[] item = code.split(" ");
		type = item[1];
		name = item[0].replaceAll("`", "");
		if(code.contains("COMMENT '")){
			comment = code.substring(code.indexOf("COMMENT '")+9);
			comment = comment.substring(0, comment.lastIndexOf("'"));
		}
		
	}
	
}
