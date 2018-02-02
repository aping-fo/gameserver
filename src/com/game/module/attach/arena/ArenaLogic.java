package com.game.module.attach.arena;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.module.activity.ActivityConsts;
import com.game.module.admin.MessageConsts;
import com.game.module.admin.MessageService;
import com.game.module.player.PlayerData;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.GlobalConfig;
import com.game.data.Response;
import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.robot.RobotService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.arena.ArenaReportVO;
import com.game.params.arena.ArenaResultVO;
import com.game.util.ConfigData;
import com.server.SessionManager;

@Service
public class ArenaLogic extends AttachLogic<ArenaAttach> {

	public static final int ARENA_UPGRADE=2557;//你挑战{0}胜利，排名提升至{1}名！
	public static final int ARENA_WIN=2558;//你挑战{0}胜利！
	public static final int ARENA_LOST=2559;//你挑战{0}失败，排名不变！
	public static final int ARENA_DOWNGRADE=2560;//你被{0}击败！排名下降至{1}名！
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private SerialDataService serialDataService;
	@Autowired
	private RobotService robotService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private MailService mailService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TitleService titleService;

	private SerialData serialData;
	private ConcurrentHashMap<Integer, ArenaPlayer> ranks;
	private ConcurrentHashMap<Integer, ArenaPlayer> playerRanks;
	private AtomicInteger minRank;
	
	@Override
	public byte getType() {
		return AttachType.ARENA;
	}

	@Override
	public void handleInit() {
		super.handleInit();
	}

	@Override
	public ArenaAttach generalNewAttach(int playerId) {
		ArenaPlayer aPlayer = searchArenaPlayer(playerId);
		ArenaAttach attach = new ArenaAttach(playerId, getType());
		GlobalConfig config = ConfigData.globalParam();
		attach.setChallenge(config.arenaChallenge);
		attach.setUniqueId(aPlayer.getUniqueId());
		return attach;
		
	}

	//自动加入竞技场(生成前面的机器人)
	public void autoArenaRobot(){
		serialData = serialDataService.getData();
		ranks = serialData.getRanks();
		minRank = new AtomicInteger(ranks.size());
		playerRanks = serialData.getPlayerRanks();
		if(serialData.getInitArena()){
			return;
		}
		List<Integer> robots = robotService.getRobots();
		for(int id : robots){
			generalArenaPlayer(id);
		}
		serialData.setInitArena(true);
	}

	public ArenaPlayer searchArenaPlayer(int playerId) {
		ArenaPlayer arenaPlayer = playerRanks.get(playerId);
		if(arenaPlayer == null) {
			arenaPlayer = generalArenaPlayer(playerId);
		}
		return arenaPlayer;
	}

	public ArenaPlayer generalArenaPlayer(int playerId){
		int rank = minRank.incrementAndGet();
		ArenaPlayer aPlayer = new ArenaPlayer(playerId, playerId,rank);
		ranks.put(rank, aPlayer);
		playerRanks.put(playerId, aPlayer);
		return aPlayer;
	}
	
	public ArenaPlayer getArenaPlayer(int playerId){
		ArenaAttach attach = getAttach(playerId);
		int uniqueId = attach.getUniqueId();
		if(uniqueId == 0){
			return searchArenaPlayer(playerId);
		}
		return getArenaPlayerByUniqueId(uniqueId);
	}
	
	public ArenaPlayer getArenaPlayerByUniqueId(int uniqueId){
		return playerRanks.get(uniqueId);
	}
	
	public ArenaPlayer getArenaPlayerByRank(int rank){
		return ranks.get(rank);
	}
	
	public int getMinRank(){
		return minRank.get();
	}
	
	public void dailyReset(int playerId){
		ArenaAttach attach = getAttach(playerId);
		attach.setBuyCount(0);
		GlobalConfig config = ConfigData.globalParam();
		attach.setChallenge(config.arenaChallenge);
		attach.commitSync();
	}
	
	public void quit(int playerId){
		ArenaAttach attach = getAttach(playerId);
		if(attach.getOpponent() > 0){
			takeResult(playerId, false);
		}
	}
	
	public ArenaResultVO takeResult(int playerId, boolean isWin){
		ArenaResultVO vo = new ArenaResultVO();
		ArenaAttach attach = getAttach(playerId);
		ArenaPlayer me = getArenaPlayer(playerId);
		int opponentId = attach.getOpponent();
		if(opponentId == 0){
			vo.code = Response.ERR_PARAM;
			return vo;
		}
		attach.setOpponent(0);
		int record = attach.getRecord();
		GlobalConfig config = ConfigData.globalParam();
		Player player = playerService.getPlayer(playerId);
		ArenaPlayer opponent = getArenaPlayerByUniqueId(opponentId);
		Player oppPlayer = playerService.getPlayer(opponent.getPlayerId());
		PlayerData data = playerService.getPlayerData(playerId);
		Map<Integer, Integer> rewards;

		Map<Integer,int[]> condParams = Maps.newHashMap();
		if(isWin){
			//win
			if(record <= 0){
				record = 1;
			}else{
				record++;
			}
			rewards = config.arenaWinReward;
			data.setArenaWins(data.getArenaWins() + 1);
			condParams.put(Task.TYPE_ARENA_WINS,new int[]{data.getArenaWins()});
			int meRank = me.getRank();
			if(meRank > opponent.getRank()){ //交换排名,此处会不会有线程安全问题,造成同名?
				int oldOppo = opponent.getRank();
				me.setRank(opponent.getRank());
				opponent.setRank(meRank);
				ranks.put(me.getRank(), me);
				ranks.put(opponent.getRank(), opponent);
				sendReport(playerId, ARENA_UPGRADE, oppPlayer.getName(), me.getRank());
				sendReport(opponent.getPlayerId(), ARENA_DOWNGRADE, player.getName(), opponent.getRank());

				if(oldOppo == 1) {
					//广播竞技场第一名消息
					messageService.sendSysMsg(MessageConsts.MSG_AREA,player.getName(),oppPlayer.getName());
				}
				//称号
				titleService.complete(playerId, TitleConsts.ARENA,meRank, ActivityConsts.UpdateType.T_VALUE);
				//taskService.doTask(playerId,Task.TYPE_ARENA_RANK,meRank);
				condParams.put(Task.TYPE_ARENA_RANK,new int[]{meRank});
			}else{
				sendReport(playerId, ARENA_WIN, oppPlayer.getName(), 0);
			}
		}else{
			data.setArenaWins(0);
			//lost
			if(record >= 0){
				record = -1;
			}else{
				record--;
			}
			rewards = config.arenaLostReward;
			sendReport(playerId, ARENA_LOST, oppPlayer.getName(),0);
		}
		List<Reward> rewardList = new ArrayList<Reward>();
		for(Map.Entry<Integer, Integer> entry : rewards.entrySet()){
			Reward re = new Reward();
			re.id = entry.getKey();
			re.count = entry.getValue();
			rewardList.add(re);
		}
		vo.rewards = rewardList;
		vo.record = record;
		attach.setRecord(record);
		attach.commitSync();
		goodsService.addRewards(playerId, rewards, LogConsume.ARENA_REWARD, isWin);
		vo.currRank = me.getRank();

		condParams.put(Task.FINISH_JOIN_PK,new int[]{1,1});
		condParams.put(Task.TYPE_ARENA_TIMES,new int[]{1});
		taskService.doTask(playerId, condParams);
		//taskService.doTask(playerId, Task.FINISH_JOIN_PK, 1,1);
		return vo;
	}

	@Override
	public ArenaAttach getAttach(int playerId) {
		return super.getAttach(playerId);
	}

	private void sendReport(int playerId, int code, String name, int rank){
		ArenaReportVO vo = new ArenaReportVO();
		vo.id = code;
		vo.name = name;
		vo.rank = rank;
		if(SessionManager.getInstance().isActive(playerId)){
			ListParam<ArenaReportVO> msg = new ListParam<ArenaReportVO>();
			msg.params = Arrays.asList(vo);
			SessionManager.getInstance().sendMsg(ArenaExtension.REPORT, msg, playerId);
		}else{
			ArenaAttach attach = getAttach(playerId);
			attach.getReport().add(vo);
		}
	}
	
	//发放奖励，由ServerTimer定时调用
	public void sendRankReward(){
		int size = getMinRank();
		for(int i = 1; i <= size; i++){
			ArenaPlayer aPlayer = getArenaPlayerByRank(i);
			if(robotService.isRobot(aPlayer.getPlayerId())) continue;
			mailService.sendRewardMail(aPlayer.getPlayerId(), MailService.ARENA_RANK, i, LogConsume.ARENA_RANK_REWARD, String.valueOf(i));
		}
	}
}
