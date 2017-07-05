package com.game.module.scene;

import com.game.data.SceneConfig;
import com.server.util.GameData;

public class Scene {
	
	public static final int CITY = 1;//主城
	public static final int COPY = 3;//单人副本
	public static final int MULTI = 4;//多人场景
	
	
	
	
	public static final int MULTI_GANG = 42;//公会场景
	public static final int MULTI_TEAM = 43;//组队场景

	private int id;// 场景ID
	// ---其他数据
	private SubLine subLine = new SubLine();// 分线数据

	public void enterSubLine(int line) {
		subLine.enterSubLine(line);
	}

	public void exitSubLine(int line) {
		subLine.exitSubLine(line);
	}

	public int getId() {
		return id;
	}

	public SubLine getSubLine() {
		return subLine;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSubLine(SubLine subLine) {
		this.subLine = subLine;
	}
	
	public int getNewSubLine() {
		int type = GameData.getConfig(SceneConfig.class, id).type;
		return subLine.genSunLine(type);
	}

}
