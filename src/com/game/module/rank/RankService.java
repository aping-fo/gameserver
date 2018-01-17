package com.game.module.rank;

import com.game.event.InitHandler;
import com.game.module.ladder.LadderService;
import com.game.module.player.Player;
import com.game.module.rank.vo.EndlessRankEntity;
import com.game.module.rank.vo.FightingRankEntity;
import com.game.module.rank.vo.LevelRankEntity;
import com.game.module.title.TitleService;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;
import com.game.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RankService implements InitHandler {

	public static final int TYPE_FIGHTING = 1;
	public static final int TYPE_LEVEL = 2;
	public static final int TYPE_FAME = 3;
	public static final int TYPE_ENDLESS = 4;
	public static final int TYPE_AI_ARENA = 5;
	public static final int TYPE_PK_ARENA = 6;
	
	@Autowired
	private RankingDao dao;
	@Autowired
	private LadderService ladderService;
	@Autowired
	private TitleService titleService;

	private final Map<Integer, RankingList<? extends IRankCA>> rankings = new ConcurrentHashMap<Integer, RankingList<? extends IRankCA>>();
	
	@Override
	public void handleInit() {
		register(TYPE_FIGHTING, 50, (int)TimeUtil.ONE_HOUR, FightingRankEntity.class);
		register(TYPE_LEVEL, 50, (int)TimeUtil.ONE_HOUR, LevelRankEntity.class);
		register(TYPE_ENDLESS, 50, (int)TimeUtil.ONE_HOUR, EndlessRankEntity.class);
		sort();
	}
	
	private <T extends IRankCA> void register(int type, int maxCapacity, int updatePeriod,  Class<T> entityClass){
		if(rankings.containsKey(type)){
			throw new RuntimeException("duplicate key:" + type);
		}
		RankingList<T> ranking = new RankingList<T>(type, maxCapacity, updatePeriod);
		rankings.put(type, ranking);
		byte[] bytes = dao.selectRanking(type);
		if(bytes != null && bytes.length > 0){
			String data = new String(CompressUtil.decompressBytes(bytes), Charset.forName("utf-8"));
			Map<Integer, T> keys = JsonUtils.string2Map(data, Integer.class, entityClass);
			if(keys != null && !keys.isEmpty()){				
				ranking.putAll(keys);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IRankCA> RankingList<T> getRankingList(int type){
		return (RankingList<T>) rankings.get(type);
	}
	

	public void saveDB(int type, byte[] bytes){
		dao.updateRanking(type, bytes);
	}
	
	public void shutdown(){
		for(RankingList<?> ranking : rankings.values()){
			if(ranking.isDirty()){				
				ranking.saveDb();
			}
		}
	}
	
	public void sort(){
		//战力
		List<Player> players = dao.selectFightRanking();
		RankingList<FightingRankEntity> fightingList = getRankingList(TYPE_FIGHTING);
		synchronized (fightingList) {
			fightingList.clear();
			Map<Integer, FightingRankEntity> fightingEntities = new ConcurrentHashMap<Integer, FightingRankEntity>();
			for(Player player : players){
				fightingEntities.put(player.getPlayerId(), new FightingRankEntity(player.getFight()));
				//titleService.complete(player.getPlayerId(), TitleConsts.FIGHTING,player.getFight(), ActivityConsts.UpdateType.T_VALUE);
			}
			fightingList.putAll(fightingEntities);
		}
		
		//等级???? 为啥这里排过了，下面还要treemap去排序,有点搞不懂了
		players = dao.selectLevelRanking();
		RankingList<LevelRankEntity> levelList = getRankingList(TYPE_LEVEL);
		synchronized (fightingList) {
			levelList.clear();
			Map<Integer, LevelRankEntity> levelEntities = new ConcurrentHashMap<Integer, LevelRankEntity>();
			for(Player player : players){
				levelEntities.put(player.getPlayerId(), new LevelRankEntity(player.getLev(), player.getExp()));
			}
			levelList.putAll(levelEntities);
		}

		ladderService.ladderSort();
	}
}
