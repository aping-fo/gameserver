package com.game.util;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.game.module.admin.ManagerService;
import com.game.module.log.LoggerService;
import com.game.module.task.TaskService;
import com.server.util.ServerLogger;

/**
 * 一些公共的bean可以放在这里，避免了@AutoWare
 * 
 * @author luojian
 */
@Service
public class Context {

	private static ThreadService threadService;// 线程管理
	private static TimerService timerService;// 定时器系统
	private static ManagerService managerService;// 后台管理
	private static LoggerService loggerService;// 日志系统
	private static TaskService taskService;// 任务系统

	
	public static ThreadService getThreadService() {
		return Context.threadService;
	}

	public static TimerService getTimerService() {
		return timerService;
	}

	@Resource
	public void setThreadService(ThreadService service) {
		Context.threadService = service;
	}

	@Resource
	public void setTimerService(TimerService timerService) {
		Context.timerService = timerService;
	}

	public static ManagerService getManagerService() {
		return managerService;
	}

	@Resource
	public void setManagerService(ManagerService managerService) {
		Context.managerService = managerService;
	}

	public static LoggerService getLoggerService() {
		return loggerService;
	}

	@Resource
	public void setLoggerService(LoggerService loggerService) {
		Context.loggerService = loggerService;
	}

	public static TaskService getTaskService() {
		return taskService;
	}

	@Resource
	public void setTaskService(TaskService taskService) {
		Context.taskService = taskService;
	}

	// 批量数据库操作
	public static void batchDb(final String sql, final List<Object[]> params) {
		Context.getThreadService().execute(new Runnable() {
			int perCount = 0;

			@Override
			public void run() {
				boolean err = false;
				while (!params.isEmpty()) {
					perCount = params.size() >= 500 ? 500 : params.size();
					List<Object[]> perlist = params.subList(0, perCount);
					try {
						Context.getLoggerService().getDb().batchUpdate(sql, perlist);
						Thread.sleep(1000);
					} catch (Exception e) {
						ServerLogger.err(e, "Batch Db Thread Err!");
						err = true;
						break;
					}
					perlist.clear();
				}
				if (err) {
					for (Object[] p : params) {
						try {
							Context.getLoggerService().getDb().update(sql, p);
							Thread.sleep(5);
						} catch (Exception e) {
							ServerLogger.err(e, "Batch Db Single Err!"+StringUtils.join(params," , "));
						}
					}
				}
			}
		});
	}
}
