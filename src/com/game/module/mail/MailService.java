package com.game.module.mail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.ErrCode;
import com.game.data.GoodsConfig;
import com.game.data.Response;
import com.game.data.RewardMailCfg;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.mail.MailVo;
import com.game.params.mail.SMailReward;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.server.SessionManager;

@Service
public class MailService {

	public static final int ENDLESS_RANK = 10000;//无尽漩涡排行榜奖励邮件
	public static final int ARENA_RANK = 10001;//AI竞技场
	public static final int WORLD_BOSS_RANK = 10002;//世界BOSS伤害排名奖励
	public static final int WORLD_BOSS_KILL = 10003;//世界BOSS击杀奖励
	public static final int WORLD_BOSS_LAST_BEAT = 10004;//世界BOSS最后一击奖励

	@Autowired
	private MailDao mailDao;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PlayerService playerService;
	
	//邮件的标题内容，也是配置在错误码表
	public String getCode(int errCode){
		ErrCode cfg  = ConfigData.getConfig(ErrCode.class, errCode);
		return cfg.tips;
	}

	// 领取奖励
	public SMailReward getReward(int playerId, long id) {
		Player player = playerService.getPlayer(playerId);
		
		SMailReward result = new SMailReward();
		Mail mail = null;
		synchronized (player) {
			mail= mailDao.selectMail(id, playerId);
			if (mail.getHasReward() == 0 || mail.getRewardsMap().isEmpty()) {
				result.code = Response.ERR_PARAM;
				return result;
			}
		}
		
		List<GoodsEntry> rewards = new ArrayList<GoodsEntry>(mail.getRewardsMap().size());
		for (Entry<Integer, Integer> reward : mail.getRewardsMap().entrySet()) {
			rewards.add(new GoodsEntry(reward.getKey(), reward.getValue()));
		}
		if (!goodsService.checkCanAddToBag(playerId, rewards)) {
			result.code = Response.BAG_FULL;
			return result;
		}
		
		mailDao.updateReward(id);
		LogConsume log = null;
		if(mail.getType()>0){
			log = LogConsume.getLog(mail.getType());
		}
		goodsService.addRewards(playerId, rewards, log);
		
		result.rewards = mail.getRewards();
		return result;
	}
	
	// 发送系统奖励邮件
	public void sendSysMailRewards(String title, String content, int[][] rewards, final int receiverId, LogConsume log) {
		StringBuilder rewardStr = new StringBuilder();
		if (rewards != null) {
			for (int i = 0; i < rewards.length; i++) {
				GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, rewards[i][0]);
				if(cfg.vocation>0){
					Player player = playerService.getPlayer(receiverId);
					if(player.getVocation()!=cfg.vocation){
						continue;
					}
				}
				rewardStr.append(rewards[i][0]).append(":").append(rewards[i][1]);
				if (i != rewards.length - 1) {
					rewardStr.append(";");
				}
			}
		}
		sendSysMailInner(title, content, rewardStr.toString(), receiverId, log);
	}
	
	

	// 发送系统奖励邮件
	public void sendSysMail(String title, String content, List<GoodsEntry> rewards, final int receiverId,LogConsume log) {
		StringBuilder rewardStr = new StringBuilder();
		if(rewards!=null){
		for (int i = 0; i < rewards.size(); i++) {
			GoodsEntry item = rewards.get(i);
			
			rewardStr.append(item.id).append(":").append(item.count);
			if (i != rewards.size() - 1) {
				rewardStr.append(";");
			}
		}
		}
		sendSysMailInner(title, content, rewardStr.toString(), receiverId, log);
	}
	
	//使用奖励邮件模板发放奖励
	public void sendRewardMail(int playerId, int group, int rank, LogConsume log, Object... param){
		List<RewardMailCfg> cfgs = ConfigData.rewardMails.get(group);
		if(cfgs == null){
			throw new IllegalArgumentException("send reward mail fail, group is error,group=" + group);
		}
		for(RewardMailCfg cfg : cfgs){
			if(rank < cfg.rank[0] || rank > cfg.rank[1]){
				continue;
			}
			List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
			for(Map.Entry<Integer, Integer> entry : cfg.reward.entrySet()){
				rewards.add(new GoodsEntry(entry.getKey(), entry.getValue()));
			}
			sendSysMail(cfg.title, MessageFormat.format(cfg.content, param), rewards, playerId, log);
			break;
		}
	}
	
	private void sendSysMailInner(String title, String content,String rewardStr, final int receiverId,LogConsume log){
		String sender = ConfigData.getConfig(ErrCode.class, Response.SYS).tips;
		
		final Mail mail = new Mail();
		mail.setSenderId(0);
		mail.setSenderName(sender);

		mail.setTitle(title);
		mail.setContent(content);
		mail.setRewards(rewardStr);
		if (rewardStr!=null&&!rewardStr.isEmpty()) {
			mail.setHasReward(1);
		}
		mail.setReceiveId(receiverId);
		mail.setSendTime(new Date());
		if(log!=null){
			mail.setType(log.actionId);
		}

		Context.getThreadService().execute(new Runnable() {
			@Override
			public void run() {
				mailDao.insert(mail);
				SessionManager.getInstance().sendMsg(MailExtension.NEW_MAIL, toVo(mail), receiverId);
			}
		});
	}

	public MailVo toVo(Mail mail) {
		MailVo vo = new MailVo();
		vo.id = mail.getId();
		vo.content = mail.getContent();
		vo.hasReward = mail.getHasReward() == 1;
		vo.receiverId = mail.getReceiveId();
		vo.senderId = mail.getSenderId();
		vo.senderName = mail.getSenderName();
		vo.title = mail.getTitle();
		vo.rewards = mail.getRewards();
		vo.state = mail.getState();
		vo.sendTime = mail.getSendTime().getTime();
		return vo;
	}

	public SMailReward takenAll(int playerId) {
		List<Mail> mails = mailDao.selectMails(playerId);
		List<GoodsEntry> rewards = new ArrayList<GoodsEntry>();
		SMailReward result = new SMailReward();
		StringBuilder rewardStr = new StringBuilder();
		for (Mail mail : mails) {
			if (mail.getHasReward() == 1 && !mail.getRewardsMap().isEmpty()) {
				for (Entry<Integer, Integer> entry : mail.getRewardsMap().entrySet()) {
					rewards.add(new GoodsEntry(entry.getKey(), entry.getValue()));
				}
				//日志
				LogConsume log = LogConsume.GM;
				if(mail.getType()>0){
					log = LogConsume.getLog(mail.getType());
				}
				//检查背包
				if (!goodsService.checkCanAddToBag(playerId, rewards)) {
					result.rewards = rewardStr.toString();
					result.code = Response.BAG_FULL;
					return result;
				}
				mailDao.updateReward(mail.getId());
				goodsService.addRewards(playerId, rewards, log);
				for(GoodsEntry g:rewards){
					rewardStr.append(g.id).append(":").append(g.count).append(";");
				}
				rewards.clear();
			}
		}
		
		//mailDao.takenAllRewards(playerId);

		result.rewards = rewardStr.toString();
		return result;
	}

	// 批量发送邮件
	public void sendBatchMail(List<Object[]> params) {
		Context.batchDb(MailDao.BATCH_INSERT_MAIL, params);
	}
}
