package com.game.module.skill;

import com.game.data.AwakenAttributeCfg;
import com.game.data.Response;
import com.game.data.SkillCardConfig;
import com.game.data.SkillConfig;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.skill.SkillCardGroupInfo;
import com.game.params.skill.SkillCardVo;
import com.game.params.skill.SkillInfo;
import com.game.util.ConfigData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 技能系统
 */
@Service
public class SkillService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private ActivityService activityService;

    //获取技能信息
    public SkillInfo getInfo(int playerId) {
        SkillInfo info = new SkillInfo();
        PlayerData data = playerService.getPlayerData(playerId);
        info.cardGroupInfo = new SkillCardGroupInfo();
        info.cardGroupInfo.curGroupId = data.getCurCardId();
        info.cardGroupInfo.curCards = new ArrayList<>(data.getCurrCard());
        info.curSkills = new ArrayList<>(data.getCurSkills());
        info.skills = new ArrayList<>(data.getSkills());
        info.skillCards = new ArrayList<>(data.getSkillCards().size());
        for (Entry<Integer, SkillCard> card : data.getSkillCards().entrySet()) {
            SkillCardVo vo = new SkillCardVo();
            vo.id = card.getKey();
            vo.exp = card.getValue().getExp();
            vo.cardId = card.getValue().getCardId();
            vo.lev = card.getValue().getLev();
            info.skillCards.add(vo);
        }
        return info;
    }

    /**
     * @param playerId
     * @param skillId
     * @param onKey    -1二觉被动免费提升等级
     * @return
     */
    //升级技能
    public int upgradeSkill(int playerId, int skillId, int onKey) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);

        List<Integer> skills = Lists.newArrayList(skillId);
        if (onKey == 1) { //
            skills = Lists.newArrayList(playerData.getSkills());
        }

        int consume = 0;
        Map<Integer, Integer> id2skill = Maps.newHashMap();
        Map<Integer, Integer> id2lv = Maps.newHashMap();
        for (int sid : skills) {
            Map<Integer, Integer> awakeningSkillMap = playerData.getAwakeningSkillMap();
            int maxLevel = player.getLev();
            //是否已经满级
            SkillConfig cfg = ConfigData.getConfig(SkillConfig.class, sid);
            if (cfg.nextId == 0) {
                continue;
            }
            cfg = ConfigData.getConfig(SkillConfig.class, sid);
            for (Map.Entry<Integer, Integer> entry : awakeningSkillMap.entrySet()) {
                AwakenAttributeCfg awakenAttributeCfg = ConfigData.getConfig(AwakenAttributeCfg.class, entry.getValue());
                if (awakenAttributeCfg != null && awakenAttributeCfg.value == cfg.skillType) {
                    maxLevel += awakenAttributeCfg.lv;
                    break;
                }
            }
            int nextId = sid;
            do {
                if (onKey != -1) { //是否2觉被动免费提升
                    if (consume + cfg.coin > player.getCoin()) {
                        ServerLogger.info("coin not enough," + consume + ",," + player.getCoin());
                        break;
                    }
                    consume += cfg.coin;
                }

                cfg = ConfigData.getConfig(SkillConfig.class, nextId);
                nextId = cfg.nextId;
                id2skill.put(sid, nextId);
                id2lv.put(cfg.skillType, cfg.lev);
            } while (onKey == 1 && maxLevel > cfg.lev);
        }

        if (consume > 0) {
            playerService.decCoin(playerId, consume, LogConsume.SKILL_UPGRADE);
        }

        for (Map.Entry<Integer, Integer> e : id2skill.entrySet()) {
            int sid = e.getKey();
            int targetId = e.getValue();
            int index = playerData.getSkills().indexOf(sid);
            if (index == -1) {
                ServerLogger.warn("skill not found ,skillId ==>" + sid);
                return Response.ERR_PARAM;
            }
            playerData.getSkills().set(index, targetId);
            index = playerData.getCurSkills().indexOf(sid);
            if (index >= 0) {
                playerData.getCurSkills().set(index, targetId);
            }
        }

        taskService.doTask(playerId, Task.FINISH_SKILL);
        if (!id2lv.isEmpty()) {
            Map<Integer, int[]> condParams = Maps.newHashMap();
            for (Map.Entry<Integer, Integer> e : id2lv.entrySet()) {
                condParams.put(Task.TYPE_SKILL_LEVEL, new int[]{e.getKey(), e.getValue()});
            }
            taskService.doTask(playerId, condParams);
        }
        // taskService.doTask(playerId, Task.TYPE_SKILL_LEVEL, cfg.skillType, cfg.lev);

        updateSkill2Client(playerId);
        return Response.SUCCESS;
    }

    //更新技能信息
    public void updateSkill2Client(int playerId) {
        //开启了卡片投资才进行判断
        cardInvestment(playerId);
        SessionManager.getInstance().sendMsg(SkillExtension.UPDATE_SKILL, getInfo(playerId), playerId);
    }

    //升级技能卡
    public int upgradeSkillCard(int playerId, List<Integer> ids) {
        PlayerData data = playerService.getPlayerData(playerId);
        int id = ids.get(0);
        SkillCard card = data.getSkillCards().get(id);
        //已经是最高级
        SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
        if (cfg.nextCard == 0) {
            return Response.MAX_LEV;
        }
        if (cfg.type == SkillCard.SPECIAL) {
            return Response.ERR_PARAM;
        }
        //逐个经验添加
        boolean full = false;

        ConcurrentHashMap<Integer, SkillCard> skillCards = data.getSkillCards();
        if (skillCards == null) {
            ServerLogger.warn("卡片不存在" + id);
            return Response.ERR_PARAM;
        }
        List<Integer> deleyeList = new ArrayList<>();

        //使用同类材料卡升级
        for (Entry<Integer, SkillCard> entry : skillCards.entrySet()) {
            SkillCardConfig skillCardConfig = ConfigData.getConfig(SkillCardConfig.class, entry.getValue().getCardId());
            if (skillCardConfig == null) {
                continue;
            }
            if (skillCardConfig.cfgType == cfg.cfgType && skillCardConfig.subType == SkillConsts.SubType.T_MATERIAL_CARD) {
                //判断升级
                deleyeList.add(entry.getKey());//要移除的卡片
                card.setExp(card.getExp() + skillCardConfig.decompose + entry.getValue().getExp());
                while (card.getExp() >= cfg.exp) {
                    card.setLev(cfg.lv + 1);
                    card.setExp(card.getExp() - cfg.exp);
                    card.setCardId(cfg.nextCard);

                    //是否满级
                    cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
                    if (cfg.nextCard == 0) {
                        full = true;
                        break;
                    }
                }
            }
            if (full) {
                break;
            }
        }

        //删除材料卡
        if (!deleyeList.isEmpty()) {
            for (Integer cardId : deleyeList) {
                skillCards.remove(cardId);
            }
        }

        taskService.doTask(playerId, Task.FINISH_CARD_UPGRADE, cfg.lv);
        //更新前端
        updateSkill2Client(playerId);
        return Response.SUCCESS;
    }

    //合成技能卡
    public int composeCard(int playerId, List<Integer> ids) {
        PlayerData data = playerService.getPlayerData(playerId);
        SkillCard card = data.getSkillCards().get(ids.get(0));
        if (card == null) {
            return Response.ERR_PARAM;
        }
        SkillCardConfig cfgOld = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
        SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
        int newCardID = cfg.nextQualityCard;

        if (ids.size() < 5) {
            return Response.ERR_PARAM;
        }
        //检查副卡类型，品质
        for (int i = 1; i < ids.size(); i++) {
            SkillCard sc = data.getSkillCards().get(ids.get(i));
            SkillCardConfig scc = ConfigData.getConfig(SkillCardConfig.class, sc.getCardId());
            if (scc.type == SkillCard.SPECIAL || scc.quality != cfg.quality || scc.quality == 5
                    || cfg.goodsid != scc.goodsid) {
                return Response.ERR_PARAM;
            }
        }

        //检查升级
        SkillCard newCard = playerService.addSkillCard(playerId, newCardID);
        cfg = ConfigData.getConfig(SkillCardConfig.class, newCard.getCardId());
        int[] arr = ConfigData.globalParam().cardLvUpPrice[cfgOld.quality - 1];
        List<GoodsEntry> goodsEntries = Lists.newArrayList();
        goodsEntries.add(new GoodsEntry(arr[0], arr[1]));
        int ret = goodsService.decConsume(playerId, goodsEntries, LogConsume.SKILL_CARD_MAKE);
        if (ret != Response.SUCCESS) {
            return ret;
        }

        for (int delId : ids) {
            //扣除
            SkillCard del = data.getSkillCards().remove(delId);
            for (List<Integer> group : data.getSkillCardSets()) {
                for (int i = 0; i < 4; i++) {
                    if (group.get(i) == delId) {
                        group.set(i, 0);
                    }
                }
            }
            SkillCardConfig delCfg = ConfigData.getConfig(SkillCardConfig.class, del.getCardId());
            //增加经验
            newCard.setExp(newCard.getExp() + del.getExp() + delCfg.decompose);
        }

        newCard.setExp(newCard.getExp() - cfg.decompose);
        while (newCard.getExp() >= cfg.exp) {
            newCard.setLev(newCard.getLev() + 1);
            newCard.setExp(newCard.getExp() - cfg.exp);
            newCard.setCardId(cfg.nextCard);

            cfg = ConfigData.getConfig(SkillCardConfig.class, newCard.getCardId());
            if (cfg.nextCard == 0) {
                break;
            }
        }
        taskService.doTask(playerId, Task.FINISH_CARD_COMPOSE, cfg.quality + 1, 1);
        //更新前端
        updateSkill2Client(playerId);
        return Response.SUCCESS;
    }

    //使用卡牌
    public int setCard(int playerId, int index, int id) {
        PlayerData data = playerService.getPlayerData(playerId);
        if (id > 0) {
            if (!data.getSkillCards().containsKey(id)) {
                return Response.ERR_PARAM;
            }
            SkillCard card = data.getSkillCards().get(id);
            SkillCardConfig cfg = ConfigData.getConfig(SkillCardConfig.class, card.getCardId());
            if (cfg.type == SkillCard.SPECIAL) {
                return Response.ERR_PARAM;
            }
            int oldIndex = data.getCurrCard().indexOf(id);
            if (oldIndex >= 0) {
                data.getCurrCard().set(oldIndex, 0);
            }
        }
        data.getCurrCard().set(index, id);
        updateSkill2Client(playerId);
        playerService.refreshPlayerToClient(playerId);
        taskService.doTask(playerId, Task.FINISH_SKILL);
        return Response.SUCCESS;
    }

    public void gmAddSkillCard(int playerId, int cardId) {
        playerService.addSkillCard(playerId, cardId);
        updateSkill2Client(playerId);
    }

    //统计卡片等级和数量
    public Map<Integer, Integer> getTypeNumberMap(int playerId) {
        Map<Integer, Integer> skillCardMap = new ConcurrentHashMap<>();
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return skillCardMap;
        }
        ConcurrentHashMap<Integer, SkillCard> skillCards = playerData.getSkillCards();
        if (skillCards == null || skillCards.isEmpty()) {
            return skillCardMap;
        }
        for (SkillCard skillCard : skillCards.values()) {
            SkillCardConfig skillCardConfig = ConfigData.getConfig(SkillCardConfig.class, skillCard.getCardId());
            if (skillCardConfig != null && skillCardConfig.id != 59901 && !skillCardConfig.name.equals("经验卡") && skillCardConfig.lv >= 1) {
                for (int i = 1; i <= skillCardConfig.lv; i++) {
                    //高级品质装备也算低级品质的数量
                    if (skillCardMap.get(i) == null) {
                        skillCardMap.put(i, 1);
                    } else {
                        skillCardMap.put(i, skillCardMap.get(i) + 1);
                    }
                }
            }
        }
        return skillCardMap;
    }

    //卡片投资
    public void cardInvestment(int playerId) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData != null && activityService.checkIsOpen(playerData, ActivityConsts.ActivityTaskCondType.T_CARD_INVESTMENT)) {
            Map<Integer, Integer> typeNumberMap = getTypeNumberMap(playerId);
            if (typeNumberMap != null && !typeNumberMap.isEmpty()) {
                activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_CARD_INVESTMENT, true, typeNumberMap, true);
            }
        }
    }
}
