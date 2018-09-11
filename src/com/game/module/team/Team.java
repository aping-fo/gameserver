package com.game.module.team;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.module.copy.CopyInstance;


public class Team {
	public static final int TYPE_TRAVERSING = 1;//穿越仪
	
	
	private int id;
	private int type;
	private String name;
	private int leader;
	private Map<Integer, TMember> members = new ConcurrentHashMap<Integer, TMember>();
	private boolean running;// 是否在副本中
	private int mapId;//穿越仪地图ID
	private int copyId;
	private CopyInstance copyIns;
	private boolean open = true;// 是否对外开放

	public Team() {
	}

	public Team(int id, int type, String name, int leader) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.leader = leader;
	}

	public int getId() {
		return id;
	}

	/**
	 * 是否机器人参战
	 * TODO，指定ID，有robot标记更清晰
	 * @return
	 */
	public boolean isbRobot() {
		for(TMember m : members.values()) {
			if(m.getPlayerId() == Integer.MAX_VALUE){ //
				return true;
			}
		}
		return false;
	}



	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLeader() {
		return leader;
	}

	public void setLeader(int leader) {
		this.leader = leader;
	}

	public Map<Integer, TMember> getMembers() {
		return members;
	}

	public void setMembers(Map<Integer, TMember> members) {
		this.members = members;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getCopyId() {
		return copyId;
	}

	public void setCopyId(int copyId) {
		this.copyId = copyId;
	}

	public CopyInstance getCopyIns() {
		return copyIns;
	}

	public void setCopyIns(CopyInstance copyIns) {
		this.copyIns = copyIns;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public void addMember(TMember member){
		members.putIfAbsent(member.getPlayerId(), member);
	}
}
