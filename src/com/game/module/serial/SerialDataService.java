package com.game.module.serial;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.module.attach.arena.ArenaPlayer;
import com.game.module.ladder.LadderService;
import com.game.module.pet.Pet;
import com.game.module.vip.VipService;
import com.game.params.rank.LevelRankVO;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import com.game.event.InitHandler;
import com.game.event.ServiceDispose;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;

@Service
public class SerialDataService implements InitHandler, ServiceDispose {

    @Autowired
    private SerialDataDao dao;
    @Autowired
    private LadderService ladderService;
    private SerialData data;
    private SerialData2 data2;//第二部分数据

    private AtomicInteger minGRank;

    @Autowired
    private VipService vipService;

    @Override
    public void handleInit() {
        dao.initSerialData(1);
        byte[] dbData = dao.selectSerialData(1);
        if (dbData != null) {
            dbData = CompressUtil.decompressBytes(dbData);
            data = JsonUtils.string2Object(new String(dbData, Charset.forName("utf-8")), SerialData.class);
        } else {
            data = new SerialData();
        }

        //第二部分数据
        dao.initSerialData(2);
        byte[] dbData2 = dao.selectSerialData(2);
        if (dbData2 != null) {
            dbData2 = CompressUtil.decompressBytes(dbData2);
            data2 = JsonUtils.string2Object(new String(dbData2, Charset.forName("utf-8")), SerialData2.class);
        } else {
            data2 = new SerialData2();
        }

        Set<Integer> ids = new HashSet<>();
        for (ArenaPlayer ap : data.getRanks().values()) {
            if (ids.contains(ap.getPlayerId())) {
                System.out.println("+++" + ap.getPlayerId() + "--" + ap.getUniqueId());
            }
            ids.add(ap.getPlayerId());
        }

        ids = new HashSet<>();
        for (ArenaPlayer ap : data.getPlayerRanks().values()) {
            if (ids.contains(ap.getPlayerId())) {
                System.out.println("---" + ap.getPlayerId() + "--" + ap.getUniqueId());
            }
            ids.add(ap.getPlayerId());
        }
        ladderService.ladderSort();
    }

    public SerialData getData() {
        return data;
    }

    @Override
    public void serviceDispse() {
        //第一部分
        String str = JsonUtils.object2String(data);
        byte[] dbData = str.getBytes(Charset.forName("utf-8"));
        dao.updateSerialData(CompressUtil.compressBytes(dbData), 1);
        //第二部分
        String str2 = JsonUtils.object2String(data2);
        byte[] dbData2 = str2.getBytes(Charset.forName("utf-8"));
        dao.updateSerialData(CompressUtil.compressBytes(dbData2), 2);
    }

    public SerialData2 getData2() {
        return data2;
    }

    //最小的神将排名 TODO 没初始化
    public int getMinGeneralRank() {
        return minGRank.get();
    }

    //复制全局表
    public void copySerialData(SimpleJdbcTemplate mergeDb) {
        ServerLogger.warn("全局表第一部分开始复制");
        StringBuffer sql = new StringBuffer("SELECT * FROM serial_data where id = 1");
        List<SerialData> list = mergeDb.query(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(SerialData.class));
        SerialData serialData = list.get(0);
        data.getOpponents().putAll(serialData.getOpponents());
        data.getSectionOpponents().putAll(serialData.getSectionOpponents());
        data.getRanks().putAll(serialData.getRanks());
        data.getPlayerRanks().putAll(serialData.getPlayerRanks());
        data.getFriendRequests().putAll(serialData.getFriendRequests());
        data.getFriendSendRequests().putAll(serialData.getFriendSendRequests());
        data.getCopyRanks().putAll(serialData.getCopyRanks());
        data.getPlayerRefreshShops().putAll(serialData.getPlayerRefreshShops());
        data.getLadderMap().putAll(serialData.getLadderMap());
        data.getGangMap().putAll(serialData.getGangMap());
        data.getPlayerViews().putAll(serialData.getPlayerViews());
        data.getBossKillTimes().putAll(serialData.getBossKillTimes());
        data.getCdkSet().addAll(serialData.getCdkSet());
        data.getStateRanks().addAll(serialData.getStateRanks());
        data.getFacebookBindIds().putAll(serialData.getFacebookBindIds());
        data.getFacebookInviteIds().putAll(serialData.getFacebookInviteIds());
        data.getFacebookInviteSuccessIds().putAll(serialData.getFacebookInviteSuccessIds());
        data.getCopyPassFastestTimeMap().putAll(serialData.getCopyPassFastestTimeMap());
        data.getLaterDayRewardMap().putAll(serialData.getLaterDayRewardMap());
        data.setAdventureBoxNumber(new ConcurrentHashMap<>());//清空奇遇宝箱购买次数

        //重置充值排行
        Map<Integer, LevelRankVO> levelRankingsMap = data.getLevelRankingsMap();
        levelRankingsMap.putAll(serialData.getLevelRankingsMap());
        vipService.updateRechargeRank(levelRankingsMap);

        ServerLogger.warn("全局表第一部分完成复制");

        ServerLogger.warn("全局表第二部分开始复制");
        sql = new StringBuffer("SELECT * FROM serial_data where id = 2");
        List<SerialData2> list2 = mergeDb.query(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(SerialData2.class));
        SerialData2 serialData2 = list2.get(0);
        ServerLogger.warn("全局表第二部分完成复制");
    }
}
