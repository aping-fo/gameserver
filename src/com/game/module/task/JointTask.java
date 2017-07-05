package com.game.module.task;

public class JointTask extends Task {

	private int parnterId;

	public JointTask() {
		super();
	}

	public JointTask(int taskId, int partnerId) {
		super(taskId, Task.STATE_ACCEPTED);
		this.parnterId = partnerId;
	}

	public int getParnterId() {
		return parnterId;
	}

	public void setParnterId(int parnterId) {
		this.parnterId = parnterId;
	}

}
