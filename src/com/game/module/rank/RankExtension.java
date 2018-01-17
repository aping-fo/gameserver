package com.game.module.rank;

import java.util.ArrayList;
import java.util.List;

import com.game.module.gang.Gang;
import com.game.module.ladder.LadderService;
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
			if(player.getGangId() > 0){
				vo.gang = gangService.getGang(player.getGangId()).getName();
			}
			vo.fightingValue = rEntity.getFight();
			return vo;
		}
	};
	@Command(3701)
	public ListParam<FightingRankVO> getFightingRankList(int playerId,
			Object param) {
		return getRankList(playerId,RankService.TYPE_FIGHTING, FightingRankVO.class, fightingParser);
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
			if(player.getGangId() > 0){
				Gang gang = gangService.getGang(player.getGangId());
				if(gang == null){
					ServerLogger.warn("gang do not exist,gang id = " + player.getGangId());
					player.setGangId(0);
				}else{
					vo.gang = gang.getName();
				}
			}
			vo.level = rEntity.getLevel();
			return vo;
		}
	};
	@Command(3702)
	public ListParam<LevelRankVO> getLevelRankList(int playerId,
			Object param) {
		return getRankList(playerId, RankService.TYPE_LEVEL, LevelRankVO.class, levelParser);
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
			if(player.getGangId() > 0){
				vo.gang = gangService.getGang(player.getGangId()).getName();
			}
			vo.maxLayer = rEntity.getLayer();
			vo.time = rEntity.getTime();
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
		ListParam<AIArenaRankVO> result = new ListParam<AIArenaRankVO>();
		List<AIArenaRankVO> list = new ArrayList<>();
		for(int i = 1; i <= 50; i++){
			ArenaPlayer aplayer = arenaLogic.getArenaPlayerByRank(i);
			Player player = playerService.getPlayer(aplayer.getPlayerId());
			if (player == null) {
				return null;
			}
			AIArenaRankVO vo = new AIArenaRankVO();
			vo.name = player.getName();
			vo.level = player.getLev();
			vo.vocation = player.getVocation();
			if(player.getGangId() > 0){
				vo.gang = gangService.getGang(player.getGangId()).getName();
			}
			vo.fightingValue = player.getFight();
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
		RankingList<IRankCA> ranking = rankService
				.getRankingList(type);
		List<RankEntity> list = ranking.getOrderList();
		ListParam<T> result = new ListParam<T>();
		result.params = new ArrayList<T>();
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
}
