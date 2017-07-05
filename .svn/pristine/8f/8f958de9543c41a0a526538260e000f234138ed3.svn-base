package com.game.module.task;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.Response;
import com.game.data.TaskConfig;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.task.TaskListInfo;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.server.util.GameData;

@Extension
public class TaskExtension {

	@Autowired
	private TaskService taskService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PlayerService playerService;

	public static final int TASK_LIST_INFO = 1301;
	@Command(1301)
	public TaskListInfo getTask(int playerId, Object param) {
		return taskService.getCurTasks(playerId);
	}

	public static final int TASK_UPDATE = 1302;

	// 提交任务
	@Command(1303)
	public Object submit(int playerId, Int2Param param) {
		int taskId = param.param1;
		Int2Param result = new Int2Param();
		result.param2 = taskId;
		// 验证是否完成
		PlayerTask playerTask = taskService.getPlayerTask(playerId);
		Task task = param.param2 == 0 ?playerTask.getTasks().get(taskId) : playerTask.getCurrJointedTask();
		if (task == null || task.getState() != Task.STATE_FINISHED) {
			result.param1 = Response.ERR_PARAM;
			return result;
		}
		// 设置状态
		task.setState(Task.STATE_SUBMITED);
		TaskConfig taskCfg = taskService.getConfig(taskId); // 奖励物品
		goodsService.addRewards(playerId, taskCfg.rewards,
				LogConsume.TASK_REWARD, taskId);

		if(taskCfg.taskType == Task.TYPE_TASK){
			if(taskCfg.nextTaskId > 0){
				TaskConfig newTaskCfg = GameData.getConfig(TaskConfig.class, taskCfg.nextTaskId);
				if(newTaskCfg != null){
					Task newTask = taskService.addNewTask(playerId, taskCfg.nextTaskId, false);
					if(taskCfg.group == newTaskCfg.group){						
						newTask.setCount(task.getCount());
						taskService.checkFinished(newTask);
						taskService.updateTaskToClient(playerId, task);
					}else{
						taskService.doTask(playerId, taskCfg.finishType, taskCfg.finishParam);
					}
				}
			}
		}else if (taskCfg.taskType == Task.TYPE_JOINT) {
			JointTask myJointedTask = playerTask.getCurrJointedTask();
			if (myJointedTask == task) {
				playerTask.setCurrJointedTask(null);
				playerTask.alterJointedCount(1);
			}
		}
		if(taskCfg.liveness > 0){
			playerTask.alterLiveness(taskCfg.liveness);
			playerService.updateAttrsToClient(playerId, Player.LIVENESS, playerTask.getLiveness());
		}
		return result;
	}

	// 邀请任务
	@Command(1304)
	public Object inviteJoint(int playerId, Int2Param param) {
		int taskId = param.param1;
		Int2Param result = new Int2Param();
		result.param1 = taskService.inviteJointTask(playerId, taskId,
				param.param2);
		result.param2 = taskId;
		return result;
	}

	// 拒绝任务
	@Command(1305)
	public Object refuseJoint(int playerId, Int2Param param) {
		int taskId = param.param1;
		Int2Param result = new Int2Param();
		result.param1 = taskService.refuseJoint(playerId, taskId,
				param.param2);
		result.param2 = taskId;
		return result;
	}

	// 接受任务
	@Command(1306)
	public Object acceptJoint(int playerId, Int2Param param) {
		int taskId = param.param1;
		Int2Param result = new Int2Param();
		result.param1 = taskService.acceptJoint(playerId, taskId,
				param.param2);
		result.param2 = taskId;
		return result;
	}
	
	//领取活跃度奖励
	@Command(1307)
	public Object takeLivenessReward(int playerId, IntParam param){
		PlayerTask playerTask = taskService.getPlayerTask(playerId);
		Int2Param result = new Int2Param();
		if(playerTask.getLiveness() < param.param){
			result.param1 = Response.ERR_PARAM;
			return result;
		}
		if(playerTask.getLiveBox().contains(param.param)){
			result.param1 = Response.ERR_PARAM;
			return result;
		}
		int[][] rewards = ConfigData.globalParam().taskLivenessReward.get(param.param);
		if(rewards == null){
			result.param1 = Response.ERR_PARAM;
			return result;
		}
		result.param2 = param.param;
		goodsService.addRewards(playerId, rewards, LogConsume.TASK_LIVENESS_REWARD, param.param);
		playerTask.getLiveBox().add(param.param);
		return result;
	}
	
	//有玩家接受了已方的任务
	public static final int ACCEPTED_JOINT = 1308;//
}
