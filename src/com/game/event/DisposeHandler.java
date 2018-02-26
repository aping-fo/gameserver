package com.game.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.util.BeanManager;
import com.server.SessionManager;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

/**
 * 清除缓存
 */
@Service
public class DisposeHandler implements InitHandler {
	
	public static final ScheduledExecutorService scheduExec = Executors
			.newScheduledThreadPool(SysConfig.disposeThread,
					new MyTheadFactory("Dispose"));

	private static List<Dispose> disposeHandlers = new ArrayList<Dispose>();

	public static void dispose() {
		scheduExec.shutdown();
	}

	public static void dispose(final int playerId) {
		scheduExec.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					removeCache(playerId);
				} catch (Exception e) {
					ServerLogger.err(e, "dipose err");
				}
			}
		}, SysConfig.delayDispose, TimeUnit.SECONDS);
	}

	public static boolean removeCache(int playerId) {
		// 又重新登录了
		if(SessionManager.getInstance().isActive(playerId)){
			return false;
		}
		
		for (Dispose handler : disposeHandlers) {
			try {
				handler.removeCache(playerId);
			} catch (Exception e) {
				ServerLogger.err(e, "handle remove cache err!");
			}
		}
		return true;
	}

	@Override
	public void handleInit() {
		disposeHandlers.addAll(BeanManager.getApplicationCxt()
				.getBeansOfType(Dispose.class).values());
	}

}
