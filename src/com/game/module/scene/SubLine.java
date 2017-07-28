package com.game.module.scene;

import java.util.concurrent.atomic.AtomicIntegerArray;

import com.game.SysConfig;
import com.game.util.RandomUtil;

public class SubLine {

	private AtomicIntegerArray subLineUsers;

	public SubLine() {
		subLineUsers = new AtomicIntegerArray(SysConfig.subLineCount + 1);
	}

	// 进入分线
	public void enterSubLine(int subLine) {
		subLineUsers.incrementAndGet(subLine);
	}

	// 离开分线
	public void exitSubLine(int subLine) {
		if (subLine > 0)
			subLineUsers.decrementAndGet(subLine);
	}

	// 生成一个可用的分线

	/**
	 * 这个地方估计还的改成type来获取，不然这里是有问题的
	 * @param type
	 * @return
	 */
	public int genSunLine(int type) {
		int subLine = 0;
		int max = SysConfig.subLineCap;

		for (int i = 2; i < subLineUsers.length(); i++) {
			if (subLineUsers.get(i) < max) {
				subLine = i;
				break;
			}
		}
		if (subLine == 0) {// 全满了，随机一个分线
			subLine = RandomUtil.randInt(SysConfig.subLineCount - 1) + 2;
		}
		return subLine;
	}

}
