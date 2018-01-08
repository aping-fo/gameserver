package com.game.module.pet;

/**
 * Created by lucky on 2018/1/3.
 */
public class PetActivity {
    private int id;//配置ID
    private long startTime; //开始时间
    private int petId;
    private boolean awardFlag;

    public boolean isAwardFlag() {
        return awardFlag;
    }

    public void setAwardFlag(boolean awardFlag) {
        this.awardFlag = awardFlag;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean checkFinish(int sec) {
        return System.currentTimeMillis() - startTime >= sec * 100;
    }

    public int getRemainTime(int sec) {
        int pass = (int) (System.currentTimeMillis() - startTime) / 1000;
        int remain = sec - pass;
        return remain < 0 ? 0 : remain;
    }
}
