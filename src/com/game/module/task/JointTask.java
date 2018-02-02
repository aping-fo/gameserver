package com.game.module.task;

public class JointTask extends Task {

	private int parnterId;

	public JointTask() {
		super();
	}

	public JointTask(int taskId, int partnerId,int type) {
		super(taskId, Task.STATE_ACCEPTED,type);
		this.parnterId = partnerId;
	}

	public int getParnterId() {
		return parnterId;
	}

	public void setParnterId(int parnterId) {
		this.parnterId = parnterId;
	}

}
