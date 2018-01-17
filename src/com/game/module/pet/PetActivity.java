package com.game.module.pet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucky on 2018/1/3.
 */
public class PetActivity {
    private int id;//配置ID
    private long startTime; //开始时间
    private List<PetKV> pets = new ArrayList<>();
    private boolean bAward;

    public boolean isbAward() {
        return bAward;
    }

    public void setbAward(boolean bAward) {
        this.bAward = bAward;
    }

    public List<PetKV> getPets() {
        return pets;
    }

    public void setPets(List<PetKV> pets) {
        this.pets = pets;
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
        return System.currentTimeMillis() - startTime >= sec * 1000;
    }

    public int getRemainTime(int sec) {
        int pass = (int) ((System.currentTimeMillis() - startTime) / 1000);
        int remain = sec - pass;
        return remain < 0 ? 0 : remain;
    }
}
