package com.game.module.group;

import com.game.data.Response;
import com.game.params.group.GroupTeamVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/9/4.
 */
public class GroupTeam {

    private int leader; //队长
    private final int id; //队伍编号
    private boolean bFight; //是否在战斗中
    private final Map<Integer, GroupTeamMember> members;

    public AtomicInteger fightSize = new AtomicInteger(0);

    public GroupTeam(int id) {
        this.id = id;
        members = new LinkedHashMap<>(); //成员列表
    }

    public boolean isbFight() {
        return bFight;
    }

    public void setbFight(boolean bFight) {
        this.bFight = bFight;
    }

    public int getLeader() {
        return leader;
    }


    public int getId() {
        return id;
    }

    public Map<Integer, GroupTeamMember> getMembers() {
        return members;
    }

    public boolean isFull() {
        return members.size() == 2;
    }

    public int size() {
        return members.size();
    }

    public void decHp(int playerId, int hp) {
        GroupTeamMember member = members.get(playerId);
        member.setHp(member.getHp() - hp);
    }

    public boolean checkDeath() {
        for (GroupTeamMember member : members.values()) {
            if (member.getHp() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean checkCostTimes(int playerId) {
        GroupTeamMember member = members.get(playerId);
        return member.isCostTimesFlag();
    }

    public void setCostTimes(int playerId) {
        GroupTeamMember member = members.get(playerId);
        member.setCostTimesFlag(true);
    }

    /**
     * 更换队长
     *
     * @param leader
     * @return
     */
    public int changeLeader(int leader) {
        if (bFight) {
            return Response.TEAM_FIGHT;
        }
        if (this.leader != leader) {
            GroupTeamMember member = members.get(this.leader);
            if (member != null) {
                member.setReadyFlag(false);
            }
        }
        GroupTeamMember member = members.get(leader);
        if (member != null) {
            this.leader = leader;
            member.setReadyFlag(true);
            return Response.SUCCESS;
        }
        return Response.ERR_PARAM;
    }

    /**
     * 新增队员
     *
     * @param member
     */
    public void addMember(GroupTeamMember member) {
        if (members.isEmpty()) {
            leader = member.getPlayerId();
            member.setReadyFlag(true);
        }
        members.put(member.getPlayerId(), member);
    }

    public void memberReady(int playerId, boolean bReady) {
        GroupTeamMember member = members.get(playerId);
        member.setReadyFlag(bReady);
    }

    /**
     * 是否都准备
     *
     * @return
     */
    public boolean isReady() {
        for (Map.Entry<Integer, GroupTeamMember> s : members.entrySet()) {
            if (!s.getValue().isReadyFlag()) {
                return false;
            }
        }
        return true;
    }

    public GroupTeamMember remove(int playerId) {
        GroupTeamMember member = members.remove(playerId);
        if (playerId == leader) { //如果移除的这个人是队长，则更换队长
            leader = 0;
            if (members.size() == 1) {
                GroupTeamMember other = members.values().iterator().next();
                other.setReadyFlag(true);
                leader = other.getPlayerId();
            }
        }
        return member;
    }

    public void clear() {
        members.clear();
    }

    /**
     * 协议
     */
    public GroupTeamVO toProto() {
        GroupTeamVO vo = new GroupTeamVO();
        vo.id = id;
        vo.fightFlag = bFight;
        vo.leaderId = leader;
        vo.members = new ArrayList<>();
        for (GroupTeamMember member : members.values()) {
            vo.members.add(member.toProto());
        }
        return vo;
    }
}
