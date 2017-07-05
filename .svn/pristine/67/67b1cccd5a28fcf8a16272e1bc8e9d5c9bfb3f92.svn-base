package com.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
	 /**
	   * @param args
	   * @throws IOException 
	   */
	  public static void main(String[] args) throws IOException {
		  new UDPServer(8888).launch();
		  
		  // TODO Auto-generated method stub
	     // 创建发送方的套接字，IP默认为本地，端口号随机  
	        DatagramSocket sendSocket = new DatagramSocket();  
	 
	        // 确定要发送的消息：  
//	        String mes = "我是设备！";  
	 
	        // 由于数据报的数据是以字符数组传的形式存储的，所以传转数据  
	        byte[] buf = new byte[8];
	        buf[3] = 1;
	        buf[7] = 100;
	 
	        // 确定发送方的IP地址及端口号，地址为本地机器地址  
	        int port = 9999;  
	        InetAddress ip = InetAddress.getLocalHost();  
	 
	        // 创建发送类型的数据报：  
	        DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, ip,  
	                port);  
	 
	        // 通过套接字发送数据：  
	        sendSocket.send(sendPacket);  
	 
	        sendSocket.close();
	  }
}
