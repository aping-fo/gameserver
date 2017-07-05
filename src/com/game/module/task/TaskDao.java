package com.game.module.task;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface TaskDao {
	
	@SQL("insert into task(playerId) values(:playerId)")
	public void insert(@SQLParam("playerId")int playerId);
	
	@SQL("select data from task where playerId=:playerId")
	public byte[] select(@SQLParam("playerId")int playerId);
	
	@SQL("update task set data=:data where playerId=:playerId")
	public void update(@SQLParam("playerId")int playerId,@SQLParam("data")byte[]data);
	

}
