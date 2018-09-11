package com.game.module.rank;

import com.game.params.rank.StateRankVO;

public class StateRank {
    private String name;//玩家名称
    private int vocation;//职业
    private int playerId;//玩家ID
    private int head;//时装头部
    private int fashionId;//时装衣服
    private int weapon;//时装武器
    private int level;//等级
    private String gang;//公会
    private int fightingValue;//战力
    private int vip;//vip
    private int title;//称号
    private int rankType;//排行榜类型

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVocation() {
        return vocation;
    }

    public void setVocation(int vocation) {
        this.vocation = vocation;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getFashionId() {
        return fashionId;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public int getWeapon() {
        return weapon;
    }

    public void setWeapon(int weapon) {
        this.weapon = weapon;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getGang() {
        return gang;
    }

    public void setGang(String gang) {
        this.gang = gang;
    }

    public int getFightingValue() {
        return fightingValue;
    }

    public void setFightingValue(int fightingValue) {
        this.fightingValue = fightingValue;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }

    public StateRankVO toProto(){
        StateRankVO vo = new StateRankVO();
        vo.name = name;//玩家名称
        vo.vocation = vocation;//职业
        vo.playerId =playerId;//玩家ID
        vo.head = head;//时装头部
        vo.fashionId =fashionId;//时装衣服
        vo.weapon = weapon;//时装武器
        vo.level = level;//等级
        vo.gang = gang;//公会
        vo.fightingValue = fightingValue;//战力
        vo.vip = vip;//vip
        vo.title = title;//称号
        vo.rankType = rankType; // 排行榜类型1、战力，2、等级，3、排位赛
        return vo;
    }
}
