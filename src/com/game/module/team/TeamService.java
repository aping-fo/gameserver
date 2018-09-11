package com.game.module.team;

import com.game.data.CopyConfig;
import com.game.data.MonsterConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.copy.CopyExtension;
import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.Goods;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.IntParam;
import com.game.params.copy.CopyResult;
import com.game.params.scene.SMonsterVo;
import com.game.params.scene.SkillHurtVO;
import com.game.params.team.MyTeamVO;
import com.game.params.team.TeamMemberVO;
import com.game.params.worldboss.MonsterHurtVO;
import com.game.util.ConfigData;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TeamService implements InitHandler {

    public static int MAX_MEMBER = 3;

    @Autowired
    private PlayerService playerService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private TaskService taskService;

    private volatile int maxTeamId = 1000;
    private Map<Integer, Team> teams = new ConcurrentHashMap<>();

    @Override
    public void handleInit() {

    }

    public Team getTeam(int teamId) {
        return teams.get(teamId);
    }

    public List<Team> getAllTeam() {
        return new ArrayList<>(teams.values());
    }

    public void dissolve(Team team) {
        for (TMember member : team.getMembers().values()) {
            kick(team, member.getPlayerId());
        }
        teams.remove(team.getId());
    }

    public void quit(int playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player != null) {
            quit(playerId, player.getTeamId());
        }
    }

    public void quit(int playerId, int teamId) {
        if (teamId > 0) {
            Team team = teams.get(teamId);

            if (!team.isbRobot() && team.getMembers().size() > 1) {
                kick(team, playerId);
            } else {
                dissolve(team);
            }

            this.checkAndSendLost(team);
        }
        ServerLogger.info("...quit team:", playerId, teamId);
    }

    public void kick(Team team, int playerId) {
        if(playerId == Integer.MAX_VALUE)return;
        Player player = playerService.getPlayer(playerId);
        String key = sceneService.getGroupKey(player);
        SessionManager.getInstance().removeFromGroup(key, player.getPlayerId());
        player.setTeamId(0);
        team.getMembers().remove(playerId);
    }

    public Team createTeam(int playerId, int type, String name, int copyId) {
        int teamId = maxTeamId++;
        Team team = new Team(teamId, type, name, playerId);
        team.setCopyId(copyId);
        team.addMember(new TMember(playerId));
        teams.put(teamId, team);
        Player player = playerService.getPlayer(playerId);
        player.setTeamId(teamId);
        return team;
    }

    public int joinTeam(int playerId, int teamId) {
        Player player = playerService.getPlayer(playerId);
        if (player.getTeamId() > 0) {
            return Response.IN_TEAMING;
        }
        Team team = getTeam(teamId);
        if (team == null) {
            return Response.NO_TEAM;
        }
        if (team.isRunning()) {
            return Response.TEAM_RUNNING_NO_JOIN;
        }
        if (team.getMembers().size() >= ConfigData.globalParam().teamLimit.getOrDefault(team.getType(), 3)) {
            return Response.TEAM_FULL;
        }
        int type = team.getType();
        CopyConfig copyCfg = GameData.getConfig(CopyConfig.class,
                team.getCopyId());
        // 检查等级
        if (player.getLev() < copyCfg.lev) {
            return Response.NO_LEV;
        }
        // 次数
        if (copyCfg.count > 0) {
            Integer curCount = playerService.getPlayerData(playerId)
                    .getCopyTimes().get(copyCfg.id);
            if (curCount == null) {
                curCount = 0;
            }
            if (curCount >= copyCfg.count) {
                return Response.NO_TODAY_TIMES;
            }
        }
        if (copyCfg.needEnergy > 0) {
            if (type == Team.TYPE_TRAVERSING) {
                if (!playerService.verifyCurrency(playerId,
                        Goods.TRAVERSING_ENERGY, copyCfg.needEnergy)) {
                    return Response.NO_TRAVERSING_ENERGY;
                }

            } else if (player.getEnergy() < copyCfg.needEnergy) {
                return Response.NO_ENERGY;
            }
        }
        TMember member = new TMember(playerId);
        team.addMember(member);
        player.setTeamId(teamId);
        return Response.SUCCESS;
    }

    public MyTeamVO wrapTeam(Team team) {
        MyTeamVO vo = new MyTeamVO();
        vo.leader = team.getLeader();
        vo.member = new ArrayList<>();
        for (TMember member : team.getMembers().values()) {
            vo.member.add(wrapMember(member));
        }
        return vo;
    }

    public TeamMemberVO wrapMember(TMember member) {
        TeamMemberVO vo = new TeamMemberVO();
        vo.ready = member.isReady();
        vo.memberId = member.getPlayerId();
        Player player = playerService.getPlayer(member.getPlayerId());
        vo.fight = player.getFight();
        vo.lev = player.getLev();
        return vo;
    }

    private static final int CMD_MONSTER_INFO = 4910; //同步怪物相关信息

    // 玩家技能处理
    public void handleSkillHurt(Player player, SkillHurtVO hurtVO) {
        Team team = getTeam(player.getTeamId());
        int sceneId = player.getSceneId();
        CopyInstance copy = copyService.getCopyInstance(player.getCopyId());
        Map<Integer, SMonsterVo> monsters = copy.getMonsters().get(sceneId);
        if (hurtVO.targetType == 0) {
            TMember member = team.getMembers().get(hurtVO.targetId);
            if (hurtVO.subType == 1) {
                member.setTotalHp(member.getTotalHp() - hurtVO.hurtValue);
                if (hurtVO.hurtValue > 0) {
                    if (member.getCurHp() > member.getTotalHp()) {
                        member.setCurHp(member.getTotalHp());
                    }
                } else {
                    member.decHp(hurtVO.hurtValue);
                    if (member.getCurHp() > member.getTotalHp()) {
                        member.setCurHp(member.getTotalHp());
                    }
                }
            } else {
                member.decHp(hurtVO.hurtValue);
                if (member.getCurHp() > member.getTotalHp()) {
                    member.setCurHp(member.getTotalHp());
                }
            }

            if (member.getCurHp() <= 0) {
                this.checkAndSendLost(team);
            }
        } else {
            if (!player.checkHurt(hurtVO.hurtValue)) {
                SessionManager.getInstance().kick(player.getPlayerId());
                ServerLogger.warn("==================== 作弊玩家 Id = " + player.getPlayerId());
                return;
            }
            SMonsterVo monster = monsters.get(hurtVO.targetId);
            monster.curHp -= hurtVO.hurtValue;
            MonsterHurtVO ret = new MonsterHurtVO();
            ret.actorId = hurtVO.targetId;
            ret.curHp = monster.curHp;
            ret.hurt = hurtVO.hurtValue;
            ret.isCrit = hurtVO.isCrit;
            ret.type = 1;
            if (hurtVO.subType == 1) {
                ret.hurt = 0;
            }
            sceneService.brocastToSceneCurLine(player, CMD_MONSTER_INFO, ret, null);
            if (monster.curHp <= 0) {
                MonsterConfig monsterCfg = GameData.getConfig(MonsterConfig.class, monster.monsterId);
                Map<Integer, int[]> condParams = Maps.newHashMap();
                condParams.put(Task.FINISH_KILL, new int[]{monsterCfg.type, monster.monsterId, 1});
                condParams.put(Task.TYPE_KILL, new int[]{monsterCfg.type, 1});
                condParams.put(Task.TYPE_KILL, new int[]{0, 1});
                taskService.doTask(player.getPlayerId(), condParams);
                monsters.remove(hurtVO.targetId);
                if (copy.isOver()) {
                    //副本胜利
                    condParams.put(Task.TYPE_PASS_COPY_TEAM, new int[]{copy.getCopyId(), 1});
                    condParams.put(Task.TYPE_LEADER_PASS, new int[]{copy.getCopyId()});
                    taskService.doTask(team.getLeader(), condParams);
                    for (TMember tm : team.getMembers().values()) {
                        if(tm.getPlayerId() == Integer.MAX_VALUE) continue;
                        CopyResult result = new CopyResult();
                        copyService.getRewards(tm.getPlayerId(), copy.getCopyId(), result);
                        SessionManager.getInstance().sendMsg(CopyExtension.TAKE_COPY_REWARDS, result, tm.getPlayerId());
                        // 清除
                        copyService.removeCopy(tm.getPlayerId());
                    }
                    dissolve(team);
                }

            }
        }
    }

    /**
     * 进入战斗
     *
     * @param playerId
     * @return
     */
    public int onEnterBattle(int playerId) {
        Player player = playerService.getPlayer(playerId);
        Team team = teams.get(player.getTeamId());
        return team.getLeader() * 100;
    }

    public void updateAttr(int playerId) {
        Player player = playerService.getPlayer(playerId);
        Team team = getTeam(player.getTeamId());
        if (team == null) {
            return;
        }
        MyTeamVO vo = wrapTeam(team);
        sceneService.brocastToSceneCurLine(player, TeamExtension.MY_TEAM_INFO, vo, null);
    }

    private void checkAndSendLost(Team team) {
        boolean isLost = true;
        for (TMember tm : team.getMembers().values()) {
            if (tm.getCurHp() > 0) {
                isLost = false;
                break;
            }
        }
        if (isLost) {
            IntParam param = new IntParam();
            //副本失败
            for (TMember tm : team.getMembers().values()) {
                SessionManager.getInstance().sendMsg(CopyExtension.COPY_FAIL, param, tm.getPlayerId());
            }
        }
    }
}
