package com.game.module.player;

import com.game.data.*;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsService;
import com.game.module.goods.PlayerBag;
import com.game.module.group.GroupService;
import com.game.module.pet.PetBag;
import com.game.module.pet.PetService;
import com.game.module.pet.Pet;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.team.TeamService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.goods.AttrItem;
import com.game.params.player.PlayerVo;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;
import com.google.common.collect.Lists;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class PlayerCalculator {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private PetService petService;
    @Autowired
    private TitleService titleService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActivityService activityService;

    // 重新计算人物属性
    public void calculate(int playerId) {
        calculate(playerService.getPlayer(playerId));
    }

    // 重新计算人物的属性
    public void calculate(Player player) {
        synchronized (player) {
            initPlayer(player);
            updateAttr(player);
            // 通知前端
            playerService.refreshPlayerToClient(player.getPlayerId());
        }
    }

    public int getNewFight(int playerId) {
        Player player = playerService.getPlayer(playerId);
        synchronized (player) {
            initPlayer(player);
            updateAttr(player);
        }
        return player.getFight();
    }

    // 初始化人物的各种属性
    public void initPlayer(Player player) {
        PlayerUpgradeCfg attr = ConfigData.getConfig(PlayerUpgradeCfg.class, player.getLev());
        player.setHp(attr.hp);
        player.setAttack(attr.attack);
        player.setDefense(attr.defense);
        player.setSymptom(attr.symptom);
        player.setFu(attr.symptom);
        player.setCrit(attr.crit);

        //初始化宝石
        initJewel(player.getPlayerId());
    }

    public void initJewel(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (int type : ConfigData.globalParam().equipTypes) {
            Jewel jewel = data.getJewels().get(type);
            if (jewel == null) {
                jewel = new Jewel();
                jewel.setLev(1);
                data.getJewels().put(type, jewel);
            }
        }
    }


    // 计算属性加成
    public void updateAttr(Player player) {
        int oldFight = player.getFight();
        addEquip(player);
        addJewel(player);
        addArtifact(player);
        AddFashion(player);
        //公会科技加成
        addGuildAttr(player);
        //称号加成
        addTitleAttr(player);
        //宠物加成
        addPet(player);
        //觉醒技能加成
        addAwakeningSkill(player);

        //PlayerVo vo = playerService.toSLoginVo(player.getPlayerId());

        List<Integer> attrList = Lists.newArrayList();
        attrList.add(player.getHp());
        attrList.add(player.getAttack());
        attrList.add(player.getDefense());
        attrList.add(player.getFu());
        attrList.add(player.getSymptom());
        attrList.add(player.getCrit());
        //百分比保持最后
        addPercent(player, attrList);
        player.setAttrList(attrList);

        // 更新战斗力
        float[] fightParams = ConfigData.globalParam().fightParams;

        float fight = player.getHp() * fightParams[0] + player.getAttack() * fightParams[1] + player.getDefense() * fightParams[2] + player.getFu() * fightParams[3] + player.getSymptom() * fightParams[4] +
                player.getCrit() * fightParams[5];
        player.setFight((int) fight);

        if (fight > oldFight) {
            titleService.complete(player.getPlayerId(), TitleConsts.FIGHTING, (int) fight, ActivityConsts.UpdateType.T_VALUE);
            taskService.doTask(player.getPlayerId(), Task.TYPE_FIGHT, (int) fight);

            //战力活动
            int playerId = player.getPlayerId();
            activityService.tour(playerId, ActivityConsts.ActivityTaskCondType.T_COMBAT_EFFECTIVENESS, (int) fight);
        }
        groupService.updateAttr(player.getPlayerId());
        teamService.updateAttr(player.getPlayerId());
        //playerService.refreshPlayerToClient(player.getPlayerId());
    }

    /**
     * 加成公会属性
     *
     * @param player
     */
    private void addGuildAttr(PlayerAddition player) {
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        for (int techId : data.getTechnologys()) {
            GangScienceCfg conf = ConfigData.getConfig(GangScienceCfg.class, techId);
            if (conf.type == 1) { //威力
                player.addAttack(conf.param);
            } else if (conf.type == 2) {
                player.addDefense(conf.param);
            } else if (conf.type == 3) {
                player.addHp(conf.param);
            } else if (conf.type == 4) {
                player.addCrit(conf.param);
            } else if (conf.type == 5) {
                player.addSymptom(conf.param);
            } else if (conf.type == 6) {
                player.addFu(conf.param);
            }
        }
    }

    // 增加装备属性
    private void addEquip(PlayerAddition player) {
        PlayerBag bag = goodsService.getPlayerBag(player.getPlayerId());
        PlayerData data = playerService.getPlayerData(player.getPlayerId());

        for (Goods g : bag.getAllGoods().values()) {
            if (g.isInBag()) {
                continue;
            }
            GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, g.getGoodsId());
            if (!CommonUtil.contain(ConfigData.globalParam().equipTypes, cfg.type)) {
                continue;
            }
            int hp = cfg.hp;
            int attack = cfg.attack;
            int defense = cfg.defense;
            int crit = cfg.crit;
            int fu = cfg.fu;
            int symptom = cfg.symptom;
            int starId = cfg.type * 100000 + cfg.level * 100 + g.getStar();
            EquipStarCfg star = ConfigData.getConfig(EquipStarCfg.class, starId);
            if (star != null) {
                hp += star.hp;
                attack += star.attack;
                defense += star.defense;
                crit += star.crit;
                fu += star.fu;
                symptom += star.symptom;
            }

            int strengthLev = data.getStrengths().getOrDefault(cfg.type,0);
            EquipStrengthCfg strength = ConfigData.getConfig(EquipStrengthCfg.class, cfg.type * 1000 + strengthLev);
            if (strength != null) {
                hp += (int) (hp * strength.add * 0.01f);
                attack += (int) (attack * strength.add * 0.01f);
                defense += (int) (defense * strength.add * 0.01f);
                crit += (int) (crit * strength.add * 0.01f);
                fu += (int) (fu * strength.add * 0.01f);
                symptom += (int) (symptom * strength.add * 0.01f);
            }

            int starLev = data.getStars().getOrDefault(cfg.type,0);
            EquipUpCfg upCfg = ConfigData.getConfig(EquipUpCfg.class, cfg.type * 1000 + starLev);
            if (upCfg != null) {
                hp += upCfg.hp;
                attack += upCfg.attack;
                defense += upCfg.defense;
                crit += upCfg.crit;
                fu += upCfg.fu;
                symptom += upCfg.symptom;
            }

            player.addAttack(attack);
            player.addCrit(crit);
            player.addDefense(defense);
            player.addFu(fu);
            player.addHp(hp);
            player.addSymptom(symptom);
        }

        for (Map.Entry<Integer, Set<Integer>> s : data.getSuitMap().entrySet()) {
            SuitConfig config = ConfigData.getConfig(SuitConfig.class, s.getKey());
            boolean bSuitFlag = false;
            if (s.getValue().size() >= 2) {
                addSuit(player, config.twoAdd);
                bSuitFlag |= config.twoAdd != null;
            }
            if (s.getValue().size() >= 3) {
                addSuit(player, config.threeAdd);
                bSuitFlag |= config.threeAdd != null;
            }
            if (s.getValue().size() >= 4) {
                addSuit(player, config.fourAdd);
                bSuitFlag |= config.fourAdd != null;
            }
            if (s.getValue().size() >= 5) {
                addSuit(player, config.fiveAdd);
                bSuitFlag |= config.fiveAdd != null;
            }
            if (s.getValue().size() >= 6) {
                addSuit(player, config.sixAdd);
                bSuitFlag |= config.sixAdd != null;
            }

            if (bSuitFlag) {
                //taskService.doTask(player.getPlayerId(), Task.TYPE_SUIT, config.id);
            }
        }
    }

    /**
     * 套装值加成
     *
     * @param player
     * @param map
     */
    private void addSuit(PlayerAddition player, Map<Integer, int[]> map) {
        if (map == null) {
            return;
        }
        for (Map.Entry<Integer, int[]> s1 : map.entrySet()) {
            if (s1.getKey() == 2) {
                int[] arr = s1.getValue();
                if (arr[0] == 1) {
                    player.addHp(arr[1]);
                } else if (arr[0] == 2) {
                    player.addAttack(arr[1]);
                } else if (arr[0] == 3) {
                    player.addDefense(arr[1]);
                } else if (arr[0] == 4) {
                    player.addFu(arr[1]);
                } else if (arr[0] == 5) {
                    player.addSymptom(arr[1]);
                } else if (arr[0] == 6) {
                    player.addCrit(arr[1]);
                }
            }
        }
    }

    //宝石
    private void addJewel(PlayerAddition player) {
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        for (Entry<Integer, Jewel> entry : data.getJewels().entrySet()) {
            Jewel jewel = entry.getValue();
            int type = entry.getKey();
            /*if(jewel.getLev()==0){
                continue;
			}*/
            int id = type * 1000 + jewel.getLev();
            EquipJewelCfg cfg = ConfigData.getConfig(EquipJewelCfg.class, id);
            if (cfg == null) {
                continue;
            }
            player.addAttack(cfg.attack);
            player.addCrit(cfg.crit);
            player.addDefense(cfg.defense);
            player.addFu(cfg.fu);
            player.addHp(cfg.hp);
            player.addSymptom(cfg.symptom);
        }
    }


    //加称号属性
    private void addTitleAttr(PlayerAddition player) {
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        Player p = (Player) player;
        if (!data.getModules().contains(PlayerService.MODULE_TITLE)) {
            return;
        }
        //装备称号加成
        TitleConfig config = ConfigData.getConfig(TitleConfig.class, p.getTitle());
        if (config != null) {
            player.addAttack(config.attack);
            player.addCrit(config.crit);
            player.addDefense(config.defense);
            player.addFu(config.fu);
            player.addSymptom(config.symptom);
            player.addHp(config.hp);
        }
        //套装加成
        int size = data.getTitles().size();

        TitleSelectConfig titleSelectConfig = ConfigData.getConfig(TitleSelectConfig.class, size);
        if (titleSelectConfig != null) {
            player.addAttack(titleSelectConfig.srvAttack);
            player.addCrit(titleSelectConfig.srvCrit);
            player.addDefense(titleSelectConfig.srvDefense);
            player.addFu(titleSelectConfig.srvFu);
            player.addSymptom(titleSelectConfig.srvSymptom);
            player.addHp(titleSelectConfig.srvHp);
        }
    }

    //加宠物战力，非百分比的部分
    private void addPet(PlayerAddition player) {
        PetBag petBag = petService.getPetBag(player.getPlayerId());
        if (petBag == null) {
            return;
        }

        for (Pet pet : petBag.getPetMap().values()) {
            PetConfig config = ConfigData.getConfig(PetConfig.class, pet.getConfigId());

            if (config == null)
                continue;

            player.addAttack(config.attackFix);
            player.addCrit(config.critFix);
            player.addDefense(config.defenseFix);
            player.addFu(config.fuFix);
            player.addSymptom(config.symptomFix);
            player.addHp(config.hpFix);
        }

        Pet fightPet = petService.getFightPet(player.getPlayerId());
        if (fightPet != null) {
            PetSkillConfig config = ConfigData.getConfig(PetSkillConfig.class, fightPet.getPassiveSkillId());
            if (config != null) {
                player.addAttack(config.attackFix);
                player.addCrit(config.critFix);
                player.addDefense(config.defenseFix);
                player.addFu(config.fuFix);
                player.addSymptom(config.symptomFix);
                player.addHp(config.hpFix);
            }
        }
    }

    //加时装战力，非百分比的部分
    private void AddFashion(PlayerAddition player) {
        PlayerData data = playerService.getPlayerData(player.getPlayerId());

        //更新魅力值
        Set<Integer> fashionRankSet = data.getFashionRankSet();
        if (fashionRankSet == null || fashionRankSet.isEmpty()) {
            return;
        }
        int glamour = 0;
        for (Integer id : fashionRankSet) {
            FashionUpCfg config = ConfigData.getConfig(FashionUpCfg.class, id);
            if (config == null) {
                ServerLogger.warn("时装找不到，时装收集表id=" + id);
                continue;
            }
            glamour += config.charm;
        }
        data.setGlamour(glamour);

        Collection<Object> configs = ConfigData.getConfigs(FashionCollectCfg.class);
        if (configs == null || configs.isEmpty()) {
            ServerLogger.warn("时装收集表不存在");
            return;
        }

        Player p = (Player) player;

        List<FashionCollectCfg> list = new ArrayList<>();
        for (Object object : configs) {
            list.add((FashionCollectCfg) object);
        }

        for (int i = 0; i < list.size(); i++) {
            FashionCollectCfg cfg = list.get(i);
            boolean find = false;
            if (glamour >= cfg.value) {
                if (i != list.size() - 1) {//最后一个
                    FashionCollectCfg cfgNext =  list.get(i + 1);
                    if (glamour >= cfg.value && glamour < cfgNext.value) {
                        find = true;
                    }
                } else {
                    find = true;
                }
            }

            if (find) {
                player.addAttack(cfg.srvAttack);
                player.addCrit(cfg.srvCrit);
                player.addDefense(cfg.srvDefense);
                player.addFu(cfg.srvFu);
                player.addSymptom(cfg.srvSymptom);
                player.addHp(cfg.srvHp);
                break;
            }
        }

        //时装属性加成
        for(int fashionId : data.getFashionMap().keySet()){
            addFashionAttr(player, fashionId);
        }
        //addFashionAttr(player, p.getFashionId());
        //addFashionAttr(player, p.getWeaponId());
        //addFashionAttr(player, data.getCurHead());
    }

    private void addFashionAttr(PlayerAddition player, int id) {
        FashionCfg fashionCfg = ConfigData.getConfig(FashionCfg.class, id);
        if (fashionCfg == null) {
            return;
        }

        //附加阶级属性
        PlayerData playerData = playerService.getPlayerData(player.getPlayerId());
        if (playerData == null) {
            ServerLogger.warn("玩家数据找不到，玩家id=" + player.getPlayerId());
            return;
        }

        Set<Integer> fashionRankSet = playerData.getFashionRankSet();
        if (fashionRankSet == null || fashionRankSet.isEmpty()) {
            ServerLogger.warn("时装信息错误，玩家id=" + player.getPlayerId());
            return;
        }
        for (Integer cfgId : fashionRankSet) {
            FashionUpCfg fashionUpCfg = ConfigData.getConfig(FashionUpCfg.class, cfgId);
            if (fashionUpCfg == null) {
                ServerLogger.warn("时装阶级属性错误");
                continue;
            }
            if (fashionUpCfg.FashionID == id) {
                player.addAttack(fashionCfg.attack + fashionUpCfg.attack);
                player.addCrit(fashionCfg.crit + fashionUpCfg.crit);
                player.addDefense(fashionCfg.defense + fashionUpCfg.defense);
                player.addFu(fashionCfg.fu + fashionUpCfg.fu);
                player.addSymptom(fashionCfg.symptom + fashionUpCfg.symptom);
                player.addHp(fashionCfg.hp + fashionUpCfg.hp);
                break;
            }
        }
    }

    //处理神器
    private void addArtifact(PlayerAddition player) {
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        if (data.getArtifacts().isEmpty()) {
            return;
        }
        for (Entry<Integer, int[]> artifact : data.getArtifacts().entrySet()) {
            int id = artifact.getKey();
            int[] components = artifact.getValue();
            int activeCount = 0;
            for (int c : components) {
                if (c == 1) {
                    activeCount++;
                }
            }
            if (activeCount == 0) {
                continue;
            }
            ArtifactCfg cfg = ConfigData.getConfig(ArtifactCfg.class, id);
            for (int i = 0; i < activeCount; i++) {
                addAttrValue(player, cfg.attrs[i][0], cfg.attrs[i][1]);
            }
        }

        for (Entry<Integer, Integer> s : data.getArtifactsLevelUp().entrySet()) {
            ArtifactLevelUpCfg conf = ConfigData.getArtifactLevelUpCfgs().get(s.getKey() + "_" + s.getValue());
            for (int[] attr : conf.attrs) {
                addAttrValue(player, attr[0], attr[1]);
            }
        }
    }

    //加觉醒技能，非百分比的部分
    private void addAwakeningSkill(PlayerAddition player) {
        PlayerData playerData = playerService.getPlayerData(player.getPlayerId());

        Map<Integer, Integer> awakeningSkillMap = playerData.getAwakeningSkillMap();
        if (awakeningSkillMap.size() == 0) {
            return;
        }

        for (int key : awakeningSkillMap.keySet()) {
            AwakenAttributeCfg awakenAttributeCfg = ConfigData.getConfig(AwakenAttributeCfg.class, awakeningSkillMap.get(key));
            if (awakenAttributeCfg != null) {
                switch (awakenAttributeCfg.Attribute) {
                    case 7:
                        player.addHp(awakenAttributeCfg.value);
                        break;
                    case 8:
                        player.addAttack(awakenAttributeCfg.value);
                        break;
                    case 9:
                        player.addDefense(awakenAttributeCfg.value);
                        break;
                    case 10:
                        player.addCrit(awakenAttributeCfg.value);
                        break;
                    case 11:
                        player.addSymptom(awakenAttributeCfg.value);
                        break;
                    case 12:
                        player.addFu(awakenAttributeCfg.value);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //百分比的（要统一放在这里处理，先累加所有的百分比，再计算原始值+原始值%)
    public void addPercent(PlayerAddition player, List array) {
        //装备的特殊属性
        HashMap<Integer, Integer> percentAttrs = new HashMap<Integer, Integer>();
        PlayerBag bag = goodsService.getPlayerBag(player.getPlayerId());
        for (Goods g : bag.getAllGoods().values()) {
            if (g.isInBag()) {
                continue;
            }
            for (AttrItem attr : g.getAddAttrList()) {
                if (attr.type > 6) {
                    continue;
                }
                addPercentAttr(percentAttrs, attr.type, attr.value);
            }
        }
        //时装的百分比
        PlayerData data = playerService.getPlayerData(player.getPlayerId());
        FashionCollectCfg collect = ConfigData.getConfig(FashionCollectCfg.class, data.getFashionMap().size());
        if (collect != null) {
            addPercentAttr(percentAttrs, Goods.ATK, collect.srvattackPercent);
            addPercentAttr(percentAttrs, Goods.DEF, collect.srvDefensePercent);
            addPercentAttr(percentAttrs, Goods.CRIT, collect.srvCritPercent);
            addPercentAttr(percentAttrs, Goods.SYMPTOM, collect.srvSymptomPercent);
            addPercentAttr(percentAttrs, Goods.FU, collect.srvFuPercent);
            addPercentAttr(percentAttrs, Goods.HP, collect.srvHpPercent);
        }

        //出战宠物百分比
        Pet pet = petService.getFightPet(player.getPlayerId());
        if (pet != null) {
            PetSkillConfig skillConfig = ConfigData.getConfig(PetSkillConfig.class, pet.getPassiveSkillId());
            if (skillConfig != null) {
                addPercentAttr(percentAttrs, Goods.ATK, skillConfig.attackPercent);
                addPercentAttr(percentAttrs, Goods.DEF, skillConfig.defensePercent);
                addPercentAttr(percentAttrs, Goods.CRIT, skillConfig.critPercent);
                addPercentAttr(percentAttrs, Goods.SYMPTOM, skillConfig.symptomPercent);
                addPercentAttr(percentAttrs, Goods.FU, skillConfig.fuPercent);
                addPercentAttr(percentAttrs, Goods.HP, skillConfig.hpPercent);
            }
        }

        //套装百分比
        for (Map.Entry<Integer, Set<Integer>> s : data.getSuitMap().entrySet()) {
            SuitConfig config = ConfigData.getConfig(SuitConfig.class, s.getKey());
            if (s.getValue().size() >= 2) {
                addSuitPercent(percentAttrs, config.twoAdd);
            }
            if (s.getValue().size() >= 3) {
                addSuitPercent(percentAttrs, config.threeAdd);
            }
            if (s.getValue().size() >= 4) {
                addSuitPercent(percentAttrs, config.fourAdd);
            }
            if (s.getValue().size() >= 5) {
                addSuitPercent(percentAttrs, config.fiveAdd);
            }
            if (s.getValue().size() >= 6) {
                addSuitPercent(percentAttrs, config.sixAdd);
            }
        }

        //觉醒技能百分比
        for (int key : data.getAwakeningSkillMap().keySet()) {
            AwakenAttributeCfg awakenAttributeCfg = ConfigData.getConfig(AwakenAttributeCfg.class, data.getAwakeningSkillMap().get(key));
            if (awakenAttributeCfg != null && awakenAttributeCfg.Attribute < 7) {
                addPercentAttr(percentAttrs, awakenAttributeCfg.Attribute, awakenAttributeCfg.value);
            }
        }

        for (int i = 0; i < 6; i++) {
            array.add(0);
        }
        for (Entry<Integer, Integer> attr : percentAttrs.entrySet()) {
            addAttrValuePercent(player, attr.getKey(), attr.getValue());
            array.set(5 + attr.getKey(), attr.getValue());
        }

    }

    private void addSuitPercent(Map<Integer, Integer> percentAttrs, Map<Integer, int[]> map) {
        if (map == null) {
            return;
        }
        for (Map.Entry<Integer, int[]> s1 : map.entrySet()) {
            if (s1.getKey() == 1) {
                int[] arr = s1.getValue();
                if (arr[0] == 1) {
                    addPercentAttr(percentAttrs, Goods.HP, (arr[1]));
                } else if (arr[0] == 2) {
                    addPercentAttr(percentAttrs, Goods.ATK, (arr[1]));
                } else if (arr[0] == 3) {
                    addPercentAttr(percentAttrs, Goods.DEF, (arr[1]));
                } else if (arr[0] == 4) {
                    addPercentAttr(percentAttrs, Goods.FU, (arr[1]));
                } else if (arr[0] == 5) {
                    addPercentAttr(percentAttrs, Goods.SYMPTOM, (arr[1]));
                } else if (arr[0] == 6) {
                    addPercentAttr(percentAttrs, Goods.CRIT, (arr[1]));
                }
            }
        }
    }

    private void addPercentAttr(Map<Integer, Integer> data, int type, int value) {
        Integer curPercent = data.get(type);
        if (curPercent == null) {
            curPercent = 0;
        }
        curPercent += value;
        data.put(type, curPercent);
    }

    private void addAttrValuePercent(PlayerAddition player, int type, int valuePercent) {
        switch (type) {
            case Goods.HP:
                player.addHp((int) (player.getHp() * valuePercent * 0.01));
                break;
            case Goods.ATK:
                player.addAttack((int) (player.getAttack() * valuePercent * 0.01));
                break;
            case Goods.DEF:
                player.addDefense((int) (player.getDefense() * valuePercent * 0.01));
                break;
            case Goods.CRIT:
                player.addCrit((int) (player.getCrit() * valuePercent * 0.01));
                break;
            case Goods.FU:
                player.addFu((int) (player.getFu() * valuePercent * 0.01));
                break;
            case Goods.SYMPTOM:
                player.addSymptom((int) (player.getSymptom() * valuePercent * 0.01));
                break;
            default:
        }
    }

    private void addAttrValue(PlayerAddition player, int type, int value) {
        switch (type) {
            case Goods.HP:
                player.addHp(value);
                break;
            case Goods.ATK:
                player.addAttack(value);
                break;
            case Goods.DEF:
                player.addDefense(value);
                break;
            case Goods.CRIT:
                player.addCrit(value);
                break;
            case Goods.FU:
                player.addFu(value);
                break;
            case Goods.SYMPTOM:
                player.addSymptom(value);
                break;
            default:
        }
    }

}
