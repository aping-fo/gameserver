package com.game.module.pet;

import com.game.params.pet.PetVO;

/**
 * Created by lucky on 2017/10/25.
 */
public class Pet {
    private int id;
    private int configId;
    private int showConfigID;
    private int skillID;
    private int passiveSkillId;
    private int passiveSkillId2;
    private boolean mutateFlag;
    private boolean playFlag;
    private String name;

    public Pet() {
    }

    public Pet(int id, int configId, int passiveSkillId, String name) {
        this.id = id;
        this.passiveSkillId = passiveSkillId;
        this.configId = configId;
        this.mutateFlag = false;
        this.playFlag = false;
        this.name = name;
        this.showConfigID = configId;
    }

    public int getShowConfigID() {
        return showConfigID;
    }

    public void setShowConfigID(int showConfigID) {
        this.showConfigID = showConfigID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlayFlag() {
        return playFlag;
    }

    public void setPlayFlag(boolean playFlag) {
        this.playFlag = playFlag;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public boolean isMutateFlag() {
        return mutateFlag;
    }

    public void setMutateFlag(boolean mutateFlag) {
        this.mutateFlag = mutateFlag;
    }

    public int getPassiveSkillId() {
        return passiveSkillId;
    }

    public void setPassiveSkillId(int passiveSkillId) {
        this.passiveSkillId = passiveSkillId;
    }

    public int getSkillID() {
        return skillID;
    }

    public void setSkillID(int skillID) {
        this.skillID = skillID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PetVO toProto() {
        PetVO vo = new PetVO();
        vo.id = id;
        vo.skillId = skillID;
        vo.passiveSkillId = passiveSkillId;
        vo.hasMutate = mutateFlag;
        vo.configId = configId;
        vo.passiveSkillId2 = passiveSkillId2;
        vo.name = name;
        return vo;
    }

    public int getPassiveSkillId2() {
        return passiveSkillId2;
    }

    public void setPassiveSkillId2(int passiveSkillId2) {
        this.passiveSkillId2 = passiveSkillId2;
    }

    public boolean isMutate() {
        return this.passiveSkillId != 0;
    }
}
