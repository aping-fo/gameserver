package com.game.module.group;

import com.game.params.group.GroupTeamMemberVO;

/**
 * Created by lucky on 2017/9/4.
 */
public class GroupTeamMember {
    private int playerId;
    private boolean readyFlag;
    private int hp;
    private boolean costTimesFlag;

    public GroupTeamMember(int playerId, int hp) {
        this.playerId = playerId;
        this.hp = hp;
        this.costTimesFlag = false;
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
        return vo;
    }
}
