package com.game.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.event.ServiceDispose;
import com.game.module.vip.VipExtension;
import com.game.params.IntParam;
import com.server.SessionManager;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

/**
 * 定时执行的任务
 */
@Service
public class ServerTimer implements InitHandler {

	private final ScheduledExecutorService scheduExec = Executors
			.newScheduledThreadPool(SysConfig.timerThread, new MyTheadFactory(
					"ServerTimer"));

	private List<TimerObject> timers = new ArrayList<TimerObject>();

	public void dispose() {
		scheduExec.shutdown();
	}

	/**
	 * 将需要定时/定期执行的函数加入队列 参数1 cron:* 1/2 2-4 2 4,5 分别代表:分 时 日 月 星期 年，用空格分开每个选项
	 * 1.n/m 表示n开始，递增m，直到这个域的上限 2.n-m 表示n到m之间的数值 3.n,m 表示 列举数字中的一个 4.星号*表示任意的
	 * 5.具体的数值
	 * 
	 * 参数2 是service的名字，类名的首字母小写 参数3是方法名，该方法不带参数的
	 */
	@Override
	public void handleInit() {
		// 凌晨0点执行
		timers.add(new TimerObject("0 0 * * * *", "serverTimer", "updateTimeStr"));
		// 凌晨5点执行 每日数据重置
		timers.add(new TimerObject("0 5 * * * *", "dailyService", "resetFiveClock"));
		timers.add(new TimerObject("0 5 * * * *", "trainingLogic", "resetOpponent"));
		timers.add(new TimerObject("0 5 * * * *", "dailyService", "reset"));
		
		// 凌晨5点执行 每周数据重置
		timers.add(new TimerObject("0 5 * * 2 *", "dailyService", "resetWeekly"));
		// 每小时17分执行保存数据
		timers.add(new TimerObject("17 0-23 * * * *", "serverTimer", "saveData"));
		// 凌晨0点执行帮派每日
		timers.add(new TimerObject("2 5 * * * *", "gangService", "daily"));
		// 每小时数据帮派数据
		timers.add(new TimerObject("50 0-23 * * * *", "gangService", "update"));
		// 每小时帮派排名
		timers.add(new TimerObject("0 * * * * *", "gangService", "sort"));
		// 公会每天维护
		timers.add(new TimerObject("0 5 * * * *", "gangService", "daily"));
		//活动配置检测
		//timers.add(new TimerObject("0 * * * * *", "activityService","checkActivities"));
		//商城刷新
		timers.add(new TimerObject(toTimerFormat(ConfigData.globalParam().shopRefreshTime), "shopService", "refreshCommon"));
		//无尽漩涡排行榜重置，发奖
		timers.add(new TimerObject("0 5 * * * *", "endlessLogic", "sendReward"));
		timers.add(new TimerObject("0 5 * * * *", "arenaLogic", "sendRankReward"));
		timers.add(new TimerObject("0 * * * * *", "rankService", "sort"));

		// 凌晨0检测
		//timers.add(new TimerObject("0 0 * * 2 *", "ladderService", "weeklyAward"));
		// 凌晨0检测
		//timers.add(new TimerObject("0 0 * * 2 *", "gangDungeonService", "weekly"));
		// 延时活动奖励检测
		timers.add(new TimerObject("0 0 * * * *", "activityService", "doScheduleCheckActivityTask"));
		// 活动日常数据重置
		timers.add(new TimerObject("0 5 * * * *", "activityService", "resetDailyData"));
		updateTimeStr();
	}
	
	public String toTimerFormat(String time){
		String[] data = time.split("\\:");
		return String.format("%s %s * * * *", data[1],data[0]);
	}
	
	// 中途增加timer
	public void addTimer(String cron,String service,String function){
		timers.add(new TimerObject(cron, service, function));
	}
	
	public void updateTimeStr(){
		// 更新每天的时间字符串
		Date nowTime=new Date(); 
		SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd");
		TimeUtil.CUR_TIME_FORMAT = time.format(nowTime);
		
		SysConfig.updateOpenDays();
		
		// 更新开服天数给前端
		IntParam msg = new IntParam();
		msg.param = SysConfig.getOpenDays();
		SessionManager.getInstance().sendMsgToAll(VipExtension.UPDATE_ZERO_CLOCK, msg);
	}

	public void start() {
		int second = Calendar.getInstance().get(Calendar.SECOND);
		scheduExec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Calendar now = Calendar.getInstance();
				int time[] = new int[] { now.get(Calendar.MINUTE),
						now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.DATE),
						now.get(Calendar.MONTH) + 1,
						now.get(Calendar.DAY_OF_WEEK), now.get(Calendar.YEAR) };
				for (TimerObject timer : timers) {
					try {
						if (timer.check(time)) {
							long begin = System.currentTimeMillis();
							timer.getMethod().invoke(timer.getService());
							long end = System.currentTimeMillis();
							if ((end - begin) > 4000) {
								ServerLogger
										.warn("===========long timer:", timer
												.getService().getClass()
												.getName(), timer.getMethod()
												.getName(), end - begin);
							}
						}
					} catch (Exception e) {
						ServerLogger.err(e, "handle server timer err!");
					}
				}
			}
		}, 60 - second + 1, 60, TimeUnit.SECONDS);
	}
	
	/**
	 * 保留系列化数据
	 */
	public void saveData() {
		for (ServiceDispose dispose : BeanManager.getApplicationCxt()
				.getBeansOfType(ServiceDispose.class).values()) {
			try {
				dispose.serviceDispse();
			} catch (Exception e) {
				ServerLogger.err(e, "save serial data err!");
			}
		}
	}
	
}
