package com.game.module.player;

import com.game.SysConfig;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class Player implements PlayerAddition {

    public final static int EXP = 1;    // 经验
    public final static int DIAMOND = 2;    // 钻石
    public final static int COIN = 3;    // 金币
    public final static int CONTRIBUTE = 4;//帮贡
    public final static int ENERGY = 5;    // 体力
    public final static int EQUIP_TOOL = 6;//装备分解材料
    public final static int VIP_EXP = 7;//vip经验值
    public final static int VIP_LEV = 8;//vip等级
    public final static int LEV = 9;//等级
    public final static int LIVENESS = 10;//活跃度
    public final static int TRAVERSING_ENERGY = 11;//穿越仪能量
    public final static int ACHIEVEMENT = 12;//成就

    public int clientType;
    public String clientMac;
    public String hardwarSn1;
    public String hardwarSn2;
    public String uddi;
    public String modelVersion;
    public String ldid;
    public String adid;
    public int clientIp;
    public int clientPort;
    public String token;
    public String openId;
    public boolean online;
    public int userId = 0;
    public long onlineTime;
    public boolean bCrateRole;
    public boolean bTraversing;
    //who can see me
    public final Set<Integer> seeMeSet = Sets.newConcurrentHashSet();

    private int playerId;
    private String name;
    private String accName;// 平台账号
    private int sex;// 性别 1男2女
    private int vocation;// 职业
    private int exp;// 经验
    private int lev;
    private int coin;// 铜钱
    private int diamond;// 钻石
    private int totalCoin;//累计获得金币
    private int totalDiamond;//累计获得钻石
    private int chargeDiamond;// 充值的数量
    private int vip;// vip等级
    private int contribute;//帮贡;
    private String channel;//渠道
    private int achievement; //成就
    private int energy;// 体力

    private int serverId;// 服务器id
    private Date regTime;// 注册时间
    private Date lastLoginTime;// 上次登录时间
    private Date lastLogoutTime;// 上次退出时间
    private String ip;// ip

    private int sceneId;
    private float x;
    private float y;
    private float z;

    private int hMoveDir;
    private int vMoveDir;

    private int lastSceneId;
    private float[] lastPos = new float[3];
    private int fight;//战斗力
    private int fashionId;//时装id
    private int weaponId;//武器id

    private int gangId;//帮派id
    private long energyTime;//体力恢复时间

    private int attack;
    private int defense;
    private int hp;
    private int symptom;
    private int crit;
    private int fu;
    private int title;

    // 其他临时数据
    private volatile int subLine; // 分线id
    private long lastSaveTime;
    private int copyId;
    private int teamId;


    private int groupId;
    private int groupTeamId;
    private int roomId;
    public int roomTeamId;
    private int totalChargeMoney;//累计充值金额

    public String getClientMac() {
        return clientMac;
    }

    public void setClientMac(String clientMac) {
        this.clientMac = clientMac;
    }

    private List<Integer> attrList = new ArrayList<>(12);

    public List<Integer> getAttrList() {
        return attrList;
    }

    public void setAttrList(List<Integer> attrList) {
        this.attrList = attrList;
    }

    public int getTotalChargeMoney() {
        return totalChargeMoney;
    }

    public void setTotalChargeMoney(int totalChargeMoney) {
        this.totalChargeMoney = totalChargeMoney;
    }

    public int getAchievement() {
        return achievement;
    }

    public void setAchievement(int achievement) {
        this.achievement = achievement;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupTeamId() {
        return groupTeamId;
    }

    public void setGroupTeamId(int groupTeamId) {
        this.groupTeamId = groupTeamId;
    }

    private boolean refresh;//临时变量，设置这个值为true时，更新属性不同步到前端

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getVocation() {
        return vocation;
    }

    public void setVocation(int vocation) {
        this.vocation = vocation;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public int getTotalCoin() {
        return totalCoin;
    }

    public void setTotalCoin(int totalCoin) {
        this.totalCoin = totalCoin;
    }

    public int getTotalDiamond() {
        return totalDiamond;
    }

    public void setTotalDiamond(int totalDiamond) {
        this.totalDiamond = totalDiamond;
    }

    public int getChargeDiamond() {
        return chargeDiamond;
    }

    public void setChargeDiamond(int chargeDiamond) {
        this.chargeDiamond = chargeDiamond;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getContribute() {
        return contribute;
    }

    public void setContribute(int contribute) {
        this.contribute = contribute;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(Date lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int gethMoveDir() {
        return hMoveDir;
    }

    public void sethMoveDir(int hMoveDir) {
        this.hMoveDir = hMoveDir;
    }

    public int getvMoveDir() {
        return vMoveDir;
    }

    public void setvMoveDir(int vMoveDir) {
        this.vMoveDir = vMoveDir;
    }

    public int getLastSceneId() {
        return lastSceneId;
    }

    public void setLastSceneId(int lastSceneId) {
        this.lastSceneId = lastSceneId;
    }

    public float[] getLastPos() {
        return lastPos;
    }

    public void setLastPos(float[] lastPos) {
        this.lastPos = lastPos;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public int getFashionId() {
        return fashionId;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    public int getGangId() {
        return gangId;
    }

    public void setGangId(int gangId) {
        this.gangId = gangId;
    }

    public long getEnergyTime() {
        return energyTime;
    }

    public void setEnergyTime(long energyTime) {
        this.energyTime = energyTime;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getSymptom() {
        return symptom;
    }

    public void setSymptom(int symptom) {
        this.symptom = symptom;
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
    }

    public int getFu() {
        return fu;
    }

    public void setFu(int fu) {
        this.fu = fu;
    }

    public int getSubLine() {
        return subLine;
    }

    public void setSubLine(int subLine) {
        this.subLine = subLine;
    }

    public long getLastSaveTime() {
        return lastSaveTime;
    }

    public void setLastSaveTime(long lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    @Override
    public void addHp(int add) {
        this.hp += add;
    }

    @Override
    public void addAttack(int add) {
        this.attack += add;
    }

    @Override
    public void addDefense(int add) {
        this.defense += add;
    }

    @Override
    public void addCrit(int add) {
        this.crit += add;
    }

    @Override
    public void addFu(int add) {
        this.fu += add;
    }

    @Override
    public void addSymptom(int add) {
        this.symptom += add;
    }

    public int getLev() {
        return lev;
    }

    public void setLev(int lev) {
        this.lev = lev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getCopyId() {
        return copyId;
    }

    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean checkHurt(int hurt) {
        return checkHurt(hurt, 1);
    }

    public boolean checkHurt(int hurt, float factor) {
        if (SysConfig.gm) {
            return true;
        }
        int maxHurt = (int) ((fight * 0.06 * 1.65 * 3 * 2.05 + 4185) * factor);
        return hurt <= maxHurt;
    }
}
