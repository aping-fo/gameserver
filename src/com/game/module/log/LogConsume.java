package com.game.module.log;

/**
 *  消耗品类型定义
 */
public enum LogConsume {
	
	CHARGE_ADD(1000,"充值赠送"),
	CHARGE(1001,"充值"),
	GM(1002,"GM"),
	TASK_REWARD(1003,"任务奖励"),
	VIP_BAG_COST(1004,"vip礼包购买"),
	VIP_BAG(1005,"vip礼包奖励"),
	VIP_DAILY_REWARD(1006,"vip每日奖励"),
	VIP_MONTH_CARD(1007,"vip月卡奖励"),
	VIP_FUN(1008,"vip基金"),
	USE_TOOL(1009,"使用物品获得奖励"),
	CREATE_GANG(1010,"创建帮派开销"),
	GANG_DONATE(1011,"帮派捐赠"),
	COPY_ENERGY(1012,"副本体力"),
	COPY_REWARD(1013,"副本奖励"),
	REVIVE(1014,"复活消耗"),
	RESET_COPY(1015,"重置副本"),
	THREE_STAR(1016,"三星奖励"),
	DECOMPOSE_DEC(1017,"分解装备扣除"),
	DECOMPOSE_ADD(1018,"分解装备获得"),
	UP_STAR_COST(1019,"升星消耗"),
	STRENGTH_COST(1020,"强化消耗"),
	JEWEL_UP_COST(1021,"升级宝石消耗"),
	CLEAR_LOCK(1022,"洗练锁定消耗"),
	CLEAR_COST(1023,"洗练消耗"),
	SKILL_UPGRADE(1024,"升级技能"),
	COMPOSE_ARTIFACT_COST(1025,"合成神器部件消耗"),
	COMPOSE_ARTIFACT(1026,"合成神器部件"),
	SHOP_BUY_COST(1027,"商城购买消耗"),
	SHOP_BUY_ADD(1028,"商城购买"),
	SHOP_REFRESH_COST(1029,"商城刷新"),
	ENDLESS_CLEAR(1030, "无尽漩涡立即结束扫荡"),
	ENDLESS_RANK_REWARD(1031, "无尽漩涡排行榜奖励"),
	ARENA_REWARD(1032, "竞技场奖励"),
	BUY_TREASURE_TIME(1033, "购买金币副本次数"),
	QUICK_TREASURE(1034, "快速完成金币副本"),
	SWEEP_COPY(1035, "副本扫荡"),
	BUY_ARENA_CHALLENGE(1036, "购买AI竞技场挑战次数"),
	ARENA_RANK_REWARD(1037, "AI竞技场排名奖励"),
	EXPRIENCE_REWARD(1038, "英雄试练奖励"),
	GANG_RENAME(1039, "重命名公会"),
	GANG_BROCAST(1040, "公会广播"),
	BUY_EXPRIENCE_TIME(1041, "购买经验副本次数"),
	QUICK_EXPRIENCE(1042, "快速完成经验副本"),
	STRENGTH_COST_BACK(1043,"强化返还"),
	TASK_LIVENESS_REWARD(1044, "任务活跃奖奖励"),
	TRAVERSING_COPY(1045, "特性副本"),
	ACTIVE_FASHION(1046,"激活时装"),
	GANG_TRAINING_REWARD(1047,"公会练功房奖励"),
	LOTTERY_REQUEST(1048,"抽奖材料"),
	LOTTERY_REWARD(1049,"抽奖奖励"),
	SIGN_REWARD(1050,"签到奖励"),
	WORLD_BOSS_REWARD(1051,"世界BOSS奖励"),
	WORLD_BOSS_BUY(1052,"世界BOSS购买"),
	WORLD_BOSS_KILL(1053,"世界BOSS击杀奖励"),
	WORLD_BOSS_LAST_BEAT(1054,"世界BOSS最后一击奖励"),
	WORLD_HORN(1055,"喇叭"),
	SQ_UP(1056,"神器升阶"),
	QUICK_LEADAWAY(1057, "快速完成顺手牵羊副本"),
	BUY_LEADAWAY_TIME(1058, "购买顺手牵羊副本次数"),
	LADDER_AWARD(1059, "排位赛奖励"),
	QUICK_GOLD(1060, "快速完成金币副本价格"),
	GUILD_POINT(1061, "公会贡献"),
	GUILD_BOSS(1062, "公会BOSS奖励"),
	GUILD_OPEN_BOSS(1063, "开启公会副本活动"),
	GUILD_COPY_REWARD(1064, "公会副本活动阶段奖励"),
	BUY_ENERGY(1065, "购买体力"),
	BUY_COIN(1066, "购买金币"),
	ITEM_COMPOUND(1067, "物品合成"),
	PET_MATERIAL_DEC(1068, "宠物碎片分解"),
	PET_DEC(1069, "宠物分解"),
	PET_IMPROVE(1070, "宠物分解"),
	BAG_INIT(1071, "初始装备"),
	ACTIVITY_REWARD(1072, "活动奖励领取"),
	ACTIVITY_RE_REWARD(1073, "活动补奖励领取"),
	ACTIVITY_OPEN(1074, "活动开启"),
	PET_ACTIVITY(1075, "宠物玩法立即完成"),
	PET_ACTIVITY_REWARD(1076, "宠物玩法奖励领取"),
	ACHIEVEMENT_GET_ALL(1077, "成就一键领取"),
	OPEN_BOX(1078, "开箱子"),
	ACTIVATIONCODE_GIFTBAG(1079, "激活码礼包"),
	SKILL_CARD_MAKE(1080, "技能卡合成"),
	AWAKENING_SKILL(1081, "觉醒技能升级"),
	BUY_COPY_TIMES(1082, "购买主线副本"),
	FACEBOOK_INVITE(1083, "facebook邀请"),
	ACTIVITY_CONSUME(1084, "活动任务消耗"),
	ZERO_GIFTBAG(1085, "0元礼包赠送"),
	FASHION_UPGRADE(1086, "时装升阶"),
	DestinyCard(1087, "命运卡牌"),
	RecoveryGoods(1088, "物品回收"),
	STAR_COST(1089,"升星消耗")
	;
	public int actionId;// id
	public String desc;

	private LogConsume(int actionId, String desc) {
		this.actionId = actionId;
		this.desc = desc;
	}
	
	public static LogConsume getLog(int actionId){
		for(LogConsume log:LogConsume.values()){
			if(log.actionId==actionId){
				return log;
			}
		}
		return null;
	}
	
	public static void main(String[] args){
		for(LogConsume log:LogConsume.values()){
			System.out.println(String.format("insert into paid_items values('%d','%s','0','%s');",log.actionId, log.toString(), log.desc));
		}
	}
}
