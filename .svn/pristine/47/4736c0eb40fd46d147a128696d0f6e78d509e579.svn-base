package com.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 机器人名称
 */
public class RobotNames {
	
	private static List<String> names = new ArrayList<String>();
	
	//初始化
	public static void init()throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(new File("config/robots.txt")));
		String line=reader.readLine();
		while(line!=null){
			if(!line.trim().isEmpty()){
				names.add(line);
			}
			line = reader.readLine();
		}
		reader.close();
	}
	
	public static List<String> getRobotNames(){
		return names;
	}

}
