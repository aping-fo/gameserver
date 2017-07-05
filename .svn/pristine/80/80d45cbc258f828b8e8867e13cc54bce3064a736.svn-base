package com.game.module.system;

import io.netty.channel.Channel;

import java.lang.reflect.Method;

import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.anotation.UnLogin;
import com.server.util.Profile;
import com.server.util.ServerLogger;

/**
 * 系统命令
 */
@Extension
public class SystemExtension {

	// 关服
	@UnLogin
	@Command(9901)
	public Object stop(int playerId, IntParam code,Channel channel) {
		if (code == null || code.param != 1024 * 1 + 9) {
			return null;
		}
		ServerLogger.info("rec stop command");
		System.exit(0);
		return null;
	}

	//动态运行class类
	@UnLogin
	@Command(9902)
	public Object runClass(int playerId, RunClassParam param,Channel channel) {
		if (param == null || param.code != 1024 * 2 + 9) {
			return null;
		}
		String className = param.className;
		try {
			Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
			Method method = clazz.getDeclaredMethod("run");
			method.invoke(clazz.newInstance());
		} catch (Exception e) {
			ServerLogger.err(e, "handle reflex class err!");
		}
		return null;
	}

	// 设置性能监控的
	@UnLogin
	@Command(9903)
	public Object setProfile(int playerId, ProfileParam param,Channel channel) {
		if (param == null || param.code != 1024 * 3 + 9) {
			return null;
		}
		boolean profile = param.profile;
		ServerLogger.info("set profile:" + profile);
		Profile.setOpen(profile);
		return null;
	}

}
