package com.game.module.rank;

import java.util.*;

import com.game.data.Response;
import com.game.module.copy.CopyService;
import com.game.module.fashion.FashionService;
import com.game.module.gang.Gang;
import com.game.module.ladder.LadderService;
import com.game.module.player.PlayerData;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.skill.SkillService;
import com.game.module.vip.VipService;
import com.game.params.rank.*;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.attach.arena.ArenaLogic;
import com.game.module.attach.arena.ArenaPlayer;
import com.game.module.gang.GangService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.rank.vo.EndlessRankEntity;
import com.game.module.rank.vo.FightingRankEntity;
import com.game.module.rank.vo.LevelRankEntity;
import com.game.params.IProtocol;
import com.game.params.ListParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class RankExtension {

    @Autowired
    private RankService rankService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GangService gangService;
    @Autowired
    private ArenaLogic arenaLogic;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private FashionService fashionService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private VipService vipService;

    // 战力
    private IParser<FightingRankVO> fightingParser = new IParser<FightingRankVO>() {
        @Override
        public FightingRankVO parse(RankEntity entity) {
            Player player = playerService.getPlayer(entity.getPlayerId());
            if (player == null) {
                return null;
            }
            FightingRankEntity rEntity = (FightingRankEntity) entity.getCa();
            FightingRankVO vo = new FightingRankVO();
            vo.name = player.getName();
            vo.level = player.getLev();
            vo.vocation = player.getVocation();
            if (player.getGangId() > 0) {
                vo.gang = gangService.getGang(player.getGangId()).getName();
            }
            vo.fightingValue = rEntity.getFight();
            vo.playerId = player.getPlayerId();
            vo.vip = player.getVip();
            return vo;
        }
    };

    @Command(3701)
    public ListParam<FightingRankVO> getFightingRankList(int playerId,
                                                         Object param) {
        return getRankList(playerId, RankService.TYPE_FIGHTING, FightingRankVO.class, fightingParser);
    }

    //等级
    private IParser<LevelRankVO> levelParser = new IParser<LevelRankVO>() {
        @Override
        public LevelRankVO parse(RankEntity entity) {
            Player player = playerService.getPlayer(entity.getPlayerId());
            if (player == null) {
                return null;
            }
            LevelRankEntity rEntity = (LevelRankEntity) entity.getCa();
            LevelRankVO vo = new LevelRankVO();
            vo.name = player.getName();
            vo.vocation = player.getVocation();
            if (player.getGangId() > 0) {
                Gang gang = gangService.getGang(player.getGangId());
                if (gang == null) {
                    ServerLogger.warn("gang do not exist,gang id = " + player.getGangId());
                    player.setGangId(0);
                } else {
                    vo.gang = gang.getName();
                }
            }
            vo.level = rEntity.getLevel();
            vo.playerId = player.getPlayerId();
            vo.fightingValue = player.getFight();
            vo.vip = player.getVip();

            PlayerData playerData = playerService.getPlayerData(player.getPlayerId());
            if (playerData == null) {
                ServerLogger.warn("玩家数据不存在，玩家ID=" + player.getPlayerId());
            } else {
                vo.coins = playerData.getTotalCharge();
            }

            return vo;
        }
    };

    @Command(3702)
    public ListParam<LevelRankVO> getLevelRankList(int playerId, Object param) {
        ListParam<LevelRankVO> listParam = new ListParam<>();
        listParam.params = vipService.getLevelRankings(playerId);
        return listParam;
    }

    // 声望
    private IParser<FameRankVO> fameParser = new IParser<FameRankVO>() {
        @Override
        public FameRankVO parse(RankEntity entity) {
            return null;
        }
    };

    @Command(3703)
    public ListParam<FameRankVO> getFameRankList(int playerId, Object param) {
        return getRankList(playerId, RankService.TYPE_FAME, FameRankVO.class, fameParser);
    }

    // 无尽漩涡
    private IParser<EndlessRankVO> endlessParser = new IParser<EndlessRankVO>() {
        @Override
        public EndlessRankVO parse(RankEntity entity) {
            Player player = playerService.getPlayer(entity.getPlayerId());
            if (player == null) {
                return null;
            }
            EndlessRankEntity rEntity = (EndlessRankEntity) entity.getCa();
            EndlessRankVO vo = new EndlessRankVO();
            vo.name = player.getName();
            vo.level = player.getLev();
            vo.vocation = player.getVocation();
            if (player.getGangId() > 0) {
                vo.gang = gangService.getGang(player.getGangId()).getName();
            }
            vo.maxLayer = rEntity.getLayer();
            vo.time = rEntity.getTime();
            vo.playerId = player.getPlayerId();
            vo.fightingValue = player.getFight();
            vo.vip = player.getVip();
            return vo;
        }
    };

    @Command(3704)
    public ListParam<EndlessRankVO> getEndlessRankList(int playerId,
                                                       Object param) {
        return getRankList(playerId, RankService.TYPE_ENDLESS, EndlessRankVO.class, endlessParser);
    }

    // AI竞技场
    @Command(3705)
    public ListParam<AIArenaRankVO> getAIArenaRankList(int playerId,
                                                       Object param) {
//        arenaLogic.repair();
        ListParam<AIArenaRankVO> result = new ListParam<AIArenaRankVO>();
        List<AIArenaRankVO> list = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            ArenaPlayer aplayer = arenaLogic.getArenaPlayerByRank(i);
            Player player = playerService.getPlayer(aplayer.getPlayerId());
            if (player == null) {
                return null;
            }
            AIArenaRankVO vo = new AIArenaRankVO();
            vo.name = player.getName();
            vo.level = player.getLev();
            vo.vocation = player.getVocation();
            if (player.getGangId() > 0) {
                vo.gang = gangService.getGang(player.getGangId()).getName();
            }
            vo.fightingValue = player.getFight();
            vo.playerId = player.getPlayerId();
            vo.vip = player.getVip();
            list.add(vo);
        }
        result.params = list;
        return result;
    }

    // 排位赛
    private IParser<PKArenaRankVO> pkArenaParser = new IParser<PKArenaRankVO>() {
        @Override
        public PKArenaRankVO parse(RankEntity entity) {
            return null;
        }
    };

    @Command(3706)
    public ListParam<PKArenaRankVO> getPKArenaRankList(int playerId,
                                                       Object param) {
        return getRankList(playerId, RankService.TYPE_PK_ARENA, PKArenaRankVO.class, pkArenaParser);
    }


    private <T extends IProtocol> ListParam<T> getRankList(int playerId, int type,
                                                           Class<T> clazz, IParser<T> parser) {
        RankingList<IRankCA> ranking = rankService.getRankingList(type);
        List<RankEntity> list = ranking.getOrderList();
        ListParam<T> result = new ListParam<>();
        result.params = new ArrayList<>();
        for (RankEntity entity : list) {
            result.params.add(parser.parse(entity));
        }
        return result;
    }

    // 排位赛
    @Command(3707)
    public ListParam<LadderRankVO> getLadderRankList(int playerId, Object param) {
        return ladderService.getLadderRank();
    }

    // 排位第一模型
    @Command(3708)
    public ListParam<StateRankVO> stateRank(int playerId, Object param) {
        return rankService.getStateRank();
    }

    // 3星副本排行榜
//    @Command(3709)
//    public ListParam<NormalCopyRankVO> getMaxStarCopyRankings(int playerId, Object param) {
//        return copyService.getMaxStarCopyRankings();
//    }

    // 时装排行榜
//    @Command(3710)
//    public ListParam<FashionCopyRankVO> getFashionRankings(int playerId, Object param) {
//        return fashionService.getFashionRankings();
//    }

    // 技能卡排行榜
//    @Command(3711)
//    public ListParam<SkillCardRankVO> getSkillCardRankings(int playerId, Object param) {
//        return skillService.getSkillCardRankings();
//    }
}
