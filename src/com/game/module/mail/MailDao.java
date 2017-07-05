package com.game.module.mail;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface MailDao {
	
	public static final String BATCH_INSERT_MAIL = "insert into mail(senderId,senderName,receiveId,title,content,sendTime,state,rewards,hasReward,type) values(?,?,?,?,?,now(),0,?,?,?)";
	
	@SQL("select * from mail where receiveId = :playerId and state <>2 order by id desc limit 30")
	public List<Mail> selectMails(@SQLParam("playerId")int playerId);
	
	@SQL("update mail set state = :state where id=:id and receiveId=:playerId")
	public int setState(@SQLParam("id")long id,@SQLParam("playerId")int playerId,@SQLParam("state")int state);
	
	@SQL("select * from mail where id = :id and receiveId = :playerId")
	public Mail selectMail(@SQLParam("id")long id,@SQLParam("playerId")int playerId);
	
	@SQL("insert into mail(senderId,senderName,receiveId,title,content,sendTime,state,rewards,hasReward,type) values(:m.senderId,:m.senderName,:m.receiveId,:m.title,:m.content,now(),:m.state,:m.rewards,:m.hasReward,:m.type)")
	public void insert(@SQLParam("m")Mail mail);
	
	@SQL("update mail set state=:state where receiveId = :playerId and hasReward=0")
	public int delAll(@SQLParam("state")int state,@SQLParam("playerId")int playerId);
	
	@SQL("update mail set hasReward=0,state=1 where receiveId = :playerId and hasReward=1 ")
	public void takenAllRewards(@SQLParam("playerId")int playerId);
	
	@SQL("delete from mail where state = 2 or sendTime< DATE_ADD(CURDATE(), INTERVAL -7 DAY)")
	public void delUnvalid();
	
	@SQL("update mail set hasReward=0,state=1 where id = :id")
	public void updateReward(@SQLParam("id")long id);
	
	@SQL("select count(*) from mail where receiveId = :playerId and state =0")
	public int selectNewMails(@SQLParam("playerId")int playerId);
	

}
