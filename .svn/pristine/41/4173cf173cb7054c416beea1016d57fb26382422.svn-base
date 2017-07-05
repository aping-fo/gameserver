package com.game.module.goods;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface GoodsDao {
	
	@SQL("insert into goods(playerId) values(:playerId)")
	public void insert(@SQLParam("playerId")int playerId);
	
	@SQL("select data from goods where playerId=:playerId")
	public byte[] select(@SQLParam("playerId")int playerId);
	
	@SQL("update goods set data=:data where playerId=:playerId")
	public void update(@SQLParam("playerId")int playerId,@SQLParam("data")byte[]data);
}
