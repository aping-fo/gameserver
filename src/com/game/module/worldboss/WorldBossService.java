package com.game.module.worldboss;

import com.game.SysConfig;
import com.game.event.InitHandler;
import com.game.module.admin.MessageService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsEntry;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.util.CompressUtil;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.game.util.TimerService;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 世界BOSS
 */
@Service
public class WorldBossService implements InitHandler {

    private int size;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TimerService timerService;
    @Autowired
    private MailService mailService;
    @Autowired
    private WorldBossDao worldBossDao;

    private int maxId = 0;

    private boolean tenMinFlag;
    private boolean fiveMinFlag;
    private boolean beginFlag;

    private WorldRecord worldRecord = new WorldRecord();
    private WorldBoss worldBoss; //当前
    /**
     * 前10名,战斗开始，显示上一次，战斗开始后显示当前
     */
    private List<HurtRecord> records = new CopyOnWriteArrayList<>();

    private ReentrantLock lock = new ReentrantLock();
    private ReentrantLock idLock = new ReentrantLock();

    @Override
    public void handleInit() {
        size = 10; //TODO 加载配置

        Integer curMaxId = worldBossDao.selectMaxId();
        if (curMaxId == null) {
            curMaxId = 0;
        } else {
            byte[] data = worldBossDao.selectWorldBossRecords(curMaxId);
            if (data != null) {
                worldRecord = JsonUtils.string2Object(
                        new String(data, Charset.forName("utf-8")), WorldRecord.class);
                List<HurtRecord> list = new ArrayList<>(worldRecord.getHurtMap().values());
                Collections.sort(list, SORT);
                int n = list.size() > size ? size : list.size();
                for (int i = 0; i < n; i++) {
                    records.add(list.get(i));
                }
            }
        }

        try {
            idLock.lock();
            maxId = (curMaxId / 1000);
        } finally {
            idLock.unlock();
        }

        Calendar c = Calendar.getInstance();
        int second = c.get(Calendar.SECOND);
        timerService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkWorldBoss();
            }

        }, 60 - second, 60, TimeUnit.SECONDS);
    }

    private int getNextId() {
        try {
            idLock.lock();
            maxId++;
            return maxId * 1000 + SysConfig.serverId;
        } finally {
            idLock.unlock();
        }
    }

    /**
     * 检测活动是否开启
     *
     * @return
     */
    public boolean checkOpen() {

        return false;
    }

    /**
     * 获取世界BOSS信息
     *
     * @return
     */
    public WorldBoss getWorldBoss() {

        return null;
    }

    public void addHurt(int playerId, int hurt) {
        Player player = playerService.getPlayer(playerId);
        HurtRecord hr = worldRecord.getHurtMap().get(playerId);
        if (hr == null) {
            hr = new HurtRecord(playerId, player.getName());
        }

        hr.setHurt(hr.getHurt() + hurt);

        int leftHp = worldBoss.getHp() - hurt;
        if (leftHp < 0) {
            leftHp = 0;
        }
        worldBoss.setHp(leftHp);
        if (leftHp == 0) {
            //KILL
            worldRecord.getKillMap().put(worldBoss.getId(), playerId);
            //TODO 刷新下一个
        }
        try {
            lock.lock();
            //更新排名
            int index = 0;
            for (int i = 0; i < size; i++) {
                HurtRecord record = records.get(i);
                if (hr.getHurt() > record.getHurt()) {
                    index = i;
                    break;
                }
            }

            if (index < size) {
                records.add(index, hr);
            }

            if (records.size() > 10) {
                for (int i = 10; i < records.size(); i++) {
                    records.remove(i);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void checkWorldBoss() {
        Map<Integer, Integer> map = ConfigData.globalParam().worldBossOpenTime;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        for (Map.Entry<Integer, Integer> s : map.entrySet()) {
            int openHour = s.getKey();
            int endHour = s.getValue();

            if (openHour - hour == 1) { //先不考虑0点
                if (60 - min <= 10) {
                    if (!tenMinFlag) {
                        messageService.sendSysMsg(2, "世界BOSS活动将于10分钟后开启，参与活动可获得大量奖励！");
                        tenMinFlag = true;
                    }
                } else if (60 - min <= 5) {
                    if (!fiveMinFlag) {
                        messageService.sendSysMsg(2, "世界BOSS活动将于5分钟后开启，参与活动可获得大量奖励！");
                        fiveMinFlag = true;
                    }
                }
            } else if (hour >= openHour && hour <= endHour) {
                if (!beginFlag) {
                    messageService.sendSysMsg(2, "世界BOSS活动已经开启，玩家请前往击杀");
                    beginFlag = true;
                }

                int day = c.get(Calendar.DAY_OF_YEAR);
                if (worldRecord.getDay() == day) { //在活动范围内
                    return;
                } else { //新开一活动
                    worldRecord = new WorldRecord();
                    worldRecord.setId(getNextId());
                    worldRecord.setDay(day);
                    records.clear();

                    worldBoss = new WorldBoss();
                }
            } else if (hour > endHour) { //结束,发奖励
                if (worldRecord.getRewardFlag() != 1) {
                    //排序
                    List<HurtRecord> list = new ArrayList<>(worldRecord.getHurtMap().values());
                    Collections.sort(list, SORT);

                    List<GoodsEntry> killRewards = new ArrayList<>(); //击杀BOSS奖励
                    //最后一击奖励
                    Map<Integer, int[]> lastFightReward = ConfigData.globalParam().worldBossLastFightReward;
                    Map<Integer, int[]> killReward = ConfigData.globalParam().worldBossKillReward;
                    Map<Integer, List<GoodsEntry>> map3 = new HashMap<>();
                    for (Map.Entry<Integer, Integer> s2 : worldRecord.getKillMap().entrySet()) {
                        List<GoodsEntry> list1 = map3.get(s2.getValue());
                        if (list1 == null) {
                            list1 = new ArrayList<>();
                            map3.put(s2.getValue(), list1);
                        }

                        int[] nArr = lastFightReward.get(s2.getKey()); //获取最后一击奖励
                        for (int k = 0; k < nArr.length; k += 2) {
                            GoodsEntry goodsEntry = new GoodsEntry(nArr[k], nArr[k + 1]);
                            list1.add(goodsEntry);
                        }

                        nArr = killReward.get(s2.getKey()); //击杀奖励
                        for (int k = 0; k < nArr.length; k += 2) {
                            GoodsEntry goodsEntry = new GoodsEntry(nArr[k], nArr[k + 1]);
                            killRewards.add(goodsEntry);
                        }
                    }
                    for (Map.Entry<Integer, List<GoodsEntry>> s3 : map3.entrySet()) {
                        mailService.sendSysMail("最后一击奖励", "", s3.getValue(), s3.getKey(), LogConsume.WORLD_BOSS_REWARD);
                    }

                    List<GoodsEntry> rewards = new ArrayList<>();
                    Map<Integer, int[]> rankReward = ConfigData.globalParam().worldBossReward;
                    //排名奖励
                    for (int i = 0; i < list.size(); i++) {
                        HurtRecord hr = list.get(i);
                        rewards.clear();
                        for (Map.Entry<Integer, int[]> s1 : rankReward.entrySet()) {
                            if (i <= s.getKey()) {
                                int[] nArr = s1.getValue();
                                for (int k = 0; k < nArr.length; k += 2) {
                                    GoodsEntry goodsEntry = new GoodsEntry(nArr[k], nArr[k + 1]);
                                    rewards.add(goodsEntry);
                                }
                            }
                        }

                        //伤害兑换金币
                        rewards.add(new GoodsEntry(Goods.COIN, hr.getHurt() / ConfigData.globalParam().worldBossHurtReward));
                        mailService.sendSysMail("排名奖励", "", rewards, hr.getPlayerId(), LogConsume.WORLD_BOSS_REWARD);
                        mailService.sendSysMail("击杀奖励", "", killRewards, hr.getPlayerId(), LogConsume.WORLD_BOSS_REWARD);
                    }

                    worldRecord.setRewardFlag(1);
                    tenMinFlag = false;
                    fiveMinFlag = false;
                    beginFlag = false;
                    String str = JsonUtils.object2String(worldRecord);
                    byte[] dbData = str.getBytes(Charset.forName("utf-8"));
                    worldBossDao.updateWorldRecord(worldRecord.getId(), CompressUtil.compressBytes(dbData));
                }
            }
        }
    }

    public void shutdown() {
        try {
            byte[] data = CompressUtil.compressBytes(JsonUtils.object2String(worldRecord).getBytes("utf-8"));
            worldBossDao.updateWorldRecord(worldRecord.getId(), data);
        } catch (Exception e) {
            ServerLogger.err(e, "save world boss data fail");
        }


    }


    private static Comparator<HurtRecord> SORT = new Comparator<HurtRecord>() {
        @Override
        public int compare(HurtRecord o1, HurtRecord o2) {
            return o2.getHurt() - o1.getHurt();
        }
    };
}
