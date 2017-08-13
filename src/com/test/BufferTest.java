package com.test;

import com.game.params.BufferBuilder;

public class BufferTest {

	public static void main(String[] args) {
		System.out.println("ssssssssssssss");
		for(int j = 0; j < 10; j++){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(int i = 0; i < 1000000; i++){			
						BufferBuilder buff = new BufferBuilder();
						buff.getBuf().writeBytes(new byte[10000]);
						int count = buff.getBuf().refCnt();
						if(count > 1){				
							System.out.println(buff + " : " + count);
						}
						buff.getBuf().retain();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						buff = null;
						System.gc();
					}
				}
			}).start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
	

}
