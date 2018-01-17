package com.game.module.pet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.game.params.Int2Param;
import com.game.params.pet.PetBagVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/10/25.
 */
public class PetBag {
    @JsonIgnore
    public final AtomicInteger idGen = new AtomicInteger();
    @JsonIgnore
    public volatile boolean updateFlag;

    //宠物  <configID,pet>
    private Map<Integer, Pet> petMap = new HashMap<>();
    //材料 <configID -- material>
    private Map<Integer, Integer> materialMap = new HashMap<>();

    private int fightPetId;

    private int showPetId;
    //宠物玩法，活动ID --- 活动
    private Map<Integer, PetActivity> petActivityMap = Maps.newHashMap();
    private Map<Integer, PetActivityData> petActivityAttr = Maps.newHashMap();

    public Map<Integer, PetActivityData> getPetActivityAttr() {
        return petActivityAttr;
    }

    public PetActivityData getPetActivityData(int type) {
        PetActivityData data = petActivityAttr.get(type);
        if (data == null) {
            data = new PetActivityData();
            data.setLevel(1);
            petActivityAttr.put(type, data);
        }
        return data;
    }

    public void setPetActivityAttr(Map<Integer, PetActivityData> petActivityAttr) {
        this.petActivityAttr = petActivityAttr;
    }

    public Map<Integer, PetActivity> getPetActivityMap() {
        return petActivityMap;
    }

    public void setPetActivityMap(Map<Integer, PetActivity> petActivityMap) {
        this.petActivityMap = petActivityMap;
    }

    public int getShowPetId() {
        return showPetId;
    }

    public void setShowPetId(int showPetId) {
        this.showPetId = showPetId;
    }

    public int getFightPetId() {
        return fightPetId;
    }

    public void setFightPetId(int fightPetId) {
        this.fightPetId = fightPetId;
    }

    public Map<Integer, Pet> getPetMap() {
        return petMap;
    }

    public void setPetMap(Map<Integer, Pet> petMap) {
        this.petMap = petMap;
    }

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public void setMaterialMap(Map<Integer, Integer> materialMap) {
        this.materialMap = materialMap;
    }

    public PetBagVO toProto() {
        PetBagVO vo = new PetBagVO();
        vo.pets = Lists.newArrayList();
        vo.materials = Lists.newArrayList();
        vo.fightPetId = fightPetId;
        vo.showPetId = showPetId;
        for (Map.Entry<Integer, Integer> s : materialMap.entrySet()) {
            Int2Param materialVo = new Int2Param();
            materialVo.param1 = s.getKey();
            materialVo.param2 = s.getValue();
            vo.materials.add(materialVo);
        }

        for (Pet pet : petMap.values()) {
            vo.pets.add(pet.toProto());
        }

        return vo;
    }
}
