package com.game.module.pet;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface PetDao {

	@SQL("insert into pet(playerId) values(:playerId)")
	public void insert(@SQLParam("playerId") int playerId);

	@SQL("select data from pet where playerId=:playerId")
	public byte[] select(@SQLParam("playerId") int playerId);

	@SQL("update pet set data=:data where playerId=:playerId")
	public void update(@SQLParam("playerId") int playerId, @SQLParam("data") byte[] data);
}
