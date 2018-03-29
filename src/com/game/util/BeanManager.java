package com.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.game.module.serial.SerialDataService;
import org.springframework.context.ApplicationContext;

import com.game.event.InitHandler;

/**
 * 管理同类型的bean，登陆，下线，初始化触发等
 * 
 * @author luojian
 * 
 */
public class BeanManager {

	private static List<InitHandler> initHandlers = new ArrayList<InitHandler>();

	private static ApplicationContext actx;

	public static ApplicationContext getApplicationCxt() {
		return actx;
	}

	public static <T> T getBean(Class<T> clazz) {
		return actx.getBean(clazz);
	}

	public static <T> T getBean(String clazz) {
		return (T)actx.getBean(clazz);
	}

	public static void handleInit() {
		for (InitHandler handler : initHandlers) {
			if(handler instanceof SerialDataService) { //先加载基础数据
				handler.handleInit();
			}
		}
		for (InitHandler handler : initHandlers) {
			if(!(handler instanceof SerialDataService)) {
				handler.handleInit();
			}
		}
	}

	public static void init(ApplicationContext ctx) {
		actx = ctx;

		// 获得有子类
		Map<String, InitHandler> initBeans = ctx
				.getBeansOfType(InitHandler.class);
		if (initBeans != null) {
			// 将所有的子类添加到集合
			initHandlers.addAll(initBeans.values());
		}
	}

}
