package com.game.event;

public interface InitHandler {

	/**
	 * 只要一个Bean实现了这个接口，服务器启动的时候会调用，用于加载一些配置
	 */
	public void handleInit();
}
