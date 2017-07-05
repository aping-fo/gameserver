package com.game.module.gang;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface GangDao {
	
	@SQL("select data from gang")
	public List<byte[]> selectGangs();
	
	@SQL("select max(id) from gang")
	public Integer selectMaxGangId();

	public static final String UPDATE = "REPLACE INTO gang VALUES(?,?)";
	
	@SQL("insert into gang(id,data) values(:id,:data)")
	public void insert(@SQLParam("id")int id,@SQLParam("data")byte[] data);
	
	@SQL("delete from gang where id=:id")
	public void del(@SQLParam("id")int id);
	
	@SQL("select id from gang order by totalFight desc")
	public List<Integer> selectRanks();
}
