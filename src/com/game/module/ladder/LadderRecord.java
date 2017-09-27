package com.game.module.ladder;

/**
 * Created by lucky on 2017/9/21.
 */
public class LadderRecord {
    private int type; //类型
    private int result; //胜利还是失败1:胜利 0：失败
    private String otherName; //对方姓名
    private int score; //积分变化
    private int level; //变更后段位

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
