package com.game.module.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTask {
	
	//所以任务
	private Map<Integer, Task> tasks = new ConcurrentHashMap<Integer, Task>();

	//自已的合作任务[任务ID:被邀请人ID]
	private Map<Integer, Integer> myJointTasks = new ConcurrentHashMap<Integer, Integer>();
	//完成被邀请任务次数
	private int jointedCount = 0;
	//当前被邀请任务
	private JointTask currJointedTask;
	//被邀请的任务列表[任务ID:邀请人ID]
	private List<String> jointedTasks = new ArrayList<String>();
	//活跃度
	private int liveness;
	//活跃度已领奖励
	private Set<Integer> liveBox = new HashSet<Integer>();
	
	public Map<Integer, Task> getTasks() {
		return tasks;
	}

	public void setTasks(Map<Integer, Task> tasks) {
		this.tasks = tasks;
	}

	public Map<Integer, Integer> getMyJointTasks() {
		return myJointTasks;
	}

	public void setMyJointTasks(Map<Integer, Integer> myJointTasks) {
		this.myJointTasks = myJointTasks;
	}

	public int getJointedCount() {
		return jointedCount;
	}

	public void setJointedCount(int jointedCount) {
		this.jointedCount = jointedCount;
	}
	
	public void alterJointedCount(int value){
		this.jointedCount += value;
	}

	public JointTask getCurrJointedTask() {
		return currJointedTask;
	}

	public void setCurrJointedTask(JointTask currJointedTask) {
		this.currJointedTask = currJointedTask;
	}

	public List<String> getJointedTasks() {
		return jointedTasks;
	}

	public void setJointedTasks(List<String> jointedTasks) {
		this.jointedTasks = jointedTasks;
	}

	public int getLiveness() {
		return liveness;
	}

	public void setLiveness(int liveness) {
		this.liveness = liveness;
	}
	
	public void alterLiveness(int value){
		this.liveness += value;
	}
	

	public Set<Integer> getLiveBox() {
		return liveBox;
	}

	public void setLiveBox(Set<Integer> liveBox) {
		this.liveBox = liveBox;
	}

}
