package com.game.module.activity;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.data.ActivityCfg;
import com.game.event.InitHandler;
import com.game.params.ActivityVo;
import com.game.params.Int2Param;
import com.game.params.ListParam;
import com.game.params.MapVo;
import com.game.util.BeanManager;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.server.util.ServerLogger;

@Service
public class ActivityService implements InitHandler {

	// 开启类型
	public static final int OPEN_LIMIT = 1;
	public static final int OPEN_OTHER = 2;
	public static final int OPEN_OPEN_SERVER = 3;
	public static final int OPEN_LOGIN=4;

	public static final int DAILY_UPDATE_HOUR = 5;
	public static final int DAILY_BEGIN = 0;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	// {活动类型,id}
	public static final Map<Integer, Integer> CUR_ACTIVITY = new ConcurrentHashMap<Integer, Integer>();

	private static ListParam<ActivityVo> ACTIVITY_DATA;

	@Override
	public void handleInit() {

		List<ActivityVo> data = new ArrayList<ActivityVo>(ConfigData.getConfigs(ActivityCfg.class).size());

		for (Object o : ConfigData.getConfigs(ActivityCfg.class)) {
			ActivityCfg cfg = (ActivityCfg) o;
			ActivityVo vo = new ActivityVo();
			vo.id = (short) cfg.id;
			vo.name = cfg.name;
			vo.isOpen = (byte) cfg.IsOpen;
			vo.bigType = (byte) cfg.BigType;
			vo.icon = cfg.icon;
			vo.logo = cfg.logo;
			vo.sortRange = (short) cfg.sortRange;
			vo.showLev = (short) cfg.showLev;
			vo.timeType = (byte) cfg.TimeType;
			vo.startTime = cfg.StartTime;
			vo.endTime = cfg.EndTime;
			vo.forwardView = (byte) cfg.ForwardView;
			vo.uiConfig = cfg.uiConfig;
			vo.type = (short) cfg.Type;
			vo.guildId = cfg.GuildID;
			vo.content = cfg.Content;
			// 奖励字段
			vo.rewards1 = format(cfg.rewards1);
			// 排名奖励
			vo.rankRewards = format(cfg.rankRewards);

			data.add(vo);
		}
		ACTIVITY_DATA = new ListParam<ActivityVo>();
		ACTIVITY_DATA.params = data;
	}

	private List<MapVo> format(Map<Integer, int[][]> rewards) {
		if (rewards == null) {
			return new ArrayList<MapVo>();
		}
		List<MapVo> vo = new ArrayList<MapVo>(rewards.size());
		for (Entry<Integer, int[][]> reward : rewards.entrySet()) {
			MapVo m = new MapVo();
			m.rewards = new ArrayList<Int2Param>();
			m.id = reward.getKey();
			for (int i = 0; i < reward.getValue().length; i++) {
				Int2Param p = new Int2Param();
				p.param1 = reward.getValue()[i][0];
				p.param2 = reward.getValue()[i][1];
				m.rewards.add(p);
			}
			vo.add(m);
		}
		return vo;
	}

	// 检测活动
	public void checkActivities() {
		Context.getThreadService().execute(new Runnable() {
			@Override
			public void run() {
				try {
					handleActivities();
				} catch (Exception e) {
					ServerLogger.err(e, "handle activities err!");
				}
			}
		});
	}

	// 处理每个活动
	private void handleActivities() throws Exception {
		CUR_ACTIVITY.clear();
		// 当前时间
		int openDay = SysConfig.getOpenDays();
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);

		List<ActivityCfg> cfgs = new ArrayList<ActivityCfg>(ConfigData.getConfigs(ActivityCfg.class).size());
		for (Object cfg : ConfigData.getConfigs(ActivityCfg.class)) {
			cfgs.add((ActivityCfg) cfg);
		}

		Collections.sort(cfgs, new Comparator<ActivityCfg>() {
			@Override
			public int compare(ActivityCfg o1, ActivityCfg o2) {
				return o1.id - o2.id;
			}
		});

		for (ActivityCfg activity : cfgs) {
			// 关闭
			if (activity.IsOpen == 0) {
				continue;
			}
			// 永久的不处理
			if (activity.TimeType == OPEN_OTHER) {
				continue;
			}
			// 按照开服时间的
			else if (activity.TimeType == OPEN_OPEN_SERVER) {
				// 开始
				int begin = Integer.valueOf(activity.StartTime) + 1;
				if (hour == DAILY_BEGIN && openDay == begin) {
					handleActivity(activity.beginHandler,activity);
				}
				// 结束
				int end = Integer.valueOf(activity.EndTime) + 1;
				if (hour == DAILY_BEGIN && openDay == end) {
					handleActivity(activity.endHandler,activity);
				}
				// 每日
				if (hour == DAILY_UPDATE_HOUR) {
					if (openDay >= begin && openDay < end) {
						handleActivity(activity.dailyHandler,activity);
					}
				}
			} else if (activity.TimeType == OPEN_LIMIT) {
				Calendar begin = parse(activity.StartTime);
				Calendar end = parse(activity.EndTime);
				if (equal(now, begin)) {
					handleActivity(activity.beginHandler,activity);
				}
				if (equal(now, end)) {
					handleActivity(activity.endHandler,activity);
				}
				if (hour == DAILY_UPDATE_HOUR) {
					if (now.after(begin) && now.before(end)) {
						handleActivity(activity.dailyHandler,activity);
					}
				}
			}
		}
	}

	private void handleActivity(String[] arrDaily,ActivityCfg activity) throws Exception {
		if (arrDaily != null) {
			CUR_ACTIVITY.put(activity.Type, activity.id);
			Object service = BeanManager.getApplicationCxt().getBean(arrDaily[0]);
			Method method = service.getClass().getMethod(arrDaily[1]);
			try {
				method.invoke(service);
			} catch (Exception e) {
				ServerLogger.err(e, "Handle Activity Err!");
			}
		}
	}

	public static Calendar parse(String time) {
		try {
			Date d = DATE_FORMAT.parse(time);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			return c;
		} catch (Exception e) {
			ServerLogger.err(e, "Parse Time Error!");
			return null;
		}
	}

	// 判断两个时间是否相等,比较到小时
	public boolean equal(Calendar time1, Calendar time2) {
		return time1.get(Calendar.YEAR) == time2.get(Calendar.YEAR)
				&& time1.get(Calendar.MONTH) == time2.get(Calendar.MONTH)
				&& time1.get(Calendar.DATE) == time2.get(Calendar.DATE)
				&& time1.get(Calendar.HOUR_OF_DAY) == time2.get(Calendar.HOUR_OF_DAY);
	}

	// 活动配置表
	public ListParam<ActivityVo> getActivityCfgs() {
		return ACTIVITY_DATA;
	}

}
