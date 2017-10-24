package com.game.module.scene;

import com.game.data.SceneConfig;
import com.server.util.GameData;

public class Scene {
	
	public static final int CITY = 1;//主城
	public static final int COPY = 3;//单人副本
	public static final int MULTI = 4;//多人场景
	
	
	
	public static final int MULTI_MAIN_CITY = 40;//主城
	public static final int MULTI_CITY = 41;//多人场景
	public static final int MULTI_GANG = 42;//公会场景
	public static final int MULTI_TEAM = 43;//组队场景
	public static final int MULTI_PVE = 44;//组队PVE场景
	public static final int WORLD_BOSS_PVE = 45;//世界BOSS PVE场景
	public static final int MULTI_GROUP_ROOM = 50;//团队副本房间
	public static final int MULTI_GROUP = 51;//团队副本
	public static final int MULTI_LADDER = 61;//排位赛
	public static final int MULTI_GANG_BOSS = 71;//公会BOSS

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
		/*if(config.type == Scene.MULTI &&
				(config.sceneSubType != Scene.MULTI_CITY && config.sceneSubType != Scene.MULTI_MAIN_CITY)){
			return 1;
		}*/
		//无需做人数控制
		if(config.sceneSubType == Scene.MULTI_TEAM || config.sceneSubType == Scene.MULTI_PVE
				|| config.sceneSubType == Scene.WORLD_BOSS_PVE || config.sceneSubType == Scene.MULTI_GROUP
				|| config.sceneSubType == Scene.MULTI_GROUP_ROOM) {
			return 1;
		}
		return subLine.genSunLine(config.type);
	}

}
