package com.game.data;

/**
* r任务配置表.xlsx(自动生成，请勿编辑！)
*/
public class TaskConfig {
	public int id;//id
	public int group;//任务组
	public int taskType;//任务类型
	public String taskName;//任务名称
	public int level;//触发等级
	public int finishType;//完成方式
	public int[] finishParam;//完成条件
	public int[][] rewards;//奖励
	public int liveness;//活跃度奖励
	public int nextTaskId;//后续任务id
}