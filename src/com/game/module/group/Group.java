package com.game.module.group;

import com.game.data.Response;
import com.game.params.group.GroupVO;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lucky on 2017/9/4.
 */
public class Group {

    private final int id; //团队ID
    private final int level; //等级
    private int leader; //团长
    private boolean openFlag; //是否开放
    private final ReentrantLock lock = new ReentrantLock();
    //队伍列表
    private Map<Integer, GroupTeam> teamMap = new LinkedHashMap<>(); //组队列表

    public volatile int stage = 1; //第几阶段
    public final int groupCopyId;
    private Map<Integer, Map<Integer, GroupTask>> tasks = new HashMap<>();

    public Group(int id, int leader, int level, int groupCopyId) {
        this.id = id;
        this.leader = leader;
        this.level = level;
        this.openFlag = true;
        this.groupCopyId = groupCopyId;

        for (int i = 1; i <= 6; i++) {
            GroupTeam team = new GroupTeam(i);
            teamMap.put(i, team);
        }
    }

    public Map<Integer, Map<Integer, GroupTask>> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Integer, Map<Integer, GroupTask>> tasks) {
        this.tasks = tasks;
    }

    public boolean isOpenFlag() {
        return openFlag;
    }

    public void setOpenFlag(boolean openFlag) {
        this.openFlag = openFlag;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getLeader() {
        return leader;
    }

    public Map<Integer, GroupTeam> getTeamMap() {
        return teamMap;
    }

    /**
     * 获取队伍信息
     *
     * @param teamId
     * @return
     */
    public GroupTeam getGroupTeam(int teamId) {
        return teamMap.get(teamId);
    }

    /**
     * 更换团长
     *
     * @return
     */
    public void changeGroupLeader() {
        for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
            GroupTeam team = s.getValue();
            for (int playerId : team.getMembers().keySet()) {
                if (playerId != leader) {
                    this.leader = playerId;
                }
            }
        }
    }

    /**
     * 成员退出
     *
     * @param teamId
     * @param playerId
     * @return
     */
    public int memberExit(int teamId, int playerId) {
        try {
            lock.lock();
            GroupTeam team = teamMap.get(teamId);
            team.remove(playerId);
            if (leader == playerId) {
                for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
                    for (int id : s.getValue().getMembers().keySet()) {
                        this.leader = id;
                        return id;
                    }
                }
            }
            return 0;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        try {
            lock.lock();
            int size = 0;
            for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
                size += s.getValue().size();
            }
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更换队长
     *
     * @param teamId
     * @param leander
     * @return
     */
    public int changeTeamLeader(int teamId, int leander) {
        try {
            lock.lock();
            GroupTeam groupTeam = teamMap.get(teamId);
            if (groupTeam == null) {
                return Response.TEAM_NO_EXIT;
            }
            return groupTeam.changeLeader(leander);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 调整队伍
     *
     * @param targetId
     * @return
     */
    public int changeTeam(int fromTeamId, int toTeamId, int targetId) {
        try {
            lock.lock();
            GroupTeam fromTeam = teamMap.get(fromTeamId);
            if (fromTeam == null) {
                return Response.GROUP_NO_EXIT;
            }
            if (fromTeam.isbFight()) {
                return Response.TEAM_FIGHT;
            }
            GroupTeam toTeam = teamMap.get(toTeamId);
            if (toTeam == null) {
                return Response.GROUP_NO_EXIT;
            }
            if (toTeam.isbFight()) {
                return Response.TEAM_FIGHT;
            }
            if (toTeam.isFull()) {
                return Response.GROUP_FULL;
            }
            GroupTeamMember member = fromTeam.remove(targetId);
            toTeam.addMember(member);
            return Response.SUCCESS;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        try {
            lock.lock();
            for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
                if (!s.getValue().isFull()) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int addTeamMember(int playerId, int hp) {
        try {
            lock.lock();
            for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
                if (!s.getValue().isFull()) {
                    s.getValue().addMember(new GroupTeamMember(playerId, hp));
                    return s.getKey();
                }
            }
            return 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清理数据
     */
    public void clear() {
        for (Map.Entry<Integer, GroupTeam> s : teamMap.entrySet()) {
            s.getValue().clear();
        }
        teamMap.clear();
    }

    public GroupVO toProto() {
        GroupVO vo = new GroupVO();
        vo.id = id;
        vo.leaderId = leader;
        vo.level = level;
        vo.teams = new ArrayList<>();
        vo.tasks = new ArrayList<>();
        vo.stage = stage;
        try {
            lock.lock();
            for (GroupTeam team : teamMap.values()) {
                vo.teams.add(team.toProto());
            }

            Map<Integer, GroupTask> map = tasks.get(stage);
            for (GroupTask task : map.values()) {
                vo.tasks.add(task.toProto());
            }
            return vo;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 完成条件
     *
     * @param stage
     * @param target
     * @param count
     * @return
     */
    public void completeTask(int stage, int target, int count) {
        try {
            lock.lock();
            GroupTask task = tasks.get(stage).get(target);
            int value = task.getValue() + count > task.getCount() ? task.getCount() : task.getValue() + count;
            task.setValue(value);
        } finally {
            lock.unlock();
        }
    }

    public boolean checkComplete(int stage) {
        try {
            lock.lock();
            Map<Integer, GroupTask> map = tasks.get(stage);
            for (GroupTask task : map.values()) {
                if (task.getValue() < task.getCount()) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
}
