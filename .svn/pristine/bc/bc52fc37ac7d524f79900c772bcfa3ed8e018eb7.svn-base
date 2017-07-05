package com.test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.game.params.StringParam;
import com.game.params.chat.ChatVo;
import com.game.params.player.CRegVo;
import com.game.params.player.PlayerVo;
import com.game.params.scene.CEnterScene;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;
import com.server.util.ServerLogger;
import com.server.util.Util;

@SuppressWarnings("unused")
public class ProfileTest extends Thread {

	private Socket socket;
	private String name;
	private int num = 1;
	
	private BufferedOutputStream bos;

	public ProfileTest(String name) {
		this.name = name;
		try {
//			this.socket = new Socket("192.168.7.189",10002);
//			this.socket = new Socket("120.132.67.30", 10001);
			this.socket = new Socket("192.168.7.102", 10001);
			this.socket.setKeepAlive(true);
			socket.setTcpNoDelay(false);
			bos = new BufferedOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void getServerData(){
			try {
				InputStream in = socket.getInputStream();

				while (true) {
					int avail = in.available();
					if (avail < 2) {
						Thread.sleep(10L);
						continue;
					}
					
					in.mark(10000);
					byte[] lenData = new byte[2];
					in.read(lenData, 0, 2);
					int len = Util.bytesToShort(lenData,0);
					if (in.available() < len) {
						in.reset();
						continue;
					}
					

					byte[] data = new byte[2];
					in.read(data, 0, 2);
					int cmd = Util.bytesToShort(data,0);
					System.out.println("Rec cmd:" + cmd);

					data = new byte[len - 2];
					in.read(data);
					
					
					//解析出消息体
					
					
					
					Object param = null;
					try {
						if(cmd==1003){
						// Protobuf解码
						param = new PlayerVo();
						if (param != null) {
							Schema schema = RuntimeSchema.getSchema(param.getClass());
							ProtobufIOUtil.mergeFrom(data, 0,data.length,param, schema);
							//System.out.println(param);
							if(cmd==1003){
								PlayerVo s = (PlayerVo) param;
								num = s.serialNum;
								System.out.println("num:"+num);
								break;
							}
						}
						}
					} catch (Exception e) {
						ServerLogger.err(e, "protobuff decode err:" + cmd);
					}
				
					sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public void run() {
		//int curTask = ConfigData.globalParam().firstTask;

		// 获取角色列表
		StringParam roleName = new StringParam();
		roleName.param = this.name;
		Util.sendSocketData(bos, 1001, roleName,0,0);
		sleep(2000);

		// 创角
		CRegVo regVo = new CRegVo();
		regVo.accName = this.name;
		regVo.name = this.name;
		regVo.sex = 1;
		regVo.vocation = 1;
		Util.sendSocketData(bos, 1002, regVo,0,0);
		sleep(200);
		
		//获取服务端数据
		getServerData();
		
		sleep(300);
		
		// 切换场景
		CEnterScene scene = new CEnterScene();
		scene.sceneId = 10002;
		scene.x = 1.0f;
		scene.z = 1.0f;
		Util.sendSocketData(bos, 1101, scene,++num,0);
		sleep(5000);

		// 登录初始化
		Util.sendSocketData(bos, 1006, null,++num,0);
		sleep(3000);

		int index = 1;
		while (true) {
			/*
			// 进入副本
			IntParam copy = new IntParam();
			copy.param = 10101;
			Util.sendSocketData(socket, 1902, copy);
			sleep(200);

			// 进入副本场景
			scene.sceneId = 20001;
			Util.sendSocketData(socket, 1101, scene);
			sleep(1000);

			// 杀死怪
			for(int i=1;i<=15;i++){
				Int2Param monster = new Int2Param();
				monster.param1 = i;
				monster.param2 = 1;
				Util.sendSocketData(socket, 1905, monster);
				sleep(60);
			}
			sleep(1000);

		
			// 完成任务
			
			if (curTask > 0) {
				// 领取奖励
				CopyResult reward = new CopyResult();
				reward.id = copy.param;
				reward.star = 1;
				Util.sendSocketData(socket, 1903, reward);
				sleep(2000);

				IntParam task = new IntParam();
				task.param = curTask;
				Util.sendSocketData(socket, 1303, task);
				TaskConfig taskCfg = ConfigData.getConfig(TaskConfig.class, curTask);
				if (taskCfg.nextTaskId != null) {
					curTask = taskCfg.nextTaskId[0];
				}
			}
			sleep(1000);

			// 返回主场景
			scene.sceneId = 10002;
			Util.sendSocketData(socket, 1101, scene);
			sleep(1000);

			// 移动
			MoveVo move = new MoveVo();
			move.playerId = 1001;
			move.x = 2;
			move.y = 3;
			move.z = 4;
			Util.sendSocketData(socket, 1104, move);
			sleep(1000);
*/
			if(index%20==0){
				// 发送聊天
				ChatVo chat = new ChatVo();
				chat.channel = 1;
				chat.sender = this.name;
				chat.content = "哈哈，我是闹着玩" + name;
				chat.senderId = 1001;
				chat.senderVocation = 1;
				Util.sendSocketData(bos, 1501, chat,++num,0);
				sleep(200);
			}

			// 心跳
			Util.sendSocketData(bos, 1106, null,0,0);
			try{
				this.socket.getInputStream().skip(this.socket.getInputStream().available());
			}catch(Exception e){
				e.printStackTrace();
			}
			sleep(RandomUtil.randInt(1000, 10000));
			index++;
		}
	}

	public static void main(String[] args) {
		//BaseTest.init();
		for(int i=0;i<200;i++){
			sleep(RandomUtil.randInt(1000, 3000));
			new ProfileTest("z2d"+i).start();
		}
	}
	
	public static void sleep(int time){
		try {
			Thread.sleep(RandomUtil.randInt(time-50, time+50));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
