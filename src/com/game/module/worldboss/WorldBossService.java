package com.game.module.worldboss;

import com.game.SysConfig;
import com.game.data.*;
import com.game.event.InitHandler;
import com.game.module.admin.MessageConsts;
import com.game.module.admin.MessageService;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.multi.MultiService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.Scene;
import com.game.module.scene.SceneService;
import com.game.params.IProtocol;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.Reward;
import com.game.params.scene.SkillHurtVO;
import com.game.params.worldboss.*;
import com.game.util.*;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import io.netty.util.internal.ConcurrentSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 世界BOSS
 * 重置类活动
 */
@Service
public class WorldBossService implements InitHandler {

    private int size;
    private static final int RECV_TYPE = 2; //购买复活
    private static final int RECV_TIMES = 10; //购买复活次数
    private boolean gmOpen = false;

    private static final int CMD_MONSTER_INFO = 4910; //同步怪物相关信息
    private static final int CMD_REWARD = 4911; //结算奖励
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TimerService timerService;
    @Autowired
    private MailService mailService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private MultiService multiService;
    @Autowired
    private WorldBossDao worldBossDao;

    private int maxId = 0;
    /**
     * 数据记录
     */
    private WorldRecord worldBossData = new WorldRecord();

    ExecutorService executors = Executors.newSingleThreadExecutor();
    /**
     * 前10名,战斗开始，显示上一次，战斗开始后显示当前
     */
    TreeMap<HurtRecord, HurtRecord> treeMap = new TreeMap<>(new Comparator<HurtRecord>() {
        @Override
        public int compare(HurtRecord o1, HurtRecord o2) {
            if (o1.getPlayerId() == o2.getPlayerId()) {
                return 0;
            }
            if (o1.getHurt() == o2.getHurt()) {
                return o1.getPlayerId() - o2.getPlayerId();
            }
            return o2.getHurt() - o1.getHurt();
        }
    });

    private ReentrantLock lock = new ReentrantLock();
    private ReentrantLock wbLock = new ReentrantLock();
    private ReentrantLock awardLock = new ReentrantLock();

    //所有参加世界BOSS玩家
    private Set<Integer> players = new ConcurrentSet<>();

    @Override
    public void handleInit() {
        size = 10; //TODO 加载配置
        Integer curMaxId = worldBossDao.selectMaxId();
        if (curMaxId == null) {
            curMaxId = 1;
        } else {
            byte[] data = worldBossDao.selectWorldBossRecords(curMaxId);
            if (data != null) {
                worldBossData = JsonUtils.string2Object(
                        new String(CompressUtil.decompressBytes(data), Charset.forName("utf-8")), WorldRecord.class);
                List<HurtRecord> list = new ArrayList<>(worldBossData.getHurtMap().values());
                Collections.sort(list, SORT);
                int n = list.size() > size ? size : list.size();
                for (int i = 0; i < n; i++) {
                    treeMap.put(list.get(i), list.get(i));
                }
            }
        }
        if (worldBossData == null) {
            worldBossData = new WorldRecord();
        }

        try {
            wbLock.lock();
            maxId = (curMaxId / 1000);
        } finally {
            wbLock.unlock();
        }

        checkActivity();
        Calendar c = Calendar.getInstance();
        int second = c.get(Calendar.SECOND);
        //每分钟检测活动状态
        timerService.scheduleAtFixedRate(new Runnable() { //每分钟检测一次
            @Override
            public void run() {
                try {
                    checkActivity();
                } catch (Exception e) {
                    ServerLogger.err(e, "世界boss定时器异常");
                }
            }
        }, 60 - second, 60, TimeUnit.SECONDS);

        //每N分钟检测保存一次
        timerService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (worldBossData.getbUpdate().get()) {
                        saveData();
                        worldBossData.setbUpdate(false);
                    }
                } catch (Exception e) {
                    ServerLogger.err(e, "保存数据异常");
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private int getNextId() {
        try {
            wbLock.lock();
            maxId++;
            return maxId * 1000 + SysConfig.serverId;
        } finally {
            wbLock.unlock();
        }
    }

    /**
     * 检测活动是否开启
     *
     * @return
     */
    public boolean checkOpen() {
        int[] timeArr = ConfigData.globalParam().worldBossOpenTime;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        boolean ret = false;
        int len = timeArr.length;
        for (int i = 0; i < len; i += 2) {
            int openHour = timeArr[i];
            int endHour = timeArr[i + 1];
            if (openHour <= hour && endHour > hour) {
                //活动开启中，查看所有boss是否死亡
                ret = !worldBossData.checkAllDead();
            }
        }
        return ret;
    }

    private List<HurtRecord> hurtRank() {
        try {
            wbLock.lock();
            //排序
            List<HurtRecord> list = new ArrayList<>(worldBossData.getHurtMap().values());
            Collections.sort(list, SORT);

            List<HurtRecord> top10List = new ArrayList<>();
            Map<Integer, HurtRecord> rankMap = new HashMap<>();

            for (int i = 0; i < list.size(); i++) {
                HurtRecord hr = list.get(i);
                int rank = i + 1;
                hr.setRank(rank); //设置当前排名

                //缓存所有人排名
                HurtRecord selfRank = new HurtRecord();
                selfRank.setCurHurt(hr.getCurHurt());
                selfRank.setHurt(hr.getHurt());
                selfRank.setRank(rank);
                rankMap.put(hr.getPlayerId(), selfRank);

                if (rank <= 10) { //缓存前10名
                    HurtRecord top10Hr = new HurtRecord(hr.getPlayerId(), hr.getName());
                    top10Hr.setCurHurt(hr.getCurHurt());
                    top10Hr.setHurt(hr.getHurt());
                    top10Hr.setRank(hr.getRank());

                    top10List.add(top10Hr);
                }
            }
            worldBossData.setTop10(top10List);
            worldBossData.setRankMap(rankMap);
            return list;
        } finally {
            wbLock.unlock();
        }
    }

    /**
     * 伤害统计
     * 需要同步处理
     *
     * @param playerId
     * @param hurt
     */
    private synchronized void onBossHurt(int playerId, int hurt, int bossId,boolean isCrit) {
        WorldBoss boss = worldBossData.getWorldBossMap().get(bossId);
        if (boss == null || boss.getCurHp() <= 0) {
            ServerLogger.warn("world boss can not found bossId", bossId);
            return;
        }

        Player player = playerService.getPlayer(playerId);
        HurtRecord hr = worldBossData.getHurtMap().get(playerId);
        if (hr == null) {
            hr = new HurtRecord(playerId, player.getName());
            worldBossData.getHurtMap().put(playerId, hr);
        }

        int realHurt = hurt > boss.getCurHp() ? boss.getCurHp() : hurt;
        try {
            lock.lock();
            treeMap.remove(hr);
            hr.setHurt(hr.getHurt() + realHurt);
            hr.setName(player.getName());  //在这里同步下昵称，因为有改名功能
            //更新排名
            treeMap.put(hr, hr);
            if (treeMap.size() > size) {
                treeMap.pollLastEntry();
            }
            worldBossData.setbUpdate(true);
        } finally {
            lock.unlock();
        }

        boss.setCurHp(boss.getCurHp() - hurt);
        cleanupHurt(hr, bossId);
        hr.setCurHurt(hr.getCurHurt() + realHurt);

        MonsterHurtVO ret = new MonsterHurtVO();
        ret.actorId = bossId;
        ret.curHp = boss.getCurHp() < 0 ? 0 : boss.getCurHp();
        ret.hurt = hurt;
        ret.type = 1;
        ret.isCrit = isCrit;
        broadcast(CMD_MONSTER_INFO, ret);


        if (boss.getCurHp() <= 0) { //死亡
            worldBossData.getKillMap().put(bossId, playerId);
            //boss死亡，广播
            Int2Param param = new Int2Param();
            param.param1 = playerId;
            param.param2 = bossId;
            broadcast(WorldBossExtension.BOSS_DEAD, param);

            MonsterRefreshConfig conf = ConfigData.getConfig(MonsterRefreshConfig.class, bossId);
            MonsterConfig conf1 = ConfigData.getConfig(MonsterConfig.class, conf.monsterId);
            messageService.sendSysMsg(MessageConsts.MSG_WORLD_BOSS_DIE,player.getName(), conf1.name);
            //排序
            Context.getThreadService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        hurtRank();
                    } catch (Exception e) {
                        ServerLogger.err(e, "handle hurt rank err!");
                    }
                }
            });

            if (worldBossData.checkAllDead()) {
                sendAward();
            }

            //最后一个boss击杀
            if (bossId == worldBossData.getLastBossId()) {
                worldBossData.setLastKillPlayerId(playerId);
                worldBossData.setLastKillTime(System.currentTimeMillis());
            }
            return;
        }
    }

    private void checkActivity() {
        int[] timeArr = ConfigData.globalParam().worldBossOpenTime;
        int l = timeArr.length;
        if (l % 2 != 0) { //保证2个一组
            ServerLogger.warn("world boss config error,please check~~");
            return;
        }
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        for (int i = 0; i < l; i += 2) {
            int openHour = timeArr[i];
            int endHour = timeArr[i + 1];
            if (openHour >= endHour) {
                ServerLogger.warn("world boss config error,please check config file~~");
                continue;
            }

            //当前时间大于结束时间，则当前活动已结束
            if (hour > endHour) {
                continue;
            }
            checkWorldBoss(openHour, endHour);
        }
    }

    /**
     * 检查活动是否开始，结束
     * 以及发放奖励
     */
    private void checkWorldBoss(int openHour, int endHour) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        if (openHour - hour == 1) { //先不考虑0点
            if (60 - min == 10) {//10分钟公告
                messageService.sendSysMsg(MessageConsts.MSG_WORLD_BOSS_PRE,10);
            }
            if (60 - min == 5) { //5分钟公告
                messageService.sendSysMsg(MessageConsts.MSG_WORLD_BOSS_PRE, 5);
            }
        } else if (hour >= openHour && hour < endHour) { //活动中
            if (!worldBossData.isbStart()) {
                worldBossData.setbStart(true);
                messageService.sendSysMsg(MessageConsts.MSG_WORLD_BOSS_START);
            }

            if (worldBossData.getDay() == c.get(Calendar.DAY_OF_YEAR) &&
                    worldBossData.getOpenHour() == openHour &&
                    worldBossData.getEndHour() == endHour) { //已经有该活动数据了
                return;
            } else { //新开一活动
                worldBossData = new WorldRecord();
                worldBossData.setId(getNextId());
                worldBossData.setOpenHour(openHour);
                worldBossData.setEndHour(endHour);
                worldBossData.setDay(c.get(Calendar.DAY_OF_YEAR));
                worldBossData.setStartTime(System.currentTimeMillis());

                ServerLogger.info("open new world boss activity...");

                int[] copyArr = ConfigData.globalParam().worldBossCopy;
                int len = copyArr.length;
                int index = 1;
                for (int i = 0; i < len; i += 2) {
                    int copyId = copyArr[i];
                    int bossId = copyArr[i + 1];
                    MonsterRefreshConfig refreshConfig = ConfigData.getConfig(MonsterRefreshConfig.class, bossId);
                    MonsterConfig monsterConfig = ConfigData.getConfig(MonsterConfig.class, refreshConfig.monsterId);
                    WorldBoss boss = WorldBoss.newInstance(bossId, index++, monsterConfig.hp, copyId);
                    worldBossData.getWorldBossMap().put(bossId, boss);

                    if (i == len - 2) { //记录最后一个boss
                        worldBossData.setLastBossId(bossId);
                    }
                }
                try {
                    lock.lock(); //清空排行
                    treeMap.clear();
                } finally {
                    lock.unlock();
                }
                saveData();
            }
        } else if (hour >= endHour) { //结束,发奖励
            if (hour != worldBossData.getEndHour()) {
                return;
            }
            sendAward();
        }
    }

    /**
     * 发奖励，活动结束或者所有boss打死
     */
    private void sendAward() {
        try {
            awardLock.lock();
            if (worldBossData.isbAward()) {
                return;
            }
            //活动结束，清理相关数据
            players.clear();
            worldBossData.setbAward(true);
            messageService.sendSysMsg(MessageConsts.MSG_WORLD_BOSS_END);
            multiService.clearGroup(Scene.WORLD_BOSS_PVE);
            //排序
            //List<HurtRecord> list = new ArrayList<>(worldBossData.getHurtMap().values());
            //Collections.sort(list, SORT);

            List<GoodsEntry> killRewards = new ArrayList<>(); //击杀BOSS奖励
            //最后一击奖励
            Map<Integer, int[]> lastBeatReward = ConfigData.globalParam().worldBossLastFightReward;
            Map<Integer, int[]> killReward = ConfigData.globalParam().worldBossKillReward;

            Map<Integer, List<GoodsEntry>> beatRewardMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> s : worldBossData.getKillMap().entrySet()) {
                List<GoodsEntry> lastBeatRewards = beatRewardMap.get(s.getValue()); //最后一击奖励列表
                if (lastBeatRewards == null) {
                    lastBeatRewards = new ArrayList<>();
                    beatRewardMap.put(s.getValue(), lastBeatRewards);
                }

                int[] nArr = lastBeatReward.get(s.getKey()); //获取最后一击奖励
                for (int k = 0; k < nArr.length; k += 2) {
                    GoodsEntry goodsEntry = new GoodsEntry(nArr[k], nArr[k + 1]);
                    lastBeatRewards.add(goodsEntry);
                }

                nArr = killReward.get(s.getKey()); //击杀奖励
                for (int k = 0; k < nArr.length; k += 2) {
                    GoodsEntry goodsEntry = new GoodsEntry(nArr[k], nArr[k + 1]);
                    killRewards.add(goodsEntry);
                }
            }
            String lastTitle = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_LAST_BEAT_TITLE).tips;
            String lastContent = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_LAST_BEAT_CONTENT).tips;
            //最后一击奖励邮件
            for (Map.Entry<Integer, List<GoodsEntry>> s3 : beatRewardMap.entrySet()) {
                mailService.sendSysMail(lastTitle, lastContent, s3.getValue(), s3.getKey(), LogConsume.WORLD_BOSS_LAST_BEAT);
            }
            List<GoodsEntry> rewards = new ArrayList<>();
            int[][] rankReward = ConfigData.globalParam().worldBossReward;
            int[][] rankRewardRank = ConfigData.globalParam().worldBossRewardRank;
            int rankLen = rankRewardRank.length;

            WorldBossReward rewardCli = new WorldBossReward();
            rewardCli.hurtReward = new ArrayList<>();
            rewardCli.rankReward = new ArrayList<>();

            List<HurtRecord> list = hurtRank();
            //排名奖励
            for (int i = 0; i < list.size(); i++) {
                HurtRecord hr = list.get(i);
                int rank = i + 1;
                hr.setRank(rank); //设置当前排名
                rewards.clear();
                for (int k = 0; k < rankLen; k++) {
                    int[] rankArr = rankRewardRank[k];
                    int minRank = rankArr[0];
                    int maxRank = rankArr[1];
                    if (rank >= minRank && rank <= maxRank) {
                        int[] nArr = rankReward[k];
                        for (int n = 0; n < nArr.length; n += 2) {
                            GoodsEntry goodsEntry = new GoodsEntry(nArr[n], nArr[n + 1]);
                            rewards.add(goodsEntry);

                            Reward reward = new Reward();
                            reward.count = nArr[n + 1];
                            reward.id = nArr[n];
                            rewardCli.rankReward.add(reward);
                        }
                        break;
                    }
                }
                //伤害兑换金币
                int itemId = ConfigData.globalParam().worldBossHurtReward[0];
                int num = ConfigData.globalParam().worldBossHurtReward[1];
                rewards.add(new GoodsEntry(itemId, num));

                Reward reward = new Reward();
                reward.count = itemId;
                reward.id = num;
                rewardCli.hurtReward.add(reward);

                String killTitle = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_KILL_TITLE).tips;
                String killContent = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_KILL_CONTENT).tips;

                String rankTitle = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_RANK_TITLE).tips;
                String rankContent = ConfigData.getConfig(ErrCode.class, Response.WORLD_BOSS_RANK_CONTENT).tips;

                //发送排名奖励邮件
                mailService.sendSysMail(rankTitle, rankContent, rewards, hr.getPlayerId(), LogConsume.WORLD_BOSS_REWARD);
                if (!killRewards.isEmpty()) {
                    //发送击杀奖励邮件
                    mailService.sendSysMail(killTitle, killContent, killRewards, hr.getPlayerId(), LogConsume.WORLD_BOSS_KILL);
                }

                //推送结算奖励
                //SessionManager.getInstance().sendMsg(CMD_REWARD, rewardCli, hr.getPlayerId());
            }
            saveData();
        } finally {
            awardLock.unlock();
        }
    }

    private void saveData() {
        String str = JsonUtils.object2String(worldBossData);
        byte[] dbData = str.getBytes(Charset.forName("utf-8"));
        worldBossDao.updateWorldRecord(worldBossData.getId(), CompressUtil.compressBytes(dbData));
    }

    public void shutdown() {
        try {
            saveData();
            ServerLogger.info("save world boss data success...");
        } catch (Exception e) {
            ServerLogger.err(e, "save world boss data fail");
        }
    }


    /**
     * 伤害排序
     */
    private static Comparator<HurtRecord> SORT = new Comparator<HurtRecord>() {
        @Override
        public int compare(HurtRecord o1, HurtRecord o2) {
            //return o2.getHurt() - o1.getHurt();
            if (o1.getPlayerId() == o2.getPlayerId()) {
                return 0;
            }
            if (o1.getHurt() == o2.getHurt()) {
                return o1.getPlayerId() - o2.getPlayerId();
            }
            return o2.getHurt() - o1.getHurt();
        }
    };

    public void addPlayer(Integer playerId) {
        players.add(playerId);
        multiService.onEnter(playerId);
    }

    public void removePlayer(Integer playerId) {
        players.remove(playerId);
        multiService.onExit(playerId);
    }

    public void broadcast(final int cmd, final IProtocol param) {
        executors.submit(new Runnable() {
            @Override
            public void run() {
                for (int playerId : players) {
                    SessionManager.getInstance().sendMsg(cmd, param, playerId);
                }
            }
        });
    }

    /**
     * 不是同一个BOSS，重置伤害
     *
     * @param hr
     * @param bossId
     */
    private void cleanupHurt(HurtRecord hr, int bossId) {
        if (hr != null && hr.getCurBossId() != bossId) {
            hr.setCurHurt(0);
            hr.setCurBossId(bossId);
        }
    }

    /**
     * 请求挑战BOSS
     *
     * @param playerId
     * @param index
     * @return
     */
    public Object startChallenge(int playerId, int index) {
        WorldBossChallentVO vo = new WorldBossChallentVO();
        if (!gmOpen && !checkOpen()) {
            vo.code = Response.WORLD_BOSS_END;
            ServerLogger.warn("time out,world boss info ==>" + "open time = " + worldBossData.getOpenHour() + ",end time = " + worldBossData.getEndHour());
            return vo;
        }

        WorldBoss worldBoss = null;
        if (index != 1) {
            for (WorldBoss boss : worldBossData.getWorldBossMap().values()) {
                //检查前置BOSS有没有被杀
                if (boss.getIndex() == index - 1) {
                    if (boss.getCurHp() > 0) {
                        vo.code = Response.ERR_PARAM;
                        ServerLogger.warn("pre-boss has been alive.  index = " + index);
                        return vo;
                    }
                    break;
                }
            }
        }

        for (WorldBoss boss : worldBossData.getWorldBossMap().values()) {
            if (boss.getIndex() == index) {
                worldBoss = boss;
                break;
            }
        }
        if (worldBoss == null) {
            vo.code = Response.WORLD_BOSS_END;
            ServerLogger.warn("world boss is null.... index = " + index);
            ServerLogger.warn("world boss is null..,world boss info ==>" + JsonUtils.object2String(worldBossData));
            return vo;
        }
        if (worldBoss.getCurHp() <= 0) {
            vo.code = Response.WORLD_BOSS_KILLED;
            return vo;
        }

        Integer times = worldBossData.getBuyTimes().get(playerId);
        if (times == null) {
            times = 0;
        }

        HurtRecord hr = worldBossData.getHurtMap().get(playerId);
        cleanupHurt(hr, worldBoss.getId());

        //addPlayer(playerId);
        vo.attackBuyCount = times;
        vo.copyId = worldBoss.getCopyId();
        Long deadTime = worldBossData.getPlayerDeadTime().get(playerId);
        if (deadTime == null) {
            deadTime = 0L;
        }
        vo.deadTime = deadTime.intValue();

        BossVo bossVo = new BossVo();
        bossVo.curHp = worldBoss.getCurHp();
        bossVo.hp = worldBoss.getHp();
        bossVo.monsterId = worldBoss.getId();

        vo.boss = bossVo;
        return vo;
    }

    /**
     * 获取世界boss相关数据
     *
     * @return
     */
    public Object getWorldBossInfo(int playerId) {
        WorldBossVO vo = new WorldBossVO();
        vo.startTime = worldBossData.getOpenHour();
        vo.killCount = worldBossData.getKillMap().size();
        if (worldBossData.getLastKillTime() > 0) {
            vo.bossKilledTime = (int) ((worldBossData.getLastKillTime() - worldBossData.getStartTime()) / 1000);
        }
        if (worldBossData.getLastKillPlayerId() != 0) {
            Player p = playerService.getPlayer(worldBossData.getLastKillPlayerId());
            if (p != null) {
                vo.playerName = p.getName();
            }
        }
        return vo;
    }

    /**
     * 购买攻击力
     *
     * @param playerId
     * @return
     */
    public Int2Param buyAttack(int playerId, int count) {
        Int2Param param = new Int2Param();
        param.param1 = Response.SUCCESS;
        Integer times = worldBossData.getBuyTimes().get(playerId);
        if (times == null) {
            times = 0;
        }
        if (times >= RECV_TIMES) { //判断次数
            param.param1 = Response.WORLD_BOSS_NOT_BUY_TIMES;
            return param;
        }
        // 扣钱
        int[] costArr = ConfigData.globalParam().worldBossBuyAttackCost;
        int code = goodsService.decConsume(playerId, Arrays.asList(new GoodsEntry(costArr[0], costArr[1] * count)),
                LogConsume.WORLD_BOSS_BUY);
        if (code != Response.SUCCESS) {
            param.param1 = code;
            return param;
        }

        times += count;
        param.param2 = times;
        worldBossData.getBuyTimes().put(playerId, times);
        return param;
    }

    /**
     * 获取实时伤害排名
     */
    public Object getRealTimeHurtList(int playerId) {
        List<HurtRecord> listTmp = new ArrayList<>(treeMap.values());
        WorldBossHurtRankVO vo = toRankProto(listTmp);
        listTmp.clear();
        listTmp = null;
        HurtRecord hr = worldBossData.getHurtMap().get(playerId);
        if (hr != null) {
            vo.selfRank = hr.getRank();
            vo.hurt = hr.getCurHurt();
        }
        return vo;
    }

    private WorldBossHurtRankVO toRankProto(List<HurtRecord> list) {
        WorldBossHurtRankVO cli = new WorldBossHurtRankVO();
        cli.rankList = new ArrayList<>();
        for (HurtRecord hr : list) {
            WorldBossHurtVO vo = new WorldBossHurtVO();
            vo.hurt = hr.getHurt();
            vo.nickName = hr.getName();
            cli.rankList.add(vo);
        }
        return cli;
    }

    /**
     * 获取历史伤害排名
     */
    public Object getHistoryHurtList(int playerId) {
        List<HurtRecord> list = worldBossData.getTop10();
        WorldBossHurtRankVO vo = toRankProto(list);
        Map<Integer, HurtRecord> rankMap = worldBossData.getRankMap();
        HurtRecord hr = rankMap.get(playerId);
        if (hr != null) {
            vo.selfRank = hr.getRank();
            vo.hurt = hr.getHurt();
        }
        return vo;
    }

    /**
     * 英雄死亡事件
     */
    public void onHeroDead(int playerId, int deadId) {
        IntParam param = new IntParam();
        if (playerId != deadId) {
            return;
        }
        param.param = playerId;
        Player player = playerService.getPlayer(playerId);
        ServerLogger.debug("=========" + deadId);
        sceneService.brocastToSceneCurLine(player, WorldBossExtension.PLAYER_DEAD, param);
    }

    /**
     * 处理伤害
     *
     * @param player
     * @param hurtVO
     */
    public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
        //broadcast(SceneExtension.SKILL_HURT, hurtVO);
        if (hurtVO.targetType == 1) {
            onBossHurt(player.getPlayerId(), hurtVO.hurtValue, hurtVO.targetId,hurtVO.isCrit);
        }
    }

    /**
     * 复活购买
     *
     * @param playerId
     * @param bossId
     */
    public void revive(int playerId, int type, int bossId) {
        WorldBoss worldBoss = worldBossData.getWorldBossMap().get(bossId);
        Int2Param param = new Int2Param();
        if (worldBoss.getCurHp() <= 0) {
            param.param1 = Response.WORLD_BOSS_KILLED;
            SessionManager.getInstance().sendMsg(CopyExtension.CMD_REVIVI, param, playerId);
            return;
        }
        if (type == RECV_TYPE) {
            CopyConfig copyCfg = ConfigData.getConfig(CopyConfig.class, worldBoss.getCopyId());
            // 复活价格
            List<GoodsEntry> cost = new ArrayList<>(copyCfg.reviveCost.length);
            for (int[] item : copyCfg.reviveCost) {
                cost.add(new GoodsEntry(item[0], item[1]));
            }
            int code = goodsService.decConsume(playerId, cost, LogConsume.REVIVE, worldBoss.getCopyId());
            if (code != Response.SUCCESS) {
                param.param1 = code;
                SessionManager.getInstance().sendMsg(4907, param, playerId);
                return;
            }
        } else { //自动复活时间判断
            //TODO 加验证
        }
        param.param1 = Response.SUCCESS;
        param.param2 = playerId;

        //广播
        Player player = playerService.getPlayer(playerId);
        String key = sceneService.getGroupKey(player);
        SessionManager.getInstance().sendMsgToGroup(key, 4907, param);
        return;
    }

    public void gmReset() {
        if (gmOpen && !worldBossData.checkAllDead()) {
            //已经开过了
            return;
        }
        gmOpen = true;
        worldBossData.getKillMap().clear();
        worldBossData.getTop10().clear();
        worldBossData.getRankMap().clear();
        worldBossData.getHurtMap().clear();

        for (WorldBoss worldBoss : worldBossData.getWorldBossMap().values()) {
            worldBoss.setCurHp(worldBoss.getHp());
        }
        worldBossData.setLastKillTime(0);
        worldBossData.setLastKillPlayerId(0);
        treeMap.clear();
        ServerLogger.warn("gm reset world boss activity .........");
    }
}
