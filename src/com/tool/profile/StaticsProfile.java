package com.tool.profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticsProfile {

	private static final String filePath = "c:\\profile";
	
	private static final Map<Integer, CmdInfo> cmds = new ConcurrentHashMap<Integer, CmdInfo>();
	
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line=reader.readLine();
		while(line!=null){
			String[] info = line.split(",");
			if(info==null||info.length!=2){
				continue;
			}
			int cmd = Integer.valueOf(info[0]);
			int time = Integer.valueOf(info[1]);
			CmdInfo cmdInfo = cmds.get(cmd);
			if(cmdInfo==null){
				cmdInfo = new CmdInfo();
				cmdInfo.cmd = cmd;
				cmds.put(cmd, cmdInfo);
			}
			cmdInfo.count++;
			cmdInfo.totalTime+=time;
			if(time>cmdInfo.maxTime){
				cmdInfo.maxTime = time;
			}
			cmds.put(cmd, cmdInfo);
			line = reader.readLine();
		}
		
		reader.close();
		
		for(CmdInfo cmd:cmds.values()){
			cmd.averageTime = (cmd.totalTime*1.0f)/cmd.count;
			System.out.println(String.format("%s\t%s\t%s\t%s", cmd.cmd,cmd.averageTime,cmd.count,cmd.maxTime));
		}
		
		
		
	}

}
