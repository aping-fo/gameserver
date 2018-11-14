package com.game.module.task;

public class Task{
	/**
1通关指定副本
    **/
	
	public static final int TYPE_JOINT = 1;
	public static final int TYPE_DAILY = 2;
	public static final int TYPE_CHELLENGE = 3;
	public static final int TYPE_TASK = 4;
	public static final int TYPE_GANG = 5;
	
	public static final int FINISH_TRANSIT = 1;//通关[ID,类型,星级,次数]
	public static final int FINISH_KILL = 2;//击杀[类型,ID,次数]
	public static final int FINISH_WEAR = 3;//装备[品质,次数]
	public static final int FINISH_STRONG = 4;//强化[次数]/[等级,装备类型,个数]
	public static final int FINISH_DECOMPOSE = 5;//分解[次数]
	public static final int FINISH_STONE = 6;//宝石[次数]/[等级,装备类型,个数]
	public static final int FINISH_SKILL = 7;//技能[等级,卡等级,次数]
	public static final int FINISH_CARD_UPGRADE = 8;//卡牌升级[次数]/[等级,个数]
	public static final int FINISH_CONSUME = 9;//货币消耗[类型,数量]
	public static final int FINISH_CURRENCY = 10;//货币(累计)[类型,数量]
	public static final int FINISH_BUY_POWER = 11;//购买体力[次数]
	public static final int FINISH_STAR = 12;//升星[次数]/[等级,数量]
	public static final int FINISH_CLEAR = 13;//洗练[次数]
	public static final int FINISH_CARD_COMPOSE = 14;//卡牌合成[次数]/[品质,个数]
	public static final int FINISH_JOIN_PK = 15;//参加竞技[类型,次数]类型:1-AI竞技场;2-排位赛
	public static final int FINISH_ARTIFACT = 16;//神器解密[次数]
	public static final int FINISH_DONATE = 17;//公会捐献[类型,次数]
	public static final int FINISH_ENDLESS = 18;//无尽漩涡[层数]
	public static final int FINISH_LOTTERY = 19;//抽奖[类型,次数]

	//=================成就
	public static final int TYPE_PASS_COPY_SINGLE = 20; //通关副本[副本id,次数] m表示个人还是组队，个人为
	public static final int TYPE_PASS_COPY_TEAM = 21; //通关副本[副本id,次数] m表示个人还是组队，
	public static final int TYPE_SWIPE_COPY = 22; //扫荡副本[副本id,次数]
	public static final int TYPE_HIT= 23; //副本受击次数[副本id,受击次数]  表示在副本受击次数小于给定值
	public static final int TYPE_PASS_TIME = 24; //通关时间[副本id,时间] 时间单位是秒，表示通关时间小于给定值
	public static final int TYPE_LEADER_PASS = 25; //.作为团长带领通关[副本id] 只能是时空仪组队和团队副本
	public static final int TYPE_TEAM_INVITE = 26; //.组队邀请
	public static final int TYPE_GROUP_INVITE = 27; //团队邀请
	public static final int TYPE_HIDE = 28; //躲避
	public static final int TYPE_ARENA_WINS = 29; //竞技场连胜
	public static final int TYPE_ARENA_TIMES = 30; //挑战竞技场[次数]
	public static final int TYPE_LADDER = 31; //.获得段位[段位id]
	public static final int TYPE_TRAIN_TIMES = 32; //挑战英雄试练[次数]
	public static final int TYPE_TRAIN_WIN_TIMES = 33; //通关英雄试练[次数]（挑战成功所有的关卡）
	public static final int TYPE_FRIEND_COUNT = 34; //34.拥有好友[数量]
	public static final int TYPE_CHAT = 35; //频道发言[频道id,次数]
	public static final int TYPE_SQ = 36; //解锁完整神器[神器id]
	public static final int TYPE_SQ_UP = 37; //升阶神器[阶数]
	public static final int TYPE_FAME = 38; //声望崇拜[声望id,声望值]
	public static final int TYPE_PET = 39; //宠物
	public static final int TYPE_MUTATE_PET = 40; //变异宠物[个数]
	public static final int TYPE_PET_ACTIVITY = 41;//宠物活动[活动id,次数]
	public static final int TYPE_SKILL_CARD_COUNT = 42;//获得卡片[数量]
	public static final int TYPE_SKILL_LEVEL = 43;//技能升级[技能id,级数]
	public static final int TYPE_FASH_COUNT = 44;//拥有时装[时装数量]
	public static final int TYPE_SUIT = 45;//拥有时装[时装数量]
	public static final int TYPE_BS_LEVEL = 46;//宝石升阶[部位id，阶数]
	public static final int TYPE_FIGHT = 47;//宝石升阶[部位id，阶数]
	public static final int TYPE_TITLE = 48;//收集称号[数量] 称号收集数量大于给定值可以获得成就
	public static final int TYPE_SHOP_BUY_COUNT = 49;//购买商品[次数]
	public static final int TYPE_TASK_COUNT = 50;//完成任务[个数]
	public static final int TYPE_LOTTERY_COUNT = 51;//抽卡品质[品质，次数]抽到x品质的次数
	public static final int TYPE_SIGN = 52;//签到[次数]
	public static final int TYPE_ENERGY = 53;//领取体力[次数]
	public static final int TYPE_GANG_LEVEL = 54;//公会等级[等级]
	public static final int TYPE_GANG_TEC = 55;//.公会科技开启上限[等级]
	public static final int TYPE_GANG_RANK = 56;//公会排名[名次]
	public static final int TYPE_ACHIEVEMENT_RANK = 57;//成就[名次]
	public static final int TYPE_FIGHT_RANK = 58;//等级[级数]
	public static final int TYPE_LEVEL = 59;//等级[级数]
	public static final int TYPE_ARENA_RANK = 60;//竞技场排名[名次]
	public static final int TYPE_LADDER_RANK = 61;//排位赛排名[名次]
	public static final int TYPE_WB_RANK = 62;//世界boss伤害排名[名次]
	public static final int ACHIEVEMENT_WB_LAST = 63;//世界BOSS最后一击[等级]
	public static final int ACHIEVEMENT_VIP = 64;//VIP等级[等级]
	public static final int TYPE_KILL= 65;//击杀怪物[怪物类型，只数]
	public static final int TYPE_GUILD_COPY= 66;//公会副本
	public static final int TYPE_INVITATE= 66;//发送世界频道邀请玩家通关副本[副本id]
	public static final int TYPE_PASS_TYPE_COPY = 67; //通关一类型副本[副本类型,次数]
	public static final int TYPE_PET_ANY_ACTIVITY = 68; //参加任意活动

	public static final int STATE_INIT = 0;//未接(等级不够)
	public static final int STATE_ACCEPTED = 1;//已接
	public static final int STATE_FINISHED = 2;//完成
	public static final int STATE_SUBMITED = 3;//已提交

	private int playerId;
	private int taskId;
	private volatile int state;
	private volatile int count;
	private volatile int type;
	private byte[] data;

	public Task(){}
	
	
	public Task(int taskId, int state,int type){
		this.taskId = taskId;
		this.state = state;
		this.type = type;

		if(type == TYPE_HIT) {
			count = Integer.MAX_VALUE;
		}
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public void alterCount(int value){
		this.count += value;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
