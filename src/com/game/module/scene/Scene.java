package com.game.module.scene;

import com.game.data.SceneConfig;
import com.server.util.GameData;

public class Scene {
	
	public static final int CITY = 1;//主城
	public static final int COPY = 3;//单人副本
	public static final int MULTI = 4;//多人场景
	
	
	
	public static final int MULTI_CITY = 41;//多人场景
	public static final int MULTI_GANG = 42;//公会场景
	public static final int MULTI_TEAM = 43;//组队场景
	public static final int MULTI_PVE = 44;//组队PVE场景
	public static final int WORLD_BOSSS_PVE = 45;//世界BOSS PVE场景

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
		SceneConfig config = GameData.getConfig(SceneConfig.class, id);
		if(config.type == Scene.MULTI && config.sceneSubType != Scene.MULTI_CITY){
			return 1;
		}
		return subLine.genSunLine(config.type);
	}

}
