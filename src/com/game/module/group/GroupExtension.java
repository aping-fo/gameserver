package com.game.module.group;

import com.game.params.BoolParam;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lucky on 2017/9/4.
 */
@Extension
public class GroupExtension {
    @Autowired
    private GroupService groupService;

    @Command(5001)
    public Object getGroupList(int playerId, Object param) {
        return groupService.getGroupList();
    }

    @Command(5002)
    public Object createGroup(int playerId, Int2Param param) {
        return groupService.createGroup(playerId, param.param1,param.param2);
    }

    @Command(5003)
    public Object dismissGroup(int playerId, Object param) {
        groupService.dismissGroup(playerId);
        return null;
    }

    @Command(5004)
    public Object leaderChangeMember(int playerId, Int2Param param) {
        return groupService.leaderChangeMember(playerId, param.param1, param.param2);
    }

    @Command(5005)
    public Object changeTeamLeader(int playerId, IntParam param) {
        return groupService.changeTeamLeader(playerId, param.param);
    }

    @Command(5006)
    public Object invite(int playerId, IntParam param) {
        return groupService.invite(playerId, param.param);
    }

    @Command(5007)
    public Object inviteLinked(int playerId, Object param) {
        groupService.inviteLinked(playerId);
        return null;
    }

    @Command(5008)
    public Object groupSet(int playerId, BoolParam param) {
        return groupService.groupSet(playerId, param.param);
    }

    @Command(5009)
    public Object memberExit(int playerId, Object param) {
        return groupService.memberExit(playerId);
    }

    @Command(5010)
    public Object joinGroup(int playerId, IntParam param) {
        return groupService.joinGroup(playerId, param.param);
    }

    @Command(5011)
    public Object kickMember(int playerId, IntParam param) {
        return groupService.kickMember(playerId, param.param);
    }

    @Command(5012)
    public Object selfChangeTeam(int playerId, IntParam param) {
        return groupService.selfChangeTeam(playerId, param.param);
    }

    @Command(5013)
    public Object memberReady(int playerId, BoolParam param) {
        return groupService.memberReady(playerId, param.param);
    }

    @Command(5014)
    public Object getSelfGroupInfo(int playerId, Object param) {
        return groupService.getSelfGroupInfo(playerId);
    }

    @Command(5015)
    public Object startChallenge(int playerId, IntParam param) {
        return groupService.startChallenge(playerId,param.param);
    }

    @Command(5016)
    public Object getGroupInfo(int playerId, Object param) {
        return groupService.getGroupInfo(playerId);
    }

    @Command(5017)
    public Object stageEnter(int playerId, Object param) {
        return groupService.stageEnter(playerId);
    }

    @Command(5018)
    public Object getGroupStageInfo(int playerId, Object param) {
        return groupService.getGroupStageInfo(playerId);
    }
}
