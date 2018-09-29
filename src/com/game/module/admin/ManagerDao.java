package com.game.module.admin;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface ManagerDao {

	@SQL("select * from manager")
	public List<UserManager> all();
	
	@SQL("update manager set banChat=:u.banChat,banLogin=:u.banLogin,banChatEnd=:u.banChatEnd,banLoginEnd=:u.banLoginEnd where playerId =:u.playerId")
	public void update(@SQLParam("u")UserManager u);
	
	//@SQL("insert manager(playerId,banChat,banLogin,banChatEnd,banLoginEnd) values(:u.playerId,:u.banChat,:u.banLogin,:u.banChatEnd,:u.banLoginEnd)")
	//public void insert(@SQLParam("u")UserManager u);

	@SQL("REPLACE INTO  manager VALUES(:u.playerId,:u.banChat,:u.banLogin,:u.banChatEnd,:u.banLoginEnd)")
	public void insert(@SQLParam("u")UserManager u);
}
