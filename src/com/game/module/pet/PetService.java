package com.game.module.pet;

import com.game.data.PetActivityConfig;
import com.game.data.PetConfig;
import com.game.data.PetSkillConfig;
import com.game.data.Response;
import com.game.event.InitHandler;
import com.game.module.RandomReward.RandomRewardService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.Reward;
import com.game.params.pet.*;
import com.game.util.*;
import com.google.common.collect.Lists;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lucky on 2017/9/13.
 * 排位赛
 */
@Service
public class PetService implements InitHandler {
    private static final int CMD_IMPROVE = 7007;
    private static final int CMD_TO_FIGHT = 7008;
    private static final int CMD_UPDATE_BAG = 7003;
    private static final int CMD_CHANGE = 7009;
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

    private Map<Integer, PetBag> petBags = new ConcurrentHashMap<>();
    private List<Integer> skillIds = Lists.newArrayList();

    @Override
    public void handleInit() {
        for (Object obj : GameData.getConfigs(PetSkillConfig.class)) {
            PetSkillConfig cfg = (PetSkillConfig) obj;
            if (cfg.type == 2) {
                skillIds.add(cfg.skillId);
            }
        }
    }

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
        if (checkSamePet(playerId, configID)) { //分解碎片
            addPetMaterial(playerId, configID, 1, true);
            return;
        }
        PetConfig newPetConfig = ConfigData.getConfig(PetConfig.class, configID);
        List<Pet> pets = new ArrayList<>();
        List<Int2Param> updateIds = Lists.newArrayList();
        Pet pet = new Pet(bag.idGen.incrementAndGet(), newPetConfig.id, newPetConfig.activeSkillId);
        bag.getPetMap().put(pet.getId(), pet);
        pets.add(pet);
        pushUpdateBag(playerId, pets, updateIds);
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

        PetConfig newPetConfig = ConfigData.getConfig(PetConfig.class, petConfig.petId);
        Pet pet = new Pet(bag.idGen.incrementAndGet(), newPetConfig.id, newPetConfig.activeSkillId);
        bag.getPetMap().put(pet.getId(), pet);
        List<Pet> pets = Lists.newArrayList(pet);

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

    /**
     * 分解
     *
     * @param playerId
     * @param id
     * @deprecated
     */
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

    /**
     * 变异
     *
     * @param playerId
     * @param mutateID
     * @param consume
     */
    public Int2Param mutate(int playerId, int mutateID, List<Int2Param> consume, int itemId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet mutatePet = bag.getPetMap().get(mutateID);
        if (mutatePet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }
        Pet consumePet = bag.getPetMap().get(mutateID);
        if (consumePet == null) {
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

        PetConfig petConfig = ConfigData.getConfig(PetConfig.class, mutateID);
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

        List<Int2Param> updateIds = consume(playerId, consume);

        int rand = RandomUtil.randInt(100);
        if (rand >= defaultRnd) { //变异成功
            mutatePet.setMutateFlag(true);
            mutatePet.setConfigId(petConfig.mutateId);
            int newSkillId = skillIds.get(RandomUtil.randInt(skillIds.size()));
            if (mutatePet.getPassiveSkillId() == 0) {
                mutatePet.setPassiveSkillId(newSkillId);
            } else {
                mutatePet.setPassiveSkillId2(newSkillId);
            }
        }

        List<Pet> addPets = Lists.newArrayList(mutatePet);
        pushUpdateBag(playerId, addPets, updateIds);
        cli.param1 = Response.SUCCESS;
        cli.param2 = mutateID;
        return cli;
    }


    /**
     * 提升品质
     *
     * @param playerId
     */
    public Int2Param improveQuality(int playerId, int petId) {
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

        if (currentCount < petConfig.nextQualityMaterialCount) {
            cli.param1 = Response.PET_MATERIAL_NOT_ENOUGH;
            return cli;
        }

        //是否升到最大阶
        if (petConfig.nextQualityId == 0) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }

        List<GoodsEntry> costs = Lists.newArrayList();
        for (int[] arr : petConfig.nextQualityCost) {
            GoodsEntry e = new GoodsEntry(arr[0], arr[1]);
            costs.add(e);
        }

        int ret = goodsService.decConsume(playerId, costs, LogConsume.PET_IMPROVE);
        if (Response.SUCCESS != ret) {
            cli.param1 = ret;
            return cli;
        }

        List<Pet> addPets = Lists.newArrayList();
        List<Int2Param> updateIds = Lists.newArrayList();

        PetConfig nextPet = ConfigData.getConfig(PetConfig.class, petConfig.nextQualityId);
        pet.setConfigId(petConfig.nextQualityId);
        pet.setSkillID(nextPet.activeSkillId);

        if (pet.getPassiveSkillId() != 0) {
            pet.setPassiveSkillId(pet.getPassiveSkillId() + 1);
        }

        if (pet.getPassiveSkillId2() != 0) {
            pet.setPassiveSkillId2(pet.getPassiveSkillId2() + 1);
        }

        bag.getMaterialMap().put(petConfig.materialId, currentCount - petConfig.nextQualityMaterialCount);
        if (currentCount == petConfig.nextQualityMaterialCount) {
            bag.getMaterialMap().remove(petConfig.materialId);
        }

        //减少的
        Int2Param delId = new Int2Param();
        delId.param1 = petConfig.materialId;
        delId.param2 = currentCount - petConfig.nextQualityMaterialCount;
        updateIds.add(delId);

        addPets.add(pet);
        pushUpdateBag(playerId, addPets, updateIds);
        cli.param1 = Response.SUCCESS;
        cli.param2 = petId;
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
            int fightId = toFightPet == null ? 0 : toFightPet.getConfigId();
            vo.petId = fightId;
            boolean bMutate = toFightPet == null ? false : toFightPet.isMutate();
            vo.hasMutate = bMutate;
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
    public Int2Param toShow(int playerId, int petId) {
        Int2Param cli = new Int2Param();
        PetBag bag = getPetBag(playerId);
        Pet toShowPet = bag.getPetMap().get(petId);
        if (petId != 0 && toShowPet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }

        bag.setShowPetId(petId);
        bag.updateFlag = true;
        cli.param1 = Response.SUCCESS;
        cli.param2 = petId;

        Player player = playerService.getPlayer(playerId);
        PetChangeVO vo = new PetChangeVO();
        vo.playerId = playerId;
        int fightId = toShowPet == null ? 0 : toShowPet.getConfigId();
        vo.petId = fightId;
        boolean bMutate = toShowPet == null ? false : toShowPet.isMutate();
        vo.hasMutate = bMutate;
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
            if (!pa.isAwardFlag()) {
                vo.activityList.add(toProto(pa));
            }
        }
        for (Map.Entry<Integer, Integer> e : bag.getActivityCount().entrySet()) {
            for (Object obj : GameData.getConfigs(PetActivityConfig.class)) {
                PetActivityConfig cfg = (PetActivityConfig) obj;
                if (e.getKey() == cfg.type && (cfg.levelUpCondition != 0 && e.getValue() <= cfg.levelUpCondition)) {
                    Int2Param param = new Int2Param();
                    param.param1 = cfg.id;
                    param.param2 = e.getValue();
                    vo.activityCount.add(param);
                    break;
                }
            }
        }
        return vo;
    }

    private PetActivityVO toProto(PetActivity pa) {
        PetActivityVO vo = new PetActivityVO();
        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, pa.getId());
        vo.id = pa.getId();
        int dec = 0;
        if (pa.getPetId() != 0) {
            PetConfig petConfig = ConfigData.getConfig(PetConfig.class, pa.getPetId());
            PetActivityConfig petActivityConfig = ConfigData.getConfig(PetActivityConfig.class, pa.getId());
            dec = petActivityConfig.plotAcclerate[petConfig.quality - 1];
        }
        vo.remainTime = pa.getRemainTime(config.finishSec - dec);
        vo.petId = pa.getPetId();
        return vo;
    }

    /**
     * 开始某个活动
     *
     * @param playerId
     * @param activityId
     */
    public IntParam startPetActivity(int playerId, int activityId, int petId) {
        IntParam param = new IntParam();
        PetBag bag = getPetBag(playerId);

        if (bag.getPetActivityMap().containsKey(activityId)) {
            param.param = Response.PET_ACTIVITY_DOING;
            return param;
        }

        Pet pet = bag.getPetMap().get(petId);
        if (petId != 0 && pet == null) {
            param.param = Response.PET_NOT_EXIST;
            return param;
        }

        if (pet != null && pet.isPlayFlag()) {
            param.param = Response.PET_PLAYING;
            return param;
        }

        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, activityId);
        Integer count = bag.getActivityCount().get(config.type);
        if (count == null) {
            count = 0;
        }
        int needCount = 0;
        if (config.level != 1 && count < needCount) { //不够次数
            param.param = Response.PET_ACTIVITY_NOT_OPEN;
            return param;
        }

        if (pet != null) {
            pet.setPlayFlag(true);
        }
        PetActivity petActivity = bag.getPetActivityMap().get(activityId);
        if (petActivity == null) {
            petActivity = new PetActivity();
            petActivity.setId(activityId);
            bag.getPetActivityMap().put(activityId, petActivity);
        }

        petActivity.setStartTime(System.currentTimeMillis());
        petActivity.setPetId(petId);
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
        if (!playerService.decDiamond(playerId, config.finishCostPerMins[1], LogConsume.CLEAR_LOCK, activityId)) {
            param.param = Response.ERR_PARAM;
            return param;
        }
        petActivity.setStartTime(0);
        param.param = Response.SUCCESS;
        return param;
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
        if (petActivity.isAwardFlag()) {
            param.errCode = Response.HAS_TAKE_REWARD;
            return param;
        }

        int dec = 0;
        PetActivityConfig config = ConfigData.getConfig(PetActivityConfig.class, activityId);
        PetConfig petConfig = ConfigData.getConfig(PetConfig.class, petActivity.getPetId());
        if (petActivity.getPetId() != 0) {
            if (petConfig != null) {
                dec = config.plotAcclerate[petConfig.quality - 1];
            }
        }

        if (!petActivity.checkFinish(config.finishSec - dec)) {
            param.errCode = Response.ERR_PARAM;
            return param;
        }

        List<Reward> list = Lists.newArrayList();
        if (petActivity.getPetId() != 0) {
            Pet pet = bag.getPetMap().get(petActivity.getPetId());
            pet.setPlayFlag(false);
            int rate = config.rate[petConfig.quality - 1];
            if (RandomUtil.randInt(100) < rate) {
                list.addAll(randomRewardService.getRewards(playerId, config.dropId[petConfig.quality - 1], 5, LogConsume.PET_ACTIVITY_REWARD));
            }
        }

        Integer count = bag.getActivityCount().get(config.type);
        if (count == null) {
            count = 0;
        }
        bag.getActivityCount().put(config.type, count + 1);

        List<GoodsEntry> goodsEntries = Lists.newArrayList();
        for (int[] arr : config.rewards) {
            Reward reward = new Reward();
            reward.id = arr[0];
            reward.count = arr[1];
            list.add(reward);
        }
        for (Reward reward : list) {
            goodsEntries.add(new GoodsEntry(reward.id, reward.count));
        }
        param.rewards.addAll(list);
        goodsService.addRewards(playerId, goodsEntries, LogConsume.PET_ACTIVITY_REWARD);
        param.errCode = Response.SUCCESS;
        return param;
    }
}
