package com.game.module.rank;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

import com.game.module.player.Player;

@DAO
public interface RankingDao {

	@SQL("REPLACE INTO rank VALUES(:type, :data)")
	public void updateRanking(@SQLParam("type")int type, @SQLParam("data")byte[] bytes);
	
	@SQL("SELECT data FROM rank WHERE type=:type")
	public byte[] selectRanking(@SQLParam("type")int type);
	
	@SQL("SELECT playerId, fight FROM player where accName!='sys' ORDER BY fight DESC LIMIT 50")
	public List<Player> selectFightRanking();
	
	@SQL("SELECT playerId, lev, exp FROM player where accName != 'sys' ORDER BY lev DESC, exp DESC LIMIT 50")
	public List<Player> selectLevelRanking();

	@SQL("SELECT playerId, lev, achievement FROM player ORDER BY achievement DESC LIMIT 50")
	public List<Player> selectAchievementRanking();
}
