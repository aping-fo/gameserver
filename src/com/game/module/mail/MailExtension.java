package com.game.module.mail;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.game.util.Context;
import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.LongParam;
import com.game.params.mail.MailVo;
import com.server.SessionManager;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class MailExtension {
	
	@Autowired
	private MailDao mailDao;
	@Autowired
	private MailService mailService;
	@Autowired
	private PlayerService playerService;

	//获取邮件
	@Command(1401)
	public Object getMailList(int playerId,Object param){
		ListParam<MailVo> result = new ListParam<MailVo>();
		List<Mail> mails = mailDao.selectMails(playerId);
		if(!mails.isEmpty()){
			result.params = new ArrayList<MailVo>();
			for(Mail mail:mails){
				result.params.add(mailService.toVo(mail));
			}
		}
		return result;
	}
	
	//提取单个附件
	@Command(1402)
	public Object getReward(int playerId,LongParam id){
		return mailService.getReward(playerId, id.param);
	}
	
	//删除单个
	@Command(1403)
	public Object delOne(int playerId,LongParam id){
		mailDao.setState(id.param, playerId, Mail.DEL);
		return new IntParam();
	}
	
	//一键删除
	@Command(1404)
	public Object delAll(int playerId,Object param){
		mailDao.delAll(Mail.DEL, playerId);
		return new IntParam();
	}
	
	//设置已读
	@Command(1405)
	public Object setRead(int playerId,LongParam param){
		mailDao.setState(param.param, playerId, Mail.READED);
		return new IntParam();
	}
	
	//发送邮件
	@Command(1406)
	public Object sendMail(int playerId,MailVo param){
		Player sender = playerService.getPlayer(playerId);
		
		Mail mail = new Mail();
		mail.setSenderId(playerId);
		mail.setSenderName(sender.getName());
		
		mail.setReceiveId(param.receiverId);
		mail.setTitle(param.title);
		mail.setContent(param.content);
		mail.setRewards(param.rewards);
		if(param.rewards!=null&&!param.rewards.isEmpty()){
			mail.setHasReward(1);
		}
		mail.setSendTime(new Date());
		mailDao.insert(mail);
		Context.getLoggerService().logMail(mail.getSenderId(), mail.getSenderName(), mail.getReceiveId(), mail.getTitle(), mail.getContent(), mail.getState(), mail.getRewards(), mail.getHasReward(), mail.getType());

		SessionManager.getInstance().sendMsg(NEW_MAIL,  null,param.receiverId);
		
		return new IntParam();
	}
	
	//一键提取
	@Command(1407)
	public Object takenAll(int playerId,Object param){
		return mailService.takenAll(playerId);
	}
	
	public static final int NEW_MAIL = 1408;
	
}
