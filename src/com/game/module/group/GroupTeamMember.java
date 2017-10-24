package com.game.module.group;

import com.game.params.group.GroupTeamMemberVO;

/**
 * Created by lucky on 2017/9/4.
 */
public class GroupTeamMember {
    private int playerId;
    private boolean readyFlag;
    private int hp;
    private int vocation;
    private int fight;
    private int lev;
    private String name;
    private boolean costTimesFlag;

    public GroupTeamMember(int playerId, int hp, int vocation, int fight, int lev, String name) {
        this.playerId = playerId;
        this.hp = hp;
        this.vocation = vocation;
        this.fight = fight;
        this.lev = lev;
        this.name = name;
        this.costTimesFlag = false;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public int getLev() {
        return lev;
    }

    public void setLev(int lev) {
        this.lev = lev;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public boolean isReadyFlag() {
        return readyFlag;
    }

    public void setReadyFlag(boolean readyFlag) {
        this.readyFlag = readyFlag;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isCostTimesFlag() {
        return costTimesFlag;
    }

    public void setCostTimesFlag(boolean costTimesFlag) {
        this.costTimesFlag = costTimesFlag;
    }

    public GroupTeamMemberVO toProto() {
        GroupTeamMemberVO vo = new GroupTeamMemberVO();
        vo.playerId = playerId;
        vo.readyFlag = readyFlag;
        vo.name = name;
        vo.fight = fight;
        vo.vocation = vocation;
        vo.lev = lev;
        return vo;
    }
}
