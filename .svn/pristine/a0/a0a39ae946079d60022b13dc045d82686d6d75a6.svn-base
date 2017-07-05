package com.game.module.attach;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface AttachDao {

	@SQL("select playerId,type,extraInfo from attach where playerId=:id")
	public List<Attach> getAttach(@SQLParam("id")int playerId);
	
	@SQL("select * from attach where type=:t")
	public List<Attach> getAllAttachByType(@SQLParam("t")int type);
	
	@SQL("update attach set extraInfo=:a.extraInfo where playerId=:a.playerId and type=:a.type")
	public void update(@SQLParam("a")Attach attach);
	
	@SQL("insert into attach values(:att.playerId, :att.type, :att.extraInfo)")
	public void insert(@SQLParam("att")Attach attach);
	
	@SQL("delete from attach where type=:t")
	public void clear(@SQLParam("t")byte type);
	
	@SQL("select playerId from attach where type=:t")
	public List<Integer> getAllPlayer(@SQLParam("t")int type);
}
