package com.game.module.merge;

import com.game.event.InitHandler;
import com.game.module.attach.AttachService;
import com.game.module.gang.GangService;
import com.game.module.goods.GoodsService;
import com.game.module.pet.PetService;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.module.task.TaskService;
import com.server.util.ServerLogger;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @作者 周聪
 * @时间 2018/11/7 11:18
 * 合服模块
 */
@Service
public class MergeService {
    private BasicDataSource basicDataSource;//合服数据库
    private SimpleJdbcTemplate mergeTemplate;//合服数据库

    @Resource(name = "mergeDataSource")
    private void setDataSource(BasicDataSource basicDataSource) {
        this.basicDataSource = basicDataSource;
        this.mergeTemplate = new SimpleJdbcTemplate(basicDataSource);
    }

    private SimpleJdbcTemplate getMergeDb() {
        return mergeTemplate;
    }

    @Autowired
    private AttachService attachService;
    @Autowired
    private GangService gangService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PetService petService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private TaskService taskService;

    private boolean updateNewUser(String mergeServerIdStr) {
        ServerLogger.warn("合服开始");
        SimpleJdbcTemplate mergeDb = getMergeDb();
        boolean result = true;
        try {
            playerService.copyPlayer(mergeServerIdStr, mergeDb);//复制玩家表
            playerService.copyPlayerData(mergeDb); //复制玩家数据表
            attachService.copyAttach(mergeDb); //复制副本表
            gangService.copyGang(mergeServerIdStr, mergeDb);//复制公会表
            goodsService.copyGoods(mergeDb);//复制物品表
            petService.copyPet(mergeDb);//复制宠物表
            serialDataService.copySerialData(mergeDb);//复制全局表
            playerService.copyChargeRecord(mergeDb);//复制充值记录表
            taskService.copyTask(mergeDb);//复制任务表
        } catch (Exception e) {
            ServerLogger.warn("合服失败");
            result = false;
            return result;
        }
        ServerLogger.warn("合服结束");
        return result;
    }

    //合服
    public boolean mergeServer(String ip, String serverId, String dbName) {
        basicDataSource.setUrl("jdbc:mysql://" + ip + ":3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true");
        return updateNewUser(serverId);
    }
}
