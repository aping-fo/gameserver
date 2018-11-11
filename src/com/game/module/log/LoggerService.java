package com.game.module.log;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.admin.ProhibitionEntity;
import com.game.module.giftbag.ActivationCode;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerService;
import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoggerService implements InitHandler {

    public static final String INS_DIAMOND_LOG = "insert into players_diamond_logs(player_id,item_id,op_type,param,count,server_id,lev,prev,next,create_time) values(?,?,?,?,?,?,?,?,?,now())";
    public static final String ITEM_LOG = "insert into item_log(player_id,op,count,type,goods_id,goods_type,server_id,lev,prev,next,create_time) values(?,?,?,?,?,?,?,?,?,?,now())";
    public static final String CHARGE_LOG = "insert into charge_log(role_id,role_name,charge_id,charge_type,amount,channel_id,payment_type,server_id,create_time) values(?,?,?,?,?,?,?,?,now())";//充值日志
    public static final String SERVER_CHANGE = "UPDATE server set open=? where server_id=?";//改变服务器状态
    public static final String NEW_USER = "insert into new_user(new_user_count,income,arpu,server_id,create_time,update_time) values(?,?,?,?,DATE_SUB(curdate(),INTERVAL 1 DAY),DATE_SUB(curdate(),INTERVAL 1 DAY))";//改变服务器状态
    //邮件日志
    public static final String MAIL_LOG = "insert into mail_log_new(sender_id,sender_name,receive_id,title,content,state,rewards,has_reward,type,server_id,send_time) values(?,?,?,?,?,?,?,?,?,?,now())";

    private static SimpleJdbcTemplate loggerTemplate;//日志库
    private SimpleJdbcTemplate mainTemplate;//主库

    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerDao playerDao;

    public SimpleJdbcTemplate getDb() {
        return mainTemplate;
    }

    public SimpleJdbcTemplate getGmDb() {
        return loggerTemplate;
    }

    @Resource(name = "logerDataSource")
    public void setLogDataSource(DataSource dataSource) {
        this.loggerTemplate = new SimpleJdbcTemplate(dataSource);
    }

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        this.mainTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private static final ScheduledExecutorService dbLogScheduExec = Executors.newScheduledThreadPool(SysConfig.loggerThread, new MyTheadFactory("LogerDb"));

    private static ConcurrentLinkedQueue<SQLWrapper> dbLoggers = new ConcurrentLinkedQueue<SQLWrapper>();

    private static final int count = 100000;

    private static final int TYPE_CONSUME = 2;//元宝,铜钱,经验,物品
    private static final int TYPE_DATA = 4;//相关数据


    public static final int ADD = 1;
    public static final int DEC = 2;

    private static final boolean FILE_LOG = false;
    private static AtomicInteger COUNT = new AtomicInteger();
    private static final int MAX_FILE_LOG = 200000;
    private static Map<Integer, String> types = new HashMap<Integer, String>();

    static {
        types.put(TYPE_CONSUME, "consume");
        types.put(TYPE_DATA, "data");
    }

    private static Map<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();

    private static String FILE_NAME = "";

    private String time;

    private ConcurrentLinkedQueue<FileLogger> loggers = new ConcurrentLinkedQueue<FileLogger>();
    private static final ScheduledExecutorService scheduExec = Executors.newSingleThreadScheduledExecutor(new MyTheadFactory("FileLogger"));

    public void log(int type, Object... params) {
        if (COUNT.get() >= MAX_FILE_LOG) {
            return;
        }
        COUNT.incrementAndGet();
        loggers.add(new FileLogger(type, params));
    }

    @Override
    public void handleInit() {
        //文件日志
        FILE_NAME = SysConfig.statlogpath + File.separator + SysConfig.platform + File.separator + "%s" + File.separator + SysConfig.platform + "_s%d_%s_%s_daily.txt";
        time = getDateStr();
        initFileWriter();
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    //handleLogFile(false);
                } catch (Exception e) {
                    ServerLogger.err(e, "handle log file err!");
                }
            }
        }, 20, 20, TimeUnit.SECONDS);

        //数据库日志
        dbLogScheduExec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    handleLogDb(false);
                } catch (Exception e) {
                    ServerLogger.err(e, "log db err");
                }
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public void logDiamond(int playerId, int count, int logId, boolean add, int lev, int prev, int next, Object... params) {
        addDbLogger(INS_DIAMOND_LOG, playerId, logId, add ? ADD : DEC, StringUtils.join(params, ","), count, SysConfig.serverId, lev, prev, next);
    }

    //根据不同的表归类批量插入
    private void handleLogDb(boolean isShutDown) {
        Map<String, List<Object[]>> logs = new HashMap<String, List<Object[]>>();
        for (int i = 0; i < SysConfig.loggerBatchCount; i++) {
            SQLWrapper log = dbLoggers.poll();
            if (log == null) {
                break;
            }
            List<Object[]> params = logs.get(log.getSql());
            if (params == null) {
                params = new LinkedList<Object[]>();
                logs.put(log.getSql(), params);
            }
            params.add(log.getParams());
        }
        for (Entry<String, List<Object[]>> entry : logs.entrySet()) {
            loggerTemplate.batchUpdate(entry.getKey(), entry.getValue());
        }

        //关闭后，递归调用
        if (isShutDown) {
            if (loggers.isEmpty()) {
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
    public void handleLogFile(boolean dispose) throws Exception {
        if (!FILE_LOG) {
            return;
        }
        String now = getDateStr();
        if (!now.equals(time)) {//第二天了
            time = now;
            flushWriter(true);
            initFileWriter();
        }
        for (int i = 0; i < count; i++) {
            FileLogger log = loggers.poll();
            if (log == null) {
                break;
            }
            COUNT.decrementAndGet();
            BufferedWriter writer = writers.get(log.type);
            writer.append(log.params).append("\n");
        }
        flushWriter(false);
        if (dispose) {//关闭
            if (loggers.isEmpty()) {
                flushWriter(true);
                return;
            }
            handleLogFile(true);
        }
    }

    //初始化FileWriter
    private void initFileWriter() {
        if (!FILE_LOG) {
            return;
        }
        try {
            String date = time;
            for (Entry<Integer, String> entry : types.entrySet()) {
                String fileName = String.format(FILE_NAME, date, (SysConfig.serverId), date, entry.getValue());
                File file = new File(fileName);
                if (!file.exists()) {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    file.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
                writers.put(entry.getKey(), writer);
            }
        } catch (Exception e) {
            ServerLogger.err(e, "init file writer err!");
        }
    }

    //刷新流，可选择是否关闭
    private void flushWriter(boolean close) throws Exception {
        for (BufferedWriter writer : writers.values()) {
            writer.flush();
            if (close) {
                writer.close();
            }
        }
    }

    //获得当前的日期
    private String getDateStr() {
        Date time = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(time);
    }

    public void dispose() {
        scheduExec.shutdown();
        try {
            handleLogFile(true);
        } catch (Exception e) {
            ServerLogger.err(e, "file logger dispose err!");
        }

        dbLogScheduExec.shutdown();
        ServerLogger.info("remain: " + loggers.size() + " log");
        handleLogDb(true);
        try {
            scheduExec.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //充值日志
    public void logConsume(int playerId, int lev, int vipLev, boolean add, int count, LogConsume log, int goodsId, int goodsType, int prev, int next, Object... params) {
        if (dbLoggers.size() < count) {
            addDbLogger(ITEM_LOG, playerId, add ? ADD : DEC, count, log.actionId, goodsId, goodsType, SysConfig.serverId, lev, prev, next);
        }
    }

    //插入数据库日志
    public void addDbLogger(String sql, Object... params) {
        dbLoggers.add(new SQLWrapper(sql, params));
    }

    //充值日志
    public void logCharge(int roleId, String roleName, int chargeId, String chargerType, float amount, int channelId, String paymentType) {
        addDbLogger(CHARGE_LOG, roleId, roleName, chargeId, chargerType, amount, channelId, paymentType, SysConfig.serverId);
    }

    //更改服务器状态
    public void serverChange(int open) {
        addDbLogger(SERVER_CHANGE, open, SysConfig.serverId);
    }

    //获取封禁信息
    public List<ProhibitionEntity> allProhibition() {
        StringBuffer sql = new StringBuffer("select * from prohibition where 1=1 ");
        sql.append(" and server_id=")
                .append(SysConfig.serverId)
                .append(" and closure_type=1");
        return getGmDb().query(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(ProhibitionEntity.class));
    }

    //更改服务器状态
    public void updateNewUser() {
        StringBuffer sql = new StringBuffer("SELECT sum(amount) FROM charge_log where payment_type!='test' and create_time>DATE_SUB(curdate(),INTERVAL 1 DAY) and create_time<DATE_SUB(curdate(),INTERVAL 0 DAY) and server_id=");
        sql.append(SysConfig.serverId);
        int income = getGmDb().queryForInt(sql.toString());
        int newUserCount = playerDao.queryNewPlayer();
        double arpu = 0;
        if (newUserCount != 0) {
            arpu = income / newUserCount;
        }
        addDbLogger(NEW_USER, newUserCount, income, arpu, SysConfig.serverId);
    }

    //邮件日志
    public void logMail(int senderId, String senderName, int receiveId, String title, String content, int state, String rewards, int hasReward, int type) {
        addDbLogger(MAIL_LOG, senderId, senderName, receiveId, title, content, state, rewards, hasReward, type, SysConfig.serverId);
    }

    //激活码验证
    public ActivationCode activationCodeVerification(String name, Player player) {
        StringBuffer sql = new StringBuffer("SELECT * from activation_code where");
        sql.append(" name = '" + name + "'");
        sql.append(" and (server_id = " + SysConfig.serverId);
        sql.append(" or server_id = 0 ) and DATE_FORMAT(now(),'%Y%M%D')<=DATE_FORMAT(overdue_time,'%Y%M%D') and DATE_FORMAT(now(),'%Y%M%D')>=DATE_FORMAT(invalid_time,'%Y%M%D') and (use_player_id is null or universal=1)");
        ActivationCode activationCode = getGmDb().queryForObject(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(ActivationCode.class));

        if (activationCode != null) {
            //保存使用者
            sql = new StringBuffer("update activation_code set");
            sql.append(" use_time = now()");
            sql.append(" ,use_player_id = " + player.getPlayerId());
            sql.append(" ,use_player_name = '" + player.getName() + "'");
            sql.append(" ,use_player_account = '" + player.getAccName() + "'");
            sql.append(" ,use_server_id = " + SysConfig.serverId);
            sql.append(" where id = " + activationCode.getId());
            getGmDb().update(sql.toString());
            return activationCode;
        } else {
            return null;
        }
    }
}
