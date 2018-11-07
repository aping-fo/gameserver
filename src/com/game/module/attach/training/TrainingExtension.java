package com.game.module.attach.training;

import java.util.ArrayList;
import java.util.List;

import com.game.module.copy.CopyInstance;
import com.game.module.copy.CopyService;
import com.game.module.goods.EquipService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.Reward;
import com.game.params.ladder.TrainingResultVO;
import com.game.params.training.TrainingFighterVO;
import com.game.util.JsonUtils;
import com.google.common.collect.Lists;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;

import com.game.data.Response;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.log.LogConsume;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.training.TrainOpponentVO;
import com.game.params.training.TrainingRewardVO;
import com.game.params.training.TrainingVO;
import com.game.util.ConfigData;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class TrainingExtension {

    @Autowired
    private trainingLogic logic;
    @Autowired
    private RandomRewardService rewardService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private CopyService copyService;
    @Autowired
    private GoodsService goodsService;

    //获取相关信息
    @Command(3901)
    public TrainingVO getInfo(int playerId, Object param) {
        TrainAttach attach = logic.getAttach(playerId);
        TrainingVO vo = new TrainingVO();
        vo.index = attach.getIndex();
        vo.hp = attach.getHp();
        vo.treasureBox = new ArrayList<Integer>(attach.getTreasureBox());
        List<TrainOpponentVO> list = new ArrayList<TrainOpponentVO>();
        //List<Integer> ids = attach.getOpponents();
        List<Integer> ids = logic.getOpponents(playerId);

        float[] exprienceFightRatio = ConfigData.globalParam().exprienceFightRatio;

        int j = 0;
        for (int i = ids.size() - 1; i >= 0; i--) {
            int id = ids.get(i);
            if (id == playerId) continue;
            TrainOpponent opponent = logic.getOpponent(id);
            TrainOpponentVO opp = new TrainOpponentVO();
            if (opponent == null) {
                ServerLogger.warn("不存在的id=" + id);
                continue;
            }
            opp.playerId = opponent.getPlayerId();
            opp.name = opponent.getName();
            opp.level = opponent.getLevel();
            opp.exp = opponent.getExp();
            opp.vip = opponent.getVip();
            opp.vipExp = opponent.getVipExp();
            opp.gang = opponent.getGang();
            opp.vocation = opponent.getVocation();
            opp.fashionId = opponent.getFashionId();
            opp.weapon = opponent.getWeaponId();
            opp.curCards = opponent.getCurCards();
            opp.curSkills = opponent.getCurSkills();
            opp.chenghao = opponent.getTitle();
            opp.zhanli = (int) (opponent.getFight() * exprienceFightRatio[j]);
            j++;
            list.add(opp);
            if (list.size() >= 10) break;
        }
        vo.opponents = list;
        return vo;
    }

    //挑战
    @Command(3902)
    public TrainingFighterVO challenge(int playerId, Int2Param param) {
        TrainingFighterVO result = new TrainingFighterVO();
        TrainAttach attach = logic.getAttach(playerId);
        if (attach.getHp() <= 0 || param.param1 >= logic.getMaxLevel() || param.param1 != attach.getIndex()) {
            result.code = Response.ERR_PARAM;
        }
        Player player = playerService.getPlayer(param.param2);
        float[] exprienceFightRatio = ConfigData.globalParam().exprienceFightRatio;
        float v = exprienceFightRatio[attach.getIndex()];
        result.index = attach.getIndex();
        result.attack = (int) (player.getAttack() * v);
        result.defense = (int) (player.getDefense() * v);
        result.crit = (int) (player.getCrit() * v);
        result.symptom = (int) (player.getSymptom() * v);
        result.fu = (int) (player.getFu() * v);
        result.hp = (int) (player.getHp() * v);
        result.name = player.getName();
        result.level = player.getLev();
        result.vocation = player.getVocation();
        List<Integer> bufferList = equipService.getBufferList(playerId);
        result.bufferList = Lists.newArrayList(bufferList);
        taskService.doTask(playerId, Task.TYPE_TRAIN_TIMES, 1);
        taskService.doTask(playerId, Task.TYPE_PASS_TYPE_COPY, 5, 1); //竞技场
        return result;

    }

    //挑战胜利
    @Command(3903)
    public IntParam challengeWin(int playerId, TrainingResultVO param) {
        IntParam result = new IntParam();
        TrainAttach attach = logic.getAttach(playerId);
        taskService.doTask(playerId, Task.TYPE_TRAIN_WIN_TIMES, 1);
        if (param.index >= logic.getMaxLevel() || param.index != attach.getIndex()/* || attach.getHp() < param.hp*/) {
            result.param = Response.ERR_PARAM;
        } else {
            attach.setHp(param.hp);
            if (param.victory) {
                attach.setIndex(param.index + 1);
                attach.getTreasureBox().add(param.index);
            } else {
                attach.setHp(0);
            }
            attach.commitSync();
        }
        return result;
    }

    //领取奖励
    @Command(3904)
    public TrainingRewardVO takeReward(int playerId, IntParam param) {
        TrainingRewardVO result = new TrainingRewardVO();
        TrainAttach attach = logic.getAttach(playerId);
        int index = param.param;
        if (!attach.getTreasureBox().contains(index)) {
            result.code = Response.ERR_PARAM;
        } else {
            attach.getTreasureBox().remove(index);
            int groupId = ConfigData.globalParam().exprienceRewards[index];
            result.rewards = rewardService.getRewards(playerId, groupId, LogConsume.EXPRIENCE_REWARD);

            //活动奖励
            Reward activityReward = copyService.activityReward(playerId, CopyInstance.TYPE_TRAINING);
            if (activityReward != null) {
                Reward reward = new Reward();
                reward.id = activityReward.id;
                reward.count = activityReward.count;
                result.rewards.add(reward);
                // 发送奖励到背包
                List<GoodsEntry> rewards = new ArrayList<>();
                rewards.add(new GoodsEntry(activityReward.id, activityReward.count));
                goodsService.addRewards(playerId, rewards, LogConsume.EXPRIENCE_REWARD, reward.count);
            }

            attach.commitSync();
        }
        result.index = index;
        return result;
    }
}
