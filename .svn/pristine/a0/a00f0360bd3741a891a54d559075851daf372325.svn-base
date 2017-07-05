package com.test.testnetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.game.util.RandomUtil;

public class NettyClientTest extends Thread {

	private Socket socket;
	private String name;

	public NettyClientTest(String name) {
		this.name = name;
		try {
			this.socket = new Socket("120.132.67.30", 10001);
//			 this.socket = new Socket("192.168.7.67", 22003);
			this.socket.setKeepAlive(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		int index = 1;
		while (true) {
			try {
				OutputStream socketOut = socket.getOutputStream();

				byte[] params = this.name.getBytes();
				ByteBuf data = Unpooled.buffer(4 + params.length);
				data.writeInt(params.length).writeBytes(params);
				socketOut.write(data.array());
				socketOut.flush();

				sleep(RandomUtil.randInt(10000, 50000));
				index++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < 500; i++) {
			sleep(RandomUtil.randInt(100, 300));
			new NettyClientTest("atestgdddfafafa" + i).start();
		}
	}

	public static void sleep(int time) {
		try {
			Thread.sleep(RandomUtil.randInt(time - 50, time + 50));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
