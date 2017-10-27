package com.game.module.pet;

import com.game.params.pet.PetVO;

/**
 * Created by lucky on 2017/10/25.
 */
public class Pet {
    private int id;
    private int skillID;
    private int passiveSkillId;

    public Pet() {
    }

    public Pet(int id, int skillID, int passiveSkillId) {
        this.id = id;
        this.skillID = skillID;
        this.passiveSkillId = passiveSkillId;
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
        return vo;
    }
}
