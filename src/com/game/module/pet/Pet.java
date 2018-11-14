package com.game.module.pet;

import com.game.params.pet.PetVO;

/**
 * Created by lucky on 2017/10/25.
 */
public class Pet {
    private int id;
    private int configId;
    private int showConfigID;
    private int passiveSkillId;
    private boolean playFlag;
    private String name;
    private byte[] data;
    private int playerId;

    public Pet() {
    }

    public Pet(int id, int configId, int passiveSkillId, String name) {
        this.id = id;
        this.passiveSkillId = passiveSkillId;
        this.configId = configId;
        this.playFlag = false;
        this.name = name;
        this.showConfigID = configId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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

    public int getPassiveSkillId() {
        return passiveSkillId;
    }

    public void setPassiveSkillId(int passiveSkillId) {
        this.passiveSkillId = passiveSkillId;
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
        vo.passiveSkillId = passiveSkillId;
        vo.configId = configId;
        vo.name = name;
        return vo;
    }
}
