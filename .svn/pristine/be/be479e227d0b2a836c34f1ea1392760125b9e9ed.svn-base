package com.game.module.goods;

import java.util.ArrayList;
import java.util.List;

import com.game.params.goods.AttrItem;

public class Goods {
	
	public static int EXP = 101;	// 经验
	public static int DIAMOND = 102;	// 钻石
	public static int COIN = 103;	// 金币
	public static int CONTRIBUTE = 104;//帮贡
	public static int ENERGY = 107;	// 体力
	public static int EQUIP_TOOL = 108;//装备分解材料
	public static int SPECIAL_MAP = 109;//特性副本地图
	public static int VIP_EXP = 120;//vip经验值
	public static int SKILL_CARD = 121;//技能卡
	public static int FAME = 122;//声望值
	public static int EXPERIENCE_HP = 123;//英雄试练HP
	public static int TRAVERSING_ENERGY = 124;//穿越仪能量
	
	public static int ARTIFACT_COMPONENT = 401;//神器部件
	public static int FASHION = 501;//时装道具
	
	public static int CURRENCY = 104;//通用货币类型

	
	public static int GOOODS = 20;	// 物品
	public static int BOTTLE = 503;//药瓶

	public static final int EQUIP = 1;
	public static final int BAG = 0;
	
	public static final int HP = 1;
	public static final int ATK=2;
	public static final int DEF=3;
	public static final int FU=4;
	public static final int SYMPTOM = 5;
	public static final int CRIT=6;
	
	public static final int QUALITY_WHITE = 1;
	public static final int QUALITY_GREEN = 2;
	public static final int QUALITY_BLUE = 3;
	public static final int QUALITY_VIOLET = 4;
	public static final int QUALITY_ORANGE = 5;
	
	
	private long id;
	private int playerId;
	private int goodsId;
	private int stackNum;// 当前堆叠数量
	private int storeType;// 存储位置 0背包1角色身上
	
	private int star;//星级
	
	private List<AttrItem> addAttrList;//附加属性
	private List<AttrItem> lastAttrs;//上次洗练出来的
	
	public Goods(int playerId,int goodsId,int stackNum,int storeType){
		this.playerId = playerId;
		this.goodsId = goodsId;
		this.stackNum = stackNum;
		this.storeType = storeType;
		this.star = 0;
		
		addAttrList = new ArrayList<AttrItem>(2);
		lastAttrs = new ArrayList<AttrItem>(2);
	}
	
	public Goods(){}
	
	public boolean isInBag(){
		return storeType == BAG;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}

	public int getStoreType() {
		return storeType;
	}

	public void setStoreType(int storeType) {
		this.storeType = storeType;
	}

	public int getStackNum() {
		return stackNum;
	}

	public void setStackNum(int stackNum) {
		this.stackNum = stackNum;
	}

	public List<AttrItem> getAddAttrList() {
		return addAttrList;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public void setAddAttrList(List<AttrItem> addAttrList) {
		this.addAttrList = addAttrList;
	}

	public List<AttrItem> getLastAttrs() {
		return lastAttrs;
	}

	public void setLastAttrs(List<AttrItem> lastAttrs) {
		this.lastAttrs = lastAttrs;
	}
}
