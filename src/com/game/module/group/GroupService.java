package com.game.module.group;

import com.game.data.GroupCopyCfg;
import com.game.data.Response;
import com.game.module.admin.MessageService;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.RewardList;
import com.game.params.copy.CopyResult;
import com.game.params.group.GroupTeamVO;
import com.game.params.group.GroupVO;
import com.game.params.group.SelfGroupCopyVO;
import com.game.params.scene.SMonsterVo;
import com.game.params.scene.SkillHurtVO;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.server.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/9/4.
 */
@Service
public class GroupService {

    //团队信息推送协议号
    private final static int CMD_GROUP_INFO = 5100;
    //解散推送
    private final static int CMD_DISMISS = 5101;
    //成员准备
    private final static int CMD_MEMBE_READY = 5102;
    //阶段奖励
    private final static int CMD_STAGE_AWARD = 5103;
    //ID生成
    private final AtomicInteger IdGen = new AtomicInteger(100);
    /**
     * 团队列表
     */
    private final Map<Integer, Group> groupMap = new ConcurrentHashMap<>();

    @Autowired
    private PlayerService playerService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private GoodsService goodsService;

    /**
     * 获取团队列表
     */
    public ListParam getGroupList() {
        ListParam<GroupVO> result = new ListParam();
        result.params = new ArrayList();
        for (Group group : groupMap.values()) {
            if (group.isOpenFlag()) {
                result.params.add(group.toProto());
            }
        }
        return result;
    }

    /**
     * 创建团队
     *
     * @param playerId
     * @param level
     */
    public IntParam createGroup(int playerId, int level, int groupCopyId) {
        IntParam param = new IntParam();
        /*if (!checkOpen()) {
            param.param = Response.TEAM_TIME_OVER;
            return param;
        }*/

        Player player = playerService.getPlayer(playerId);
        if (player.getLev() < level) {
            param.param = Response.TEAM_NO_LEVEL_QI;
            return param;
        }
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getGroupTimes() < 1) {
            param.param = Response.TEAM_NO_NUM;
            return param;
        }
        int groupId = IdGen.getAndDecrement();
        Group group = new Group(groupId, playerId, level, groupCopyId);
        int groupTeamId = group.addTeamMember(playerId, player.getHp());
        groupMap.put(groupId, group);
        player.setGroupId(groupId);
        player.setGroupTeamId(groupTeamId);

        GroupCopyCfg groupCopyCfg = ConfigData.getConfig(GroupCopyCfg.class, groupCopyId);

        switch (groupCopyCfg.stageCount) {
            case 3: {
                Map<Integer, GroupTask> map = new HashMap<>();
                for (int[] m : groupCopyCfg.stage3Mission) {
                    map.put(m[1], new GroupTask(m[0], m[2], m[1]));
                }
                group.getTasks().put(3, map);
            }
            case 2: {
                Map<Integer, GroupTask> map = new HashMap<>();
                for (int[] m : groupCopyCfg.stage2Mission) {
                    map.put(m[1], new GroupTask(m[0], m[2], m[1]));
                }
                group.getTasks().put(2, map);
            }
            case 1: {
                Map<Integer, GroupTask> map = new HashMap<>();
                for (int[] m : groupCopyCfg.stage1Mission) {
                    map.put(m[1], new GroupTask(m[0], m[2], m[1]));
                }
                group.getTasks().put(1, map);
            }
        }

        broadcastGroup(group);

        param.param = Response.SUCCESS;
        return param;
    }


    /**
     * 检测活动是否开启
     *
     * @return
     */
    public boolean checkOpen() {
        return TimeUtil.checkTimeIn(ConfigData.globalParam().groupCopyOpenTime);
    }

    /**
     * 解散团队
     *
     * @param playerId
     */
    public void dismissGroup(int playerId) {
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            return;
        }

        if (group.getLeader() != playerId) {
            return;
        }
        broadcastGroupDismiss(group);
        group.clear();
    }

    /**
     * 团队信息广播
     * 解散
     *
     * @param group
     */
    private void broadcastGroupDismiss(Group group) {
        IntParam param = new IntParam();
        param.param = group.getId();
        Map<Integer, GroupTeam> teamMap = group.getTeamMap();
        for (GroupTeam team : teamMap.values()) {
            for (int playerId : team.getMembers().keySet()) {
                SessionManager.getInstance().sendMsg(CMD_DISMISS, param, playerId);
            }
        }
    }


    /**
     * 团长调整队伍
     *
     * @param playerId
     * @param toTeamId
     * @param targetId
     */
    public IntParam leaderChangeMember(int playerId, int toTeamId, int targetId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.getLeader() != playerId) {
            param.param = Response.GROUP_LIMIT;
            return param;
        }

        Player targetPlayer = playerService.getPlayer(targetId);
        if (targetPlayer.getGroupTeamId() == toTeamId) { //队伍没调整
            param.param = Response.ERR_PARAM;
            return param;
        }
        int ret = group.changeTeam(targetPlayer.getGroupTeamId(), toTeamId, targetId);
        if (ret == Response.SUCCESS) {//更新数据
            targetPlayer.setGroupTeamId(toTeamId);
        }
        param.param = ret;
        return param;
    }

    /**
     * 任命队长
     *
     * @param playerId
     * @param teamLeader
     */
    public IntParam changeTeamLeader(int playerId, int teamLeader) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.getLeader() != playerId) {
            param.param = Response.GROUP_LIMIT;
            return param;
        }

        Player targetPlayer = playerService.getPlayer(teamLeader);
        int ret = group.changeTeamLeader(targetPlayer.getTeamId(), teamLeader);
        param.param = ret;
        return param;
    }

    /**
     * 团队信息广播
     * 成员加入，离开，信息变更?
     *
     * @param group
     */
    private void broadcastGroup(Group group) {
        Map<Integer, GroupTeam> teamMap = group.getTeamMap();
        GroupVO vo = group.toProto();
        for (GroupTeam team : teamMap.values()) {
            for (int playerId : team.getMembers().keySet()) {
                SessionManager.getInstance().sendMsg(CMD_GROUP_INFO, vo, playerId);
            }
        }
    }

    /**
     * 团队信息广播
     * 成员加入，离开，信息变更?
     *
     * @param group
     */
    private void broadcastGroup(Group group, RewardList list, List<GoodsEntry> items) {
        Map<Integer, GroupTeam> teamMap = group.getTeamMap();
        for (GroupTeam team : teamMap.values()) {
            for (int playerId : team.getMembers().keySet()) {
                SessionManager.getInstance().sendMsg(CMD_STAGE_AWARD, list, playerId);
                goodsService.addRewards(playerId, items, LogConsume.COPY_REWARD, group.groupCopyId);

                //扣除次数，只扣一次
                if (!team.checkCostTimes(playerId)) {
                    team.setCostTimes(playerId);
                    PlayerData data = playerService.getPlayerData(playerId);
                    data.setLadderTimes(data.getLadderTimes() - 1);
                }
            }
        }
    }

    /**
     * 队伍信息广播
     * 成员准备-取消准备
     *
     * @param groupTeam
     */
    private void broadcastGroupTeam(GroupTeam groupTeam) {
        GroupTeamVO vo = groupTeam.toProto();
        for (int playerId : groupTeam.getMembers().keySet()) {
            SessionManager.getInstance().sendMsg(CMD_MEMBE_READY, vo, playerId);
        }
    }

    /**
     * 邀请入团
     *
     * @param playerId
     * @param targetId
     */
    public IntParam invite(int playerId, int targetId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.getLeader() != playerId) {
            param.param = Response.GROUP_LIMIT;
            return param;
        }

        if (group.isFull()) {
            param.param = Response.TEAM_FULL;
            return param;
        }

        Player targetPlayer = playerService.getPlayer(targetId);
        int groupTeamId = group.addTeamMember(targetId, targetPlayer.getHp());
        targetPlayer.setGroupTeamId(groupTeamId);

        broadcastGroup(group);
        param.param = Response.SUCCESS;
        return param;
    }


    public void inviteLinked(int playerId) {
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            return;
        }

        //messageService.sendSysMsg()

    }

    /**
     * 团队设置
     *
     * @param playerId
     * @param bOpen
     */
    public IntParam groupSet(int playerId, boolean bOpen) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.getLeader() != playerId) {
            param.param = Response.GROUP_LIMIT;
            return param;
        }
        group.setOpenFlag(bOpen);
        broadcastGroup(group);

        param.param = Response.SUCCESS;
        return param;
    }


    /**
     * 成员退出
     *
     * @param playerId
     */
    public IntParam memberExit(int playerId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.size() == 1) { //如果本来只有一个，解散掉
            group.clear();
            groupMap.remove(group.getId());

            param.param = Response.SUCCESS;
            return param;
        }

        group.memberExit(player.getGroupTeamId(), playerId);
        player.setGroupId(0);
        player.setGroupTeamId(0);
        broadcastGroup(group);

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 加入团队
     *
     * @param playerId
     * @param groupId
     */
    public IntParam joinGroup(int playerId, int groupId) {
        IntParam param = new IntParam();
        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getGroupTimes() < 1) {
            param.param = Response.TEAM_NO_NUM;
            return param;
        }
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(groupId);
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.isOpenFlag()) {
            param.param = Response.TEAM_NO_OPEN;
            return param;
        }

        int groupTeamId = group.addTeamMember(playerId, player.getHp());
        if (groupTeamId == 0) {
            param.param = Response.TEAM_PEOPLE_REACHED;
            return param;
        }

        player.setGroupTeamId(groupTeamId);
        player.setGroupId(groupId);

        broadcastGroup(group);

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 踢出成员
     *
     * @param playerId
     * @param targetId
     */
    public IntParam kickMember(int playerId, int targetId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        if (group.getLeader() != playerId) {
            param.param = Response.GROUP_LIMIT;
            return param;
        }

        Player targetPlayer = playerService.getPlayer(targetId);
        group.memberExit(targetPlayer.getGroupTeamId(), targetId);
        targetPlayer.setGroupTeamId(0);
        targetPlayer.setGroupId(0);

        broadcastGroup(group);

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 自己调整队伍
     *
     * @param playerId
     * @param toTeamId
     */
    public IntParam selfChangeTeam(int playerId, int toTeamId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        int ret = group.changeTeam(player.getGroupTeamId(), toTeamId, playerId);
        if (ret == Response.SUCCESS) {//更新数据
            player.setGroupTeamId(toTeamId);
            broadcastGroup(group);
        }
        param.param = ret;
        return param;
    }

    /**
     * 成员准备 or 取消准备
     *
     * @param playerId
     * @param bReady
     */
    public IntParam memberReady(int playerId, boolean bReady) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }
        GroupTeam groupTeam = group.getGroupTeam(player.getGroupTeamId());
        groupTeam.memberReady(playerId, bReady);
        broadcastGroupTeam(groupTeam);

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 获取相关信息
     *
     * @param playerId
     * @return
     */
    public SelfGroupCopyVO getSelfGroupInfo(int playerId) {
        SelfGroupCopyVO vo = new SelfGroupCopyVO();
        PlayerData data = playerService.getPlayerData(playerId);
        vo.times = data.getGroupTimes();
        return vo;
    }

    /**
     * 开始挑战
     *
     * @param playerId
     * @return
     */
    public IntParam startChallenge(int playerId) {
        IntParam param = new IntParam();
        Player player = playerService.getPlayer(playerId);
        Group group = groupMap.get(player.getGroupId());
        if (group == null) {
            param.param = Response.GROUP_NO_EXIT;
            return param;
        }

        GroupTeam team = group.getGroupTeam(player.getGroupTeamId());
        if (team.getLeader() != playerId) {
            param.param = Response.TEME_RBAC;
            return param;
        }

        for (int id : team.getMembers().keySet()) {
            PlayerData data = playerService.getPlayerData(id);
            if (data.getGroupTimes() < 1) {
                param.param = Response.TEAM_NO_NUM_FALSE;
                return param;
            }
        }

        if (!team.isReady()) { //是否准备
            param.param = Response.TEAM_NO_OK;
            return param;
        }

        //TODO 判断 当前副本已经通关，无法进入该副本。

        //TODO 判断 当前副本所属的阶段未开启，无法进入该副本。

        //TODO 判断 需要通过当前阶段，才可进入下一阶段。
        param.param = Response.SUCCESS;
        return param;
    }

    // 玩家技能处理
    public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
        int sceneId = player.getSceneId();
        CopyInstance copy = copyService.getCopyInstance(player.getCopyId());
        Map<Integer, SMonsterVo> monsters = copy.getMonsters().get(sceneId);

        Group group = groupMap.get(player.getGroupId());
        GroupTeam team = group.getGroupTeam(player.getGroupTeamId());
        if (hurtVO.targetType == 0) {
            team.decHp(hurtVO.targetId, hurtVO.hurtValue);
            if (team.checkDeath()) {
                //副本失败
                sceneService.brocastToSceneCurLine(player, CopyExtension.COPY_FAIL, null);
            }
        } else {
            SMonsterVo monster = monsters.get(hurtVO.targetId);
            monster.curHp -= hurtVO.hurtValue;
            if (monster.curHp <= 0) {
                monsters.remove(hurtVO.targetId);
                if (copy.isOver()) {
                    //副本胜利
                    CopyResult result = new CopyResult();
                    GroupCopyCfg cfg = ConfigData.getConfig(GroupCopyCfg.class, group.groupCopyId);

                    group.completeTask(group.stage, GroupTaskType.PASS_COUNT, 1);
                    group.completeTask(group.stage, GroupTaskType.PASS_COUNT, 1);

                    copyService.getRewards(player.getPlayerId(), copy.getCopyId(), result);
                    // 更新次数,星级
                    copyService.updateCopy(player.getPlayerId(), copy, result);

                    sceneService.brocastToSceneCurLine(player, CopyExtension.TAKE_COPY_REWARDS, result, null);
                    for (int playerId : team.getMembers().keySet()) {
                        // 清除
                        copyService.removeCopy(playerId);
                    }

                    if (group.checkComplete(group.stage)) {
                        RewardList list = new RewardList();
                        list.rewards = new ArrayList<>();
                        List<GoodsEntry> items = new ArrayList<>();

                        int[][] stageReward = null;
                        if (group.stage == 1) {
                            stageReward = cfg.stage1Reward;
                        }
                        if (group.stage == 2) {
                            stageReward = cfg.stage2Reward;
                        }
                        if (group.stage == 3) {
                            stageReward = cfg.stage3Reward;

                        }
                        if (stageReward == null) {
                            return;
                        }
                        int[] arrRate = ConfigData.globalParam().groupRewardRate;
                        int rate = 0;
                        int pass = (int) ((System.currentTimeMillis() - copy.getCreateTime()) / 1000);
                        for (int i = 0; i < arrRate.length; i += 2) {
                            int time = arrRate[i];
                            int rateTmp = arrRate[i + 1];
                            if (pass <= time) {
                                rate = rateTmp;
                            }
                        }
                        for (int[] arr : stageReward) {
                            Reward reward = new Reward();
                            reward.id = arr[0];
                            reward.count = Math.round(arr[1] * (1 + rate / 100f));
                            list.rewards.add(reward);
                            items.add(new GoodsEntry(arr[0], arr[1]));
                        }
                        if (group.stage < 3) {
                            group.stage += 1;
                            broadcastGroup(group);
                            broadcastGroup(group, list, items);
                        }
                    }
                }
            }
        }
    }
}
