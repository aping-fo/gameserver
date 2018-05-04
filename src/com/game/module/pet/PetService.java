package com.game.module.pet;

import com.game.data.PetActivityConfig;
import com.game.data.PetConfig;
import com.game.data.PetSkillConfig;
import com.game.data.Response;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.Reward;
import com.game.params.pet.*;
import com.game.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lucky on 2017/9/13.
 * 宠物
 */
@Service
public class PetService {
    private static final int CMD_IMPROVE = 7007;
    private static final int CMD_TO_FIGHT = 7008;
    private static final int CMD_UPDATE_BAG = 7003;
    private static final int CMD_CHANGE = 7009;
    private static final int CMD_SHOW = 7010;
    @Autowired
    private PetDao petDao;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerCalculator calculator;
    @Autowired
    private RandomRewardService randomRewardService;
    @Autowired
    private TaskService taskService;

    private Map<Integer, PetBag> petBags = new ConcurrentHashMap<>();

    //初始化背包
    public void initBag(int playerId) {
        PetBag bag = new PetBag();
        petBags.putIfAbsent(playerId, bag);
        Context.getThreadService().execute(new Runnable() {
            @Override
            public void run() {
                petDao.insert(playerId);
            }
        });
    }

    //更新数据库
    public void updateBag(int playerId) {
        PetBag data = petBags.get(playerId);
        if (data == null) {
            return;
        }
        if (data.updateFlag) {
            data.updateFlag = false;
            String str = JsonUtils.object2String(data);
            byte[] dbData = str.getBytes(Charset.forName("utf-8"));
            petDao.update(playerId, CompressUtil.compressBytes(dbData));
        }
    }

    public PetBag getPetBag(int playerId) {
        PetBag bag = petBags.get(playerId);
        if (bag != null) {
            return bag;
        }

        byte[] dbData = petDao.select(playerId);
        if (dbData != null) {
            dbData = CompressUtil.decompressBytes(dbData);
            bag = JsonUtils.string2Object(new String(dbData, Charset.forName("utf-8")), PetBag.class);
            if (bag == null) {
                ServerLogger.warn("Err Player Goods:", playerId, dbData.length);
                bag = new PetBag();
            }
        } else {
            bag = new PetBag();
        }
        petBags.put(playerId, bag);

        int maxId = 0; //设置最大ID
        for (int id : bag.getPetMap().keySet()) {
            if (id > maxId) {
                maxId = id;
            }
        }
        bag.idGen.set(maxId);
        return bag;
    }

    /**
     * 玩家退出
     *
     * @param playerId
     */
    public void onLogout(int playerId) {
        updateBag(playerId);
        petBags.remove(playerId);
    }

    /**
     * 获取宠物列表
     *
     * @param playerId
     */
    public PetBagVO getPets(int playerId) {
        PetBag bag = getPetBag(playerId);
        return bag.toProto();
    }

    /**
     * 新增一个宠物
     *
     * @param playerId
     * @param configID
     */
    public void addPet(int playerId, int configID) {
        PetBag bag = getPetBag(playerId);
        PetConfig newPetConfig = ConfigData.getConfig(PetConfig.class, configID);
        if (checkSamePet(playerId, configID)) { //分解碎片
            addPetMaterial(playerId, newPetConfig.materialId, newPetConfig.gainNeedMaterialCount, true);
            return;
        }

        List<Pet> pets = new ArrayList<>();
        List<Int2Param> updateIds = Lists.newArrayList();
//        List<Integer> skills = ConfigData.PassiveSkills.get(newPetConfig.quality);
//        int passiveSkillId = skills.get(RandomUtil.randInt(skills.size()));

        Pet pet = new Pet(bag.idGen.incrementAndGet(), newPetConfig.id, newPetConfig.passiveSkillId, newPetConfig.name);
        bag.getPetMap().put(pet.getId(), pet);
        pets.add(pet);
        pushUpdateBag(playerId, pets, updateIds);

        if(bag.getFightPetId() == 0)
        {
            Int2Param cli1 = this.toFight(playerId, pet.getId());
            cli1.param1 = Response.SUCCESS;
            cli1.param2 = pet.getId();
            SessionManager.getInstance().sendMsg(CMD_TO_FIGHT, cli1, playerId);
        }
        //重新计算人物属性
        calculator.calculate(playerId);

        int count = getQualityCount(newPetConfig.quality, bag.getPetMap());
        taskService.doTask(playerId, Task.TYPE_PET, newPetConfig.quality, count);
    }

    private int getQualityCount(int quality, Map<Integer, Pet> map) {
        int count = 0;
        for (Pet pet : map.values()) {
            PetConfig newPetConfig = ConfigData.getConfig(PetConfig.class, pet.getConfigId());
            if (newPetConfig.quality == quality) {
                count++;
            }
        }
        return count;
    }

    private boolean checkSamePet(int playerId, int configID) {
        PetBag bag = getPetBag(playerId);
        for (Pet pet : bag.getPetMap().values()) {
            if (pet.getConfigId() == configID) {
                return true;
            }
        }
        return false;
    }

    public boolean checkEnough(int playerId, int configId, int count) {
        PetBag bag = getPetBag(playerId);
        Integer currentCount = bag.getMaterialMap().get(configId);
        if (currentCount == null) {
            currentCount = 0;
        }
        return currentCount >= count;
    }

    public List<Int2Param> consume(int playerId, List<Int2Param> params) {
        PetBag bag = getPetBag(playerId);
        List<Int2Param> updateList = Lists.newArrayList();
        for (Int2Param param : params) { //扣除材料
            Integer count = bag.getMaterialMap().get(param.param1);
            if (count == param.param2) {
                bag.getMaterialMap().remove(param.param1);
            } else {
                bag.getMaterialMap().put(param.param1, count - param.param2);
            }

            Int2Param idParam = new Int2Param();
            idParam.param1 = param.param1;
            idParam.param2 = count - param.param2;

            updateList.add(idParam);
        }
        bag.updateFlag = true;
        pushUpdateBag(playerId, Lists.newArrayList(), updateList);
        return updateList;
    }

    /**
     * 增加材料
     *
     * @param playerId
     * @param configID
     * @param count
     */
    public void addPetMaterial(int playerId, int configID, int count, boolean toCli) {
        PetBag bag = getPetBag(playerId);
        Integer currentCount = bag.getMaterialMap().get(configID);
        if (currentCount == null) {
            currentCount = 0;
        }

        int remainCount = currentCount + count;
        bag.getMaterialMap().put(configID, remainCount);

        List<Int2Param> updateIds = Lists.newArrayList();
        Int2Param idParam = new Int2Param();
        idParam.param1 = configID;
        idParam.param2 = remainCount;
        updateIds.add(idParam);
        bag.updateFlag = true;
        if (toCli) {
            pushUpdateBag(playerId, Collections.emptyList(), updateIds);
        }
    }

    /**
     * 获得
     *
     * @param playerId
     * @param id
     */
    public IntParam gainPet(int playerId, int id) {
        IntParam cli = new IntParam();
        PetBag bag = getPetBag(playerId);
        Integer currentCount = bag.getMaterialMap().get(id);
        if (currentCount == null) {
            currentCount = 0;
        }
        PetConfig petConfig = ConfigData.getConfig(PetConfig.class, id);
        PetConfig newPetConfig = ConfigData.getConfig(PetConfig.class, petConfig.petId);
        if (checkSamePet(playerId, newPetConfig.id)) {
            cli.param = Response.PET_HAS_SAME_TYPE;
            return cli;
        }

        //数量不够
        int remainCount = currentCount - petConfig.gainNeedMaterialCount;
        if (remainCount < 0) {
            cli.param = Response.PET_MATERIAL_NOT_ENOUGH;
            return cli;
        }
        if (remainCount == 0) {
            bag.getMaterialMap().remove(id);
        } else {
            bag.getMaterialMap().put(id, remainCount);
        }

        addPet(playerId, newPetConfig.id);
        List<Pet> pets = Lists.newArrayList();
        List<Int2Param> updateIds = Lists.newArrayList();
        Int2Param idParam = new Int2Param();
        idParam.param1 = id;
        idParam.param2 = remainCount;
        updateIds.add(idParam);

        pushUpdateBag(playerId, pets, updateIds);
        cli.param = Response.SUCCESS;
        return cli;
    }


    /**
     * 碎片合成
     *
     * @param playerId
     * @param id
     * @deprecated
     */
    /**
     public IntParam compound(int playerId, int id, int count) {
     IntParam cli = new IntParam();
     PetBag bag = getPetBag(playerId);
     Integer currentCount = bag.getMaterialMap().get(id);
     if (currentCount == null) {
     currentCount = 0;
     }
     PetConfig petConfig = ConfigData.getConfig(PetConfig.class, id);
     //数量不够
     int needCount = petConfig.nextQualityMaterialCount * count;
     if (currentCount < needCount) {
     cli.param = Response.PET_MATERIAL_NOT_ENOUGH;
     return cli;
     }
     if (petConfig.nextQualityId == 0) {
     cli.param = Response.ERR_PARAM;
     return cli;
     }
     List<Int2Param> updateIds = Lists.newArrayList();
     Int2Param idParam = new Int2Param();
     idParam.param1 = id;
     idParam.param2 = currentCount - needCount;
     updateIds.add(idParam);

     if (currentCount == needCount) {
     bag.getMaterialMap().remove(id);
     } else {
     bag.getMaterialMap().put(id, currentCount - needCount);
     }

     Integer newCurrentCount = bag.getMaterialMap().get(petConfig.nextQualityId);
     if (newCurrentCount == null) {
     newCurrentCount = 0;
     }

     bag.getMaterialMap().put(petConfig.nextQualityId, newCurrentCount + count);
     idParam = new Int2Param();
     idParam.param1 = petConfig.nextQualityId;
     idParam.param2 = newCurrentCount + count;
     updateIds.add(idParam);
     pushUpdateBag(playerId, Collections.emptyList(), updateIds);
     cli.param = Response.SUCCESS;
     return cli;
     }
     **/
    /**
     * 分解
     *
     * @param playerId
     * @param id
     * @deprecated
     */
    /**
     public IntParam decompose(int playerId, int id) {
     IntParam cli = new IntParam();
     PetBag bag = getPetBag(playerId);
     if (!(bag.getPetMap().containsKey(id) || bag.getMaterialMap().containsKey(id))) {
     cli.param = Response.PET_NOT_EXIST;
     return cli;
     }

     List<Int2Param> updateIds = Lists.newArrayList();
     Pet pet = bag.getPetMap().get(id);
     LogConsume type;
     int configId = id;
     if (pet != null) { //宠物分解
     if (bag.getFightPetId() == id) {
     cli.param = Response.ERR_PARAM;
     return cli;
     }
     bag.getPetMap().remove(id);
     type = LogConsume.PET_DEC;
     configId = pet.getConfigId();
     } else { //碎片分解
     bag.getMaterialMap().remove(id);
     type = LogConsume.PET_MATERIAL_DEC;
     }
     //删除
     Int2Param delId = new Int2Param();
     delId.param1 = id;
     delId.param2 = 0;
     updateIds.add(delId);
     pushUpdateBag(playerId, Collections.emptyList(), updateIds);


     PetConfig petConfig = ConfigData.getConfig(PetConfig.class, configId);
     goodsService.addRewards(playerId, petConfig.decomposeGoods, type);
     cli.param = Response.SUCCESS;
     return cli;
     }
     **/

    /**
     * 变异
     *
     * @param playerId
     * @param mutateID
     * @param consume
     */
    /*
    public Int2Param mutate(int playerId, int mutateID, List<Int2Param> consume, int itemId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet mutatePet = bag.getPetMap().get(mutateID);
        if (mutatePet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }

        int total = 0;
        for (Int2Param param : consume) {
            Integer count = bag.getMaterialMap().get(param.param1);
            if (count == null) {
                count = 0;
            }
            if (param.param2 > count) {
                cli.param1 = Response.NO_MATERIAL;
                return cli;
            }
            total += param.param2;
        }

        PetConfig petConfig = ConfigData.getConfig(PetConfig.class, mutatePet.getConfigId());
        if (petConfig.variationNeedMaterialCount != total) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }

        int defaultRnd = 50;
        List<GoodsEntry> goodsEntries = Lists.newArrayList();
        if (itemId != 0) { //扣除强化道具
            goodsEntries.add(new GoodsEntry(itemId, 1));
            Integer rate = ConfigData.globalParam().petMutateItemRate.get(itemId);
            if (rate == null) {
                rate = 0;
            }
            defaultRnd = defaultRnd + rate;
        }
        for (int[] arr : petConfig.variationCost) {
            goodsEntries.add(new GoodsEntry(arr[0], arr[1]));
        }

        if (Response.SUCCESS != goodsService.decConsume(playerId, goodsEntries, LogConsume.CLEAR_LOCK)) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }

        consume(playerId, consume);

        int rand = RandomUtil.randInt(100);
        if (rand <= defaultRnd) { //变异成功
            cli.param1 = Response.SUCCESS;
            mutatePet.setConfigId(petConfig.mutateId);
            if (mutatePet.isMutateFlag()) {
                List<Integer> list = ConfigData.PassiveSkills.get(petConfig.quality);
                int newSkillId = list.get(RandomUtil.randInt(list.size()));
                mutatePet.setPassiveSkillId2(newSkillId);
                ServerLogger.info(JsonUtils.object2String(mutatePet));
            }
            mutatePet.setMutateFlag(true);

            calculator.calculate(playerId);
        } else {
            cli.param1 = 1;
        }

        int mutate = 0;
        for (Pet pet : bag.getPetMap().values()) {
            if (pet.getSkillID() != 0) {
                mutate++;
            }
        }
        taskService.doTask(playerId, Task.TYPE_MUTATE_PET, mutate);
        List<Pet> addPets = Lists.newArrayList(mutatePet);
        pushUpdateBag(playerId, addPets, Lists.newArrayList());
        cli.param2 = mutateID;
        return cli;
    }
    */

    /**
     * 提升品质
     *
     * @param playerId
     */
    public Int2Param improveQuality(int playerId, int petId, int approveGoodsId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet pet = bag.getPetMap().get(petId);
        if (pet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }

        PetConfig petConfig = ConfigData.getConfig(PetConfig.class, pet.getConfigId());
        Integer currentCount = bag.getMaterialMap().get(petConfig.materialId);
        if (currentCount == null) {
            currentCount = 0;
        }

        //是否升到最大阶
        if (petConfig.nextQualityId == 0) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }

        List<GoodsEntry> costs = Lists.newArrayList();
        for (int[] arr : petConfig.upgradeCost) {
            GoodsEntry e = new GoodsEntry(arr[0], arr[1]);
            costs.add(e);
        }

        int upgradeRate = petConfig.upgradeRate;
        if (approveGoodsId != 0) { //扣除药水
            costs.add(new GoodsEntry(approveGoodsId, 1));
            Integer rate = ConfigData.globalParam().petMutateItemRate.get(approveGoodsId);
            if (rate == null) {
                rate = 0;
            }
            upgradeRate += rate;
        }

        int ret = goodsService.decConsume(playerId, costs, LogConsume.PET_IMPROVE);
        if (Response.SUCCESS != ret) {
            cli.param1 = ret;
            return cli;
        }

        //进化成功率
        int rand = RandomUtil.randInt(100);
        if (rand <= upgradeRate) { //变异成功
            List<Pet> addPets = Lists.newArrayList();
            List<Int2Param> updateIds = Lists.newArrayList();

            PetConfig nextPet = ConfigData.getConfig(PetConfig.class, petConfig.nextQualityId);
            pet.setConfigId(petConfig.nextQualityId);

            int count = getQualityCount(nextPet.quality, bag.getPetMap());
            taskService.doTask(playerId, Task.TYPE_PET, nextPet.quality, count);

            if (pet.getPassiveSkillId() != 0) {
                PetSkillConfig skillConfig = ConfigData.getConfig(PetSkillConfig.class, pet.getPassiveSkillId());

                if (skillConfig != null && skillConfig.nextQualityId != 0)
                    pet.setPassiveSkillId(skillConfig.nextQualityId);
            }

            calculator.calculate(playerId);

            addPets.add(pet);
            pushUpdateBag(playerId, addPets, updateIds);
            cli.param1 = Response.SUCCESS;
            cli.param2 = petId;
        } else {
            cli.param1 = 1;
        }

        SessionManager.getInstance().sendMsg(CMD_IMPROVE, cli, playerId);
        return null;
    }

    /**
     * 出战
     *
     * @param playerId
     * @param petId
     */
    public Int2Param toFight(int playerId, int petId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet toFightPet = bag.getPetMap().get(petId);
        if (petId != 0 && toFightPet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }
        Player player = playerService.getPlayer(playerId);
        bag.setFightPetId(petId);
        if (bag.getShowPetId() == 0) {
            bag.setShowPetId(petId);
            PetChangeVO vo = new PetChangeVO();
            vo.playerId = playerId;
            Int2Param cli1 = new Int2Param();
            cli1.param1 = petId;
            if (toFightPet != null) {
                vo.name = toFightPet.getName();
                vo.petId = toFightPet.getShowConfigID();
                cli1.param2 = toFightPet.getShowConfigID();
            }
            SessionManager.getInstance().sendMsg(CMD_SHOW, cli1, playerId);
            sceneService.brocastToSceneCurLine(player, CMD_CHANGE, vo);
        }
        calculator.calculate(player);
        bag.updateFlag = true;
        cli.param1 = Response.SUCCESS;
        cli.param2 = petId;
        return cli;
    }


    /**
     * 展示
     *
     * @param playerId
     * @param petId
     */
    public Int2Param toShow(int playerId, int petId, int configId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet toShowPet = bag.getPetMap().get(petId);
        if (petId != 0 && toShowPet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }

        bag.setShowPetId(petId);
        bag.updateFlag = true;
        cli.param1 = petId;
        cli.param2 = configId;

        Player player = playerService.getPlayer(playerId);
        PetChangeVO vo = new PetChangeVO();
        vo.playerId = playerId;
        if (toShowPet != null) {
            toShowPet.setShowConfigID(configId);
            vo.name = toShowPet.getName();
            vo.petId = configId;
        }
        sceneService.brocastToSceneCurLine(player, CMD_CHANGE, vo);
        return cli;
    }

    /**
     * 更新宠物背包
     *
     * @param playerId
     * @param addPets  宠物列表
     */
    private void pushUpdateBag(int playerId, List<Pet> addPets, List<Int2Param> updateIds) {
        if (addPets.isEmpty() && updateIds.isEmpty()) {
            return;
        }
        UpdatePetBagVO vo = new UpdatePetBagVO();
        vo.pets = Lists.newArrayList();
        vo.updateIds = Lists.newArrayList(updateIds);
        for (Pet pet : addPets) {
            vo.pets.add(pet.toProto());
        }

        SessionManager.getInstance().sendMsg(CMD_UPDATE_BAG, vo, playerId);

        PetBag data = petBags.get(playerId);
        data.updateFlag = true;
    }

    public Pet getFightPet(int playerId) {
        PetBag bag = getPetBag(playerId);
        return bag.getPetMap().get(bag.getFightPetId());
    }

    public Pet getShowPet(int playerId) {
        PetBag bag = getPetBag(playerId);
        return bag.getPetMap().get(bag.getShowPetId());
    }

    /**
     * 宠物更名
     *
     * @param playerId
     * @param petId
     * @param name
     * @return
     */
    public void changeName(int playerId, int petId, String name) {
        PetBag bag = getPetBag(playerId);
        Pet pet = bag.getPetMap().get(petId);
        if (pet == null) {
            return;
        }
        if ("".equals(name)) {
            return;
        }
        pet.setName(name);

        List<Pet> addPets = Lists.newArrayList(pet);
        pushUpdateBag(playerId, addPets, Collections.EMPTY_LIST);

        PetChangeVO vo = new PetChangeVO();
        vo.playerId = playerId;
        vo.petId = petId;
        pet.setShowConfigID(pet.getShowConfigID());
        vo.name = pet.getName();
        Player player = playerService.getPlayer(playerId);
        sceneService.brocastToSceneCurLine(player, CMD_CHANGE, vo);
    }

    public void addAllPet(int playerId) {
        for (Object obj : GameData.getConfigs(PetConfig.class)) {
            PetConfig cfg = (PetConfig) obj;
            if (cfg.type == 1) {
                addPet(playerId, cfg.id);
            }
        }

        List<Pet> pets = new ArrayList<>();
        List<Int2Param> updateIds = Lists.newArrayList();
        PetBag bag = getPetBag(playerId);
        for (Pet pet : bag.getPetMap().values()) {
            PetConfig config = ConfigData.getConfig(PetConfig.class, pet.getConfigId());
            pets.add(pet);
        }
        pushUpdateBag(playerId, pets, updateIds);
    }
    ///////////////////////宠物玩法

    /**
     * 获取当前活动列表
     *
     * @param playerId
     */
    public PetGardenVO getPetActivity(int playerId) {
        PetGardenVO vo = new PetGardenVO();
        vo.activityCount = Lists.newArrayList();
        vo.activityList = Lists.newArrayList();
        PetBag bag = getPetBag(playerId);
        for (PetActivity pa : bag.getPetActivityMap().values()) {
            if (!pa.isbAward()) {
                vo.activityList.add(toProto(pa));
            }
        }

        for (Map.Entry<Integer, PetActivityData> e : bag.getPetActivityAttr().entrySet()) {
            Int2Param param = new Int2Param();
            param.param1 = e.getKey();
            param.param2 = e.getValue().getTotalCount();
            vo.activityCount.add(param);
        }
        ServerLogger.info(JsonUtils.object2String(vo));
        return vo;
    }


    private PetActivityVO toProto(PetActivity pa) {
        PetActivityVO vo = new PetActivityVO();
        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, pa.getId());
        vo.id = pa.getId();
        int dec = 0;
        vo.petId = Lists.newArrayList();
        for (PetKV kv : pa.getPets()) {
            vo.petId.add(kv.getPetId());
            PetConfig petConfig = ConfigData.getConfig(PetConfig.class, kv.getPetConfigId());
            PetActivityConfig petActivityConfig = ConfigData.getConfig(PetActivityConfig.class, pa.getId());
            dec = dec + petActivityConfig.plotAcclerate[petConfig.quality - 1];
        }
        vo.remainTime = pa.getRemainTime(config.finishSec - dec);
        return vo;
    }

    /**
     * 开始某个活动
     *
     * @param playerId
     * @param startPetActivityVO
     */
    public IntParam startPetActivity(int playerId, StartPetActivityVO startPetActivityVO) {
        IntParam param = new IntParam();
        PetBag bag = getPetBag(playerId);

        int count = 0;
        int type = 0;
        Set<Integer> petSet = Sets.newHashSet();
        for (PetPlayData playData : startPetActivityVO.petActivitys) { //校验
            int activityId = playData.activityId;
            PetActivity petActivity = bag.getPetActivityMap().get(activityId);
            if (petActivity != null && !petActivity.isbAward()) {
                param.param = Response.PET_ACTIVITY_DOING;
                return param;
            }

            for (int petId : playData.petIds) {
                Pet pet = bag.getPetMap().get(petId);
                if (pet == null) {
                    param.param = Response.PET_NOT_EXIST;
                    return param;
                }

                if (pet.isPlayFlag()) {
                    param.param = Response.PET_PLAYING;
                    return param;
                }
                if (petSet.contains(petId)) {
                    param.param = Response.PET_ACTIVITY_SAME;
                    return param;
                }
                petSet.add(petId);
            }
            PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, activityId);
            PetActivityData petActivityData = bag.getPetActivityData(config.type);
            int curLevel = getCurrLevel(config.type, petActivityData.getTotalCount());
            if (config.level > curLevel) {
                param.param = Response.PET_ACTIVITY_NOT_OPEN;
                return param;
            }
            if (petActivityData.getMaxCount() < config.count) {
                petActivityData.setMaxCount(config.count);
            }
            if (petActivityData.getDoingCount() + count >= petActivityData.getMaxCount()) { //超过当前可以并行活动个数
                param.param = Response.PET_ACTIVITY_DOING;
                return param;
            }
            count += 1;

            type = config.type;
        }
        petSet.clear();
        PetActivityData petActivityData = bag.getPetActivityAttr().get(type);
        petActivityData.setDoingCount(petActivityData.getDoingCount() + count);

        for (PetPlayData playData : startPetActivityVO.petActivitys) { //开始活动
            int activityId = playData.activityId;
            PetActivity petActivity = bag.getPetActivityMap().get(activityId);
            if (petActivity == null) {
                petActivity = new PetActivity();
                petActivity.setId(activityId);
                bag.getPetActivityMap().put(activityId, petActivity);
            }

            for (int petId : playData.petIds) {
                Pet pet = bag.getPetMap().get(petId);
                pet.setPlayFlag(true);
                PetKV kv = new PetKV();
                kv.setPetId(petId);
                kv.setPetConfigId(pet.getConfigId());
                petActivity.getPets().add(kv);
            }
            petActivity.setStartTime(System.currentTimeMillis());
            petActivity.setbAward(false);

        }
        bag.updateFlag = true;
        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 开始某个活动
     *
     * @param playerId
     * @param activityId
     */
    public IntParam finishPetActivity(int playerId, int activityId) {
        PetBag bag = getPetBag(playerId);
        IntParam param = new IntParam();
        //扣除资源
        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, activityId);
        PetActivity petActivity = bag.getPetActivityMap().get(activityId);
        if (petActivity.getStartTime() == 0) {
            param.param = Response.ERR_PARAM;
            return param;
        }
        int dec = 0;
        for (PetKV kv : petActivity.getPets()) {
            PetConfig petConfig = ConfigData.getConfig(PetConfig.class, kv.getPetConfigId());
            PetActivityConfig petActivityConfig = ConfigData.getConfig(PetActivityConfig.class, petActivity.getId());
            dec += petActivityConfig.plotAcclerate[petConfig.quality - 1];
        }

        int remain = petActivity.getRemainTime(config.finishSec - dec);
        int min = remain / 60 + (remain % 60 == 0 ? 0 : 1);
        int needDiamond = min * config.finishCostPerMins[1];
        if (needDiamond != 0 && !playerService.decDiamond(playerId, needDiamond, LogConsume.CLEAR_LOCK, activityId)) {
            param.param = Response.ERR_PARAM;
            return param;
        }
        bag.updateFlag = true;
        petActivity.setStartTime(0);
        param.param = Response.SUCCESS;
        return param;
    }

    private int getCurrLevel(int type, int count) {
        int level = 1;
        for (Object obj : GameData.getConfigs(PetActivityConfig.class)) {
            PetActivityConfig cfg = (PetActivityConfig) obj;
            if (type == cfg.type) {
                if (cfg.levelUpCondition != 0 && count >= cfg.levelUpCondition) {
                    if (level < cfg.level + 1) {
                        level = cfg.level + 1;
                    }
                }
            }
        }
        return level;
    }

    /**
     * 领取活动奖励
     *
     * @param playerId
     * @param activityId
     */
    public PetGetRewardVO getPetActivityRewards(int playerId, int activityId) {
        PetBag bag = getPetBag(playerId);
        PetGetRewardVO param = new PetGetRewardVO();
        param.rewards = Lists.newArrayList();
        PetActivity petActivity = bag.getPetActivityMap().get(activityId);
        if (petActivity.isbAward()) {
            param.errCode = Response.HAS_TAKE_REWARD;
            return param;
        }
        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, activityId);
        int dec = 0;
        for (PetKV kv : petActivity.getPets()) {
            PetConfig petConfig = ConfigData.getConfig(PetConfig.class, kv.getPetConfigId());
            dec += config.plotAcclerate[petConfig.quality - 1];
        }

        if (!petActivity.checkFinish(config.finishSec - dec)) {
            param.errCode = Response.ERR_PARAM;
            return param;
        }

        List<Reward> list = Lists.newArrayList();
        for (PetKV kv : petActivity.getPets()) {
            Pet pet = bag.getPetMap().get(kv.getPetId());
            pet.setPlayFlag(false);
            PetConfig petConfig = ConfigData.getConfig(PetConfig.class, kv.getPetConfigId());
            int rate = config.rate[petConfig.quality - 1];
            if (RandomUtil.randInt(100) < rate) {
                List<Reward> dropRewards = randomRewardService.getRewards(playerId, config.dropId, config.dropCount, LogConsume.PET_ACTIVITY_REWARD);
                if (dropRewards != null) {
                    list.addAll(dropRewards);
                }
            }
        }

        petActivity.setStartTime(0);
        petActivity.setbAward(true);
        petActivity.getPets().clear();
        PetActivityData data = bag.getPetActivityData(config.type);
        data.setTotalCount(data.getTotalCount() + 1);
        taskService.doTask(playerId, Task.TYPE_PET_ACTIVITY, activityId, data.getTotalCount());
       /* if (config.levelUpCondition != 0 && data.getTotalCount() >= config.levelUpCondition) {
            data.setLevel(config.level + 1);
            ServerLogger.info(JsonUtils.object2String(data));
        }*/

        if (data.getDoingCount() > 0) {
            data.setDoingCount(data.getDoingCount() - 1);
        }


        List<GoodsEntry> goodsEntries = Lists.newArrayList();
        for (int[] arr : config.rewards) {
            Reward reward = new Reward();
            reward.id = arr[0];
            reward.count = arr[1];
            list.add(reward);
        }
        for (int[] arr : config.rewards) {
            goodsEntries.add(new GoodsEntry(arr[0], arr[1]));
        }

        bag.updateFlag = true;
        param.rewards.addAll(list);
        ServerLogger.info(JsonUtils.object2String(param));
        goodsService.addRewards(playerId, goodsEntries, LogConsume.PET_ACTIVITY_REWARD);
        param.errCode = Response.SUCCESS;
        return param;
    }

    public void gmAddPetPlayTimes(int playerId, int type, int count) {
        PetBag bag = getPetBag(playerId);
        PetActivityData petActivityData = bag.getPetActivityData(type);
        petActivityData.setTotalCount(petActivityData.getTotalCount() + count);
    }
}
