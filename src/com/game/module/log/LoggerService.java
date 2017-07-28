package com.game.module.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.player.PlayerService;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

@Service
public class LoggerService  implements InitHandler {
	
	public static final String INS_DIAMOND_LOG = "insert into players_diamond_logs(player_id,item_id,op_type,param,count,create_time) values(?,?,?,?,?,now())";
	
	
	private SimpleJdbcTemplate loggerTemplate;//日志库
	private SimpleJdbcTemplate mainTemplate;//主库
	
	@Autowired
	private PlayerService playerService;
	
	public SimpleJdbcTemplate getDb(){
		return mainTemplate;
	}
	
	@Resource(name = "logerDataSource")
	public void setLogDataSource(DataSource dataSource){
		this.loggerTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	@Resource(name = "dataSource")
	public void setDataSource(DataSource dataSource){
		this.mainTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	private static final ScheduledExecutorService dbLogScheduExec = Executors.newScheduledThreadPool(SysConfig.loggerThread,new MyTheadFactory("LogerDb"));
	
	private static ConcurrentLinkedQueue<SQLWrapper> dbLoggers = new ConcurrentLinkedQueue<SQLWrapper>();
	

	
	private static final int count = 100000;
	
	private static final int TYPE_CONSUME = 2;//元宝,铜钱,经验,物品
	private static final int TYPE_DATA = 4;//相关数据
	
	
	public static final int ADD = 1;
	public static final int DEC = 2;
	
	private static AtomicInteger COUNT = new AtomicInteger();
	private static final int MAX_FILE_LOG = 200000;
	private static Map<Integer, String> types = new HashMap<Integer, String>();
	static{
		types.put(TYPE_CONSUME, "consume");
		types.put(TYPE_DATA, "data");
	}
	
	private static Map<Integer,BufferedWriter> writers = new HashMap<Integer,BufferedWriter> ();
	
	//web4399/20121210/web4399___s154___20121210___conislog___daily.txt
	private static  String FILE_NAME="";
	
	
	private String time;
	
	private ConcurrentLinkedQueue<FileLogger> loggers = new ConcurrentLinkedQueue<FileLogger>();
	private static final ScheduledExecutorService scheduExec = Executors.newSingleThreadScheduledExecutor(new MyTheadFactory("FileLogger"));
	
	public void log(int type,Object...params){
		if(COUNT.get()>=MAX_FILE_LOG){
			return;
		}
		COUNT.incrementAndGet();
		loggers.add(new FileLogger(type, params));
	}

	@Override
	public void handleInit() {
		//文件日志
		FILE_NAME = SysConfig.statlogpath+File.separator+SysConfig.platform+File.separator+"%s"+File.separator+SysConfig.platform+"_s%d_%s_%s_daily.txt";
		time = getDateStr();
		initFileWriter();
		scheduExec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try{
					handleLogFile(false);
				}catch(Exception e){
					ServerLogger.err(e, "handle log file err!");
				}
			}
		}, 20, 20, TimeUnit.SECONDS);
		
		//数据库日志
		dbLogScheduExec.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try{
					handleLogDb(false);
				}catch(Exception e){
					ServerLogger.err(e, "log db err");
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
		
	}

	public void logDiamond(int playerId, int count,int logId, boolean add,Object...params){
		addDbLogger(INS_DIAMOND_LOG, playerId,logId,add?ADD:DEC,StringUtils.join(params,","),count);
	}
	
	//根据不同的表归类批量插入
	private  void handleLogDb(boolean isShutDown){
		Map<String, List<Object[]>> logs = new HashMap<String, List<Object[]>>();
		for(int i=0;i<SysConfig.loggerBatchCount;i++){
			SQLWrapper log = dbLoggers.poll();
			if(log==null){
				break;
			}
			List<Object[]> params = logs.get(log.getSql());
			if(params==null){
				params = new LinkedList<Object[]>();
				logs.put(log.getSql(), params);
			}
			params.add(log.getParams());
		}
		for(Entry<String, List<Object[]>> entry:logs.entrySet()){
			loggerTemplate.batchUpdate(entry.getKey(), entry.getValue());
		}
		
		//关闭后，递归调用
		if(isShutDown){
			if(loggers.isEmpty()){
				return;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handleLogDb(true);
		}
	}
	
	
	//写入文件
	public void handleLogFile(boolean dispose) throws Exception{
		String now = getDateStr();
		if(!now.equals(time)){//第二天了
			time = now;
			flushWriter(true);
			initFileWriter();
		}
		for(int i=0;i<count;i++){
			FileLogger log = loggers.poll();
			if(log==null){
				break;
			}
			COUNT.decrementAndGet();
			BufferedWriter writer = writers.get(log.type);
			writer.append(log.params).append("\n");
		}
		flushWriter(false);
		if(dispose){//关闭
			if(loggers.isEmpty()){
				flushWriter(true);
				return;
			}
			handleLogFile(true);
		}
	}
	
	//初始化FileWriter
	private void initFileWriter(){
		try{
		String date = time;
		for(Entry<Integer, String> entry:types.entrySet()){
			String fileName = String.format(FILE_NAME, date,(SysConfig.serverId),date,entry.getValue());
			File file = new File(fileName);
			if(!file.exists()){
				File parent = file.getParentFile();
				if(!parent.exists()){
					parent.mkdirs();
				}
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true),"UTF-8"));
			writers.put(entry.getKey(), writer);
		}
		}catch(Exception e){
			ServerLogger.err(e, "init file writer err!");
		}
	}
	
	//刷新流，可选择是否关闭
	private void flushWriter(boolean close) throws Exception{
		for(BufferedWriter writer:writers.values()){
			writer.flush();
			if(close){
				writer.close();
			}
		}
	}
	
	//获得当前的日期
	private String getDateStr(){
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(time);
	}
	
	public void dispose(){
		scheduExec.shutdown();
		try {
			handleLogFile(true);
		} catch (Exception e) {
			ServerLogger.err(e, "file logger dispose err!");
		}
		
		dbLogScheduExec.shutdown();
		ServerLogger.info("remain: "+loggers.size()+" log");
		handleLogDb(true);
		try {
			scheduExec.awaitTermination(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 消耗日志
	 */
	public void logConsume(int playerId,int lev,int vipLev,boolean add,int count,LogConsume log,int goodsId,int goodsType,Object...params){
		if(log==null){
			return;
		}

		Object p[] = new Object[]{playerId,lev,vipLev,add?ADD:DEC,count,goodsType,goodsId,log.actionId,System.currentTimeMillis(),0,0,0,0};
		if(params!=null&&params.length>0){
			int len = params.length;
			if(params.length>4){
				ServerLogger.warn("err logger param count.must be less than 4");
				len = 4;
			}
			for(int i=0;i<len;i++){
				p[9+i]=params[i];
			}
		}
		log(TYPE_CONSUME, p);
	}
	
	
	//插入数据库日志
	public void addDbLogger(String sql,Object...params){
		dbLoggers.add(new SQLWrapper(sql, params));
	}
}
