package com.game.module.rank;

import com.game.data.EndlessCfg;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.attach.endless.EndlessAttach;
import com.game.module.attach.endless.EndlessLogic;
import com.game.module.fashion.Fashion;
import com.game.module.fashion.FashionService;
import com.game.module.gang.GangService;
import com.game.module.ladder.LadderService;
import com.game.module.pet.Pet;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.rank.vo.EndlessRankEntity;
import com.game.module.rank.vo.FightingRankEntity;
import com.game.module.rank.vo.LevelRankEntity;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.task.TaskService;
import com.game.params.FashionInfo;
import com.game.params.ListParam;
import com.game.params.rank.FashionCopyRankVO;
import com.game.params.rank.LadderRankVO;
import com.game.params.rank.LevelRankVO;
import com.game.params.rank.StateRankVO;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;
import com.game.util.RandomUtil;
import com.game.util.TimeUtil;
import com.google.common.collect.Lists;
import com.server.util.ServerLogger;
import com.test.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RankService implements InitHandler {

    public static final int TYPE_FIGHTING = 1;
    public static final int TYPE_LEVEL = 2;
    public static final int TYPE_FAME = 3;
    public static final int TYPE_ENDLESS = 4;
    public static final int TYPE_AI_ARENA = 5;
    public static final int TYPE_PK_ARENA = 6;
    public static final int TYPE_ACHIEVEMENT = 7;

    @Autowired
    private RankingDao dao;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GangService gangService;
    @Autowired
    private EndlessLogic endlessLogic;

    private final Map<Integer, RankingList<? extends IRankCA>> rankings = new ConcurrentHashMap<Integer, RankingList<? extends IRankCA>>();
    private ListParam<StateRankVO> listParam = new ListParam<>();//5点排行第一

    @Override
    public void handleInit() {
        register(TYPE_FIGHTING, 50, (int) TimeUtil.ONE_HOUR, FightingRankEntity.class);
        register(TYPE_ENDLESS, 50, (int) TimeUtil.ONE_HOUR, EndlessRankEntity.class);
        register(TYPE_ACHIEVEMENT, 50, (int) TimeUtil.ONE_HOUR, EndlessRankEntity.class);
        sort();
    }

    private <T extends IRankCA> void register(int type, int maxCapacity, int updatePeriod, Class<T> entityClass) {
        if (rankings.containsKey(type)) {
            throw new RuntimeException("duplicate key:" + type);
        }
        RankingList<T> ranking = new RankingList<T>(type, maxCapacity, updatePeriod);
        rankings.put(type, ranking);
        byte[] bytes = dao.selectRanking(type);
        if (bytes != null && bytes.length > 0) {
            String data = new String(CompressUtil.decompressBytes(bytes), Charset.forName("utf-8"));
            Map<Integer, T> keys = JsonUtils.string2Map(data, Integer.class, entityClass);
            if (keys != null && !keys.isEmpty()) {
                ranking.putAll(keys);
            }
        }
    }

    public void removeInvalidEndlessRank() {
        RankingList<EndlessRankEntity> ranking = getRankingList(TYPE_ENDLESS);
        int lev = 2060;
        int passTime = 30;
        float fightFactor = 1.5f;
        List<Integer> invalidPlayerId = new ArrayList<>();
        boolean foundInvalid = false;
        EndlessCfg eCfg = endlessLogic.getConfig();

        for (int i = 0; i < ranking.getSize(); ++i) {
            RankEntity entity = ranking.getRankEntity(i);
            if (entity == null)
                continue;

            EndlessRankEntity rankEntity = (EndlessRankEntity) entity.getCa();
            if (rankEntity == null)
                continue;

            EndlessAttach attach = endlessLogic.getAttach(entity.getPlayerId());
            int fight = Math.round(eCfg.baseData + (eCfg.baseData * (attach.getMaxLayer() - 1) * eCfg.growRatio
                    + eCfg.baseData * (attach.getMaxLayer() / eCfg.sectionLayer) * eCfg.sectionMultiple * eCfg.scetionRatio));

            Player p = playerService.getPlayer(entity.getPlayerId());
            boolean isInvalid = false;
            if (p != null) {
                if (p.getFight() * fightFactor < fight) {
                    isInvalid = true;
                }
            }
            if (!isInvalid && (rankEntity.getLayer() > lev)) {
                isInvalid = true;
            }

            if (isInvalid) {
                foundInvalid = true;
                invalidPlayerId.add(entity.getPlayerId());
            }
        }

        if (!foundInvalid)
            return;

        for (int i = 0; i < invalidPlayerId.size(); ++i) {
            ranking.remove(invalidPlayerId.get(i));
        }
        ranking.saveDb();

        List<Player> allPlayer = playerService.getAllPlayer();
        for (Player player : allPlayer) {
            EndlessAttach attach = endlessLogic.getAttach(player.getPlayerId());
            if (attach == null) {
                ServerLogger.warn("无尽漩涡记录不存在=" + player.getPlayerId());
                continue;
            }
            attach.setPassTime(RandomUtil.randInt(60, 85));

            int fight = Math.round(eCfg.baseData + (eCfg.baseData * (attach.getMaxLayer() - 1) * eCfg.growRatio
                    + eCfg.baseData * (attach.getMaxLayer() / eCfg.sectionLayer) * eCfg.sectionMultiple * eCfg.scetionRatio));

            if (player.getFight() * fightFactor < fight || attach.getMaxLayer() > lev || attach.getCurrLayer() > lev) {
                attach.setMaxLayer(0);
                attach.setCurrLayer(1);
                attach.saveDb();
            }

            ranking.updateEntity(player.getPlayerId(), new EndlessRankEntity(attach.getCurrLayer(), attach.getPassTime()));
        }
        ranking.saveDb();
        ServerLogger.warn("无尽漩涡错误数据修复成功");
    }

    @SuppressWarnings("unchecked")
    public <T extends IRankCA> RankingList<T> getRankingList(int type) {
        return (RankingList<T>) rankings.get(type);
    }


    public void saveDB(int type, byte[] bytes) {
        dao.updateRanking(type, bytes);
    }

    public void shutdown() {
        for (RankingList<?> ranking : rankings.values()) {
            if (ranking.isDirty()) {
                ranking.saveDb();
            }
        }
    }

    public void sort() {
        //战力
        List<Player> players = dao.selectFightRanking();
        RankingList<FightingRankEntity> fightingList = getRankingList(TYPE_FIGHTING);
        synchronized (fightingList) {
            fightingList.clear();
            Map<Integer, FightingRankEntity> fightingEntities = new ConcurrentHashMap<Integer, FightingRankEntity>();
            for (Player player : players) {
                fightingEntities.put(player.getPlayerId(), new FightingRankEntity(player.getFight()));
            }
            fightingList.putAll(fightingEntities);
        }

        players = dao.selectAchievementRanking();
        RankingList<LevelRankEntity> achievementList = getRankingList(TYPE_ACHIEVEMENT);
        synchronized (achievementList) {
            achievementList.clear();
            Map<Integer, LevelRankEntity> achievementEntities = new ConcurrentHashMap<>();
            int i = 1;
            for (Player player : players) {
                playerService.getPlayerData(player.getPlayerId());
                achievementEntities.put(player.getPlayerId(), new LevelRankEntity(player.getLev(), player.getExp(), player.getTotalChargeMoney()));
            }
            achievementList.putAll(achievementEntities);
        }
        ladderService.ladderSort();

        //获取5点的排行第一
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour == 5) {
            stateRank();
        }
    }

    // 排行第一模型
    public ListParam<StateRankVO> getStateRank() {
        ListParam<StateRankVO> listParam = new ListParam<>();
        listParam.params = new ArrayList<>();
        for (StateRank rank : serialDataService.getData().getStateRanks()) {
            listParam.params.add(rank.toProto());
        }
        return listParam;
    }

    // 排行第一模型
    public ListParam<StateRankVO> stateRank() {
        ListParam<StateRankVO> listParam = new ListParam<>();
        listParam.params = new ArrayList<>();

        List<StateRank> stateRanks = Lists.newArrayList();
        //战力第一
        RankingList<IRankCA> ranking = getRankingList(TYPE_FIGHTING);
        List<RankEntity> list = ranking.getOrderList();
        if (list != null && list.size() > 0) {
            stateRanks.add(getStateRankVO(list.get(0).getPlayerId(), 1));
        }

        //充值第一
        SerialData serialData = serialDataService.getData();
        if (serialData == null) {
            ServerLogger.warn("序列化数据不存在");
            listParam.code = Response.ERR_PARAM;
            return listParam;
        }

        Map<Integer, LevelRankVO> levelRankingsMap = serialData.getLevelRankingsMap();
        if (levelRankingsMap == null) {
            ServerLogger.warn("充值排行不存在");
            listParam.code = Response.ERR_PARAM;
            return listParam;
        }
        if (levelRankingsMap.size() > 0) {
            for (LevelRankVO levelRankVO : levelRankingsMap.values()) {
                stateRanks.add(getStateRankVO(levelRankVO.playerId, 2));
                break;
            }
        }

        //排位赛第一
        ListParam<LadderRankVO> ladderRank = ladderService.getLadderRank();
        List<LadderRankVO> params = ladderRank.params;
        if (params != null && params.size() > 0) {
            stateRanks.add(getStateRankVO(params.get(0).playerId, 3));
        }
        serialDataService.getData().setStateRanks(stateRanks);
        listParam.code = Response.SUCCESS;

        return listParam;
    }

    //获取排行第一
    private StateRank getStateRankVO(int playerId, int rankType) {
        Player player = playerService.getPlayer(playerId);
        StateRank stateRankVO = new StateRank();
        stateRankVO.setName(player.getName());
        stateRankVO.setVocation(player.getVocation());
        stateRankVO.setPlayerId(playerId);
        FashionInfo fashionInfo = fashionService.getFashionInfo(playerId);
        stateRankVO.setHead(fashionInfo.head);
        stateRankVO.setFashionId(fashionInfo.cloth);
        stateRankVO.setWeapon(fashionInfo.weapon);
        stateRankVO.setLevel(player.getLev());
        if (player.getGangId() > 0) {
            if (gangService.getGang(player.getGangId()) != null && gangService.getGang(player.getGangId()).getName() != null) {
                stateRankVO.setGang(gangService.getGang(player.getGangId()).getName());
            }
        }
        stateRankVO.setFightingValue(player.getFight());
        stateRankVO.setVip(player.getVip());
        stateRankVO.setTitle(player.getTitle());
        stateRankVO.setRankType(rankType);
        return stateRankVO;
    }

    //复制排行表
    public void copyRank(SimpleJdbcTemplate mergeDb) {
        StringBuffer sql = new StringBuffer("SELECT * FROM pet");
        List<Pet> list = mergeDb.query(sql.toString(), ParameterizedBeanPropertyRowMapper.newInstance(Pet.class));
        ServerLogger.warn("宠物表开始复制，复制数据=" + list.size());
        for (Pet object : list) {
            String str = JsonUtils.object2String(object);
            byte[] dbData = str.getBytes(Charset.forName("utf-8"));
//            petDao.insertPet(object.getId(), CompressUtil.compressBytes(dbData));
        }
        ServerLogger.warn("宠物表完成复制");
    }
}
