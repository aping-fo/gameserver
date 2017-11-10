package com.game.module.pet;

import com.game.data.PetConfig;
import com.game.data.Response;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneExtension;
import com.game.module.scene.SceneService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.pet.PetBagVO;
import com.game.params.pet.PetChangeVO;
import com.game.params.pet.UpdatePetBagVO;
import com.game.util.CompressUtil;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.game.util.JsonUtils;
import com.google.common.collect.Lists;
import com.server.SessionManager;
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
public class PetService {
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
        List<Pet> pets = new ArrayList<>();
        List<Int2Param> updateIds = Lists.newArrayList();
        Pet pet = new Pet(bag.idGen.incrementAndGet(), newPetConfig.id, newPetConfig.activeSkillId);
        bag.getPetMap().put(pet.getId(), pet);
        pets.add(pet);
        pushUpdateBag(playerId, pets, updateIds);
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
     * @param consumeID
     */
    public Int2Param mutate(int playerId, int mutateID, int consumeID, int newSkillID) {
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

        if (mutateID == consumeID) {
            cli.param1 = Response.ERR_PARAM;
            return cli;
        }
        mutatePet.setPassiveSkillId(newSkillID);
        mutatePet.setMutateFlag(true);

        bag.getPetMap().remove(consumeID);
        List<Int2Param> updateIds = Lists.newArrayList();
        Int2Param delId = new Int2Param();
        delId.param1 = consumeID;
        delId.param2 = 0;
        updateIds.add(delId);
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
        for (int i = 0; i < petConfig.nextQualityCost.length; i += 2) {
            GoodsEntry e = new GoodsEntry(petConfig.nextQualityCost[i], petConfig.nextQualityCost[i + 1]);
            costs.add(e);
        }

        int ret = goodsService.decConsume(playerId, costs, LogConsume.PET_IMPROVE);
        if (Response.SUCCESS != ret) {
            cli.param1 = ret;
            return cli;
        }

        bag.getPetMap().remove(petId);
        List<Pet> addPets = Lists.newArrayList();
        List<Int2Param> updateIds = Lists.newArrayList();

        PetConfig nextPet = ConfigData.getConfig(PetConfig.class, petConfig.nextQualityId);
        pet.setConfigId(nextPet.id);
        pet.setSkillID(nextPet.activeSkillId);

        if (pet.getPassiveSkillId() != 0) {
            pet.setPassiveSkillId(pet.getPassiveSkillId() + 1);
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
        if (bag.getFightPetId() == petId) {
            Int2Param vo = toFight(playerId, petConfig.nextQualityId);
            SessionManager.getInstance().sendMsg(CMD_TO_FIGHT, vo, playerId);
        }
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
        if (toFightPet == null) {
            cli.param1 = Response.PET_NOT_EXIST;
            return cli;
        }

        bag.setFightPetId(petId);
        bag.updateFlag = true;
        cli.param1 = Response.SUCCESS;
        cli.param2 = petId;

        Player player = playerService.getPlayer(playerId);
        PetChangeVO vo = new PetChangeVO();
        vo.playerId = playerId;
        vo.petId = toFightPet.getConfigId();
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
}
