package com.game;

import com.game.params.IntParam;
import com.game.params.LongParam;
import com.game.params.StringParam;
import com.game.params.mail.MailVo;
import com.game.params.player.CRegVo;
import com.game.params.scene.CEnterScene;
import com.server.util.Util;

import java.net.Socket;

/**
 * 模拟发包工具
 */
public class GameClient {

	public static void main(String[] args) throws Exception {
		/*SysConfig.init();

		DOMConfigurator.configure("config/log4j.xml");
		//GameClient client = new GameClient("192.168.6.237", "testa");
		//client.start();159982679 159990708 159973763
		String host;
		int port;
		if(args.length >= 3) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}else {
			host = "192.168.6.237";
			port = 10010;
		}
		System.out.println(System.currentTimeMillis());
		for(int i = 0;i<50;i++) {
			new Robot("苹果-114" + i).start(host,port);
		}*/
	}

	private Socket socket;
	@SuppressWarnings("unused")
	private String name;

	private String host;

	public GameClient(String host, String userName) {
		this.name = userName;
		this.host = host;
	}

	/*public void start() {
		try {

			this.socket = new Socket(host, 10010);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						InputStream in = socket.getInputStream();

						while (true) {
							int avail = in.available();
							if (avail < 2) {
								Thread.sleep(10L);
								continue;
							}
							in.mark(900000);
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

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();


			CRegVo vo = new CRegVo();
			vo.accName = "robot-5";
			vo.name = "robot-5";
			vo.sex = 1;
			vo.vocation = 1;
			getRoleList(vo);
			Util.sendSocketData(socket, 1002, vo,0,0);

			//sleep(100l);

			// testFriend();
			// testCopy();
			// testBag();
			//testMail();
			//testTask();
			//Thread.sleep(100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public void getRoleList(CRegVo vo) {
		StringParam param = new StringParam();
		param.param = vo.accName;
		Util.sendSocketData(socket, 1001, param,0,0);
	}
	public void testCopy() {
		Util.sendSocketData(socket, 1901, new IntParam(),0,0);
		// 进入副本
		IntParam param = new IntParam();
		param.param = 10101;
		Util.sendSocketData(socket, 1902, param,0,0);
		// 切换场景
		CEnterScene param2 = new CEnterScene();
		param2.sceneId = 10003;
		Util.sendSocketData(socket, 1101, param2,0,0);
		// 获取奖励

		IntParam param3 = new IntParam();
		param3.param = 10101;
		Util.sendSocketData(socket, 1903, param3,0,0);

	}

	public void testFriend() {
		// 添加好友
		IntParam p1 = new IntParam();
		p1.param = 124002;
		Util.sendSocketData(socket, 1601, p1,0,0);
		// 获取好友
		Util.sendSocketData(socket, 1603, p1,0,0);
		// 删除好友
		IntParam p2 = new IntParam();
		p2.param = 124002;
		Util.sendSocketData(socket, 1602, p2,0,0);

		// 删除黑名单
		IntParam p4 = new IntParam();
		p4.param = 124002;
		Util.sendSocketData(socket, 1605, p4,0,0);

		// 增加黑名单
		IntParam p3 = new IntParam();
		p3.param = 124002;
		Util.sendSocketData(socket, 1604, p3,0,0);
	}

	public void testBag() {
		// 获取信息
		IntParam p1 = new IntParam();
		p1.param = 124002;
		Util.sendSocketData(socket, 1201, p1,0,0);
		// 穿
		LongParam p2 = new LongParam();
		p2.param = 1;
		Util.sendSocketData(socket, 1202, p2,0,0);
		// 脱
		LongParam p3 = new LongParam();
		p3.param = 1;
		Util.sendSocketData(socket, 1203, p3,0,0);
		// 打开礼包
		LongParam p4 = new LongParam();
		p4.param = 2;
		Util.sendSocketData(socket, 1204, p4,0,0);
	}
	
	public void testMail()
	{
		//发邮件
		MailVo mail = new MailVo();
		
		mail.receiverId = 1002;
		mail.receiverName = "test225";
		mail.title = "测试1";
		mail.content = "测试内容1";
		mail.rewards = "1:10000";
		Util.sendSocketData(socket, 1406, mail,0,0);
		//发邮件
		mail.receiverId = 1002;
		mail.receiverName = "dddd";
		Util.sendSocketData(socket, 1406, mail,0,0);
		//获取邮件列表
		Util.sendSocketData(socket, 1401, new IntParam(),0,0);
		//提取单个
		LongParam id = new LongParam();
		id.param = 1;
		Util.sendSocketData(socket, 1402, id,0,0);
		//设置已读
		Util.sendSocketData(socket, 1405, id,0,0);
		//删除单个
		Util.sendSocketData(socket, 1403, id,0,0);
		//一键提取
		Util.sendSocketData(socket, 1407, new IntParam(),0,0);
		//一键删除
		Util.sendSocketData(socket, 1404, new IntParam(),0,0);
	}

	public void sleep(long mis) {
		try {
			Thread.sleep(mis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testTask(){
		Util.sendSocketData(socket, 1301, new IntParam(),0,0);
	}
}
