package com.game.module.goods;


import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.Long2Param;
import com.game.params.LongList;
import com.game.params.LongParam;
import com.game.params.goods.CTool;
import com.server.anotation.Command;
import com.server.anotation.Extension;

@Extension
public class BagExtension {

	@Autowired
	private GoodsService goodsService;
	@Autowired
	private EquipService equipService;
	@Autowired
	private PlayerService playerService;

	// 获得背包信息
	@Command(1201)
	public Object getGoodsInfo(int playerId, Object param) {
		return goodsService.getAllGoods(playerId);
	}
	
	//获取物品信息
	public static final int UPDATE_EQUP = 1202;
	@Command(1202)
	public Object getEquipInfo(int playerId,Object param){
		return equipService.getEquip(playerId);
	}

	// 穿上
	@Command(1203)
	public Object wear(int playerId, LongParam param) {
		long id = param.param;
		int result = equipService.wear(playerId, id);
		IntParam code = new IntParam();
		code.param = result;
		return code;
	}

	// 脱下
	@Command(1204)
	public Object putOff(int playerId, LongParam param) {
		long id = param.param;
		int result = equipService.putOff(playerId, id);
		IntParam code = new IntParam();
		code.param = result;
		return code;
	}
	public static final int GOODS_UPDATE = 1205;
	
	//使用物品
	@Command(1206)
	public Object use(int playerId, CTool tool) {
		return goodsService.useTool(playerId, tool.id, tool.count);
	}
	
	//分解
	@Command(1207)
	public Object decopose(int playerId,LongList ids){
		return equipService.decompose(playerId, ids.lList);
	}
	
	//升星
	@Command(1208)
	public Object upStar(int playerId,LongParam id){
		IntParam result = new IntParam();
		result.param = equipService.upStar(playerId, id.param);
		return result;
	}
	
	//强化
	@Command(1209)
	public Object strength(int playerId,Int2Param type){
		IntParam result = new IntParam();
		result.param = equipService.strength(playerId, type.param1, type.param2==1);
		return result;
	}
	
	//升级宝石
	@Command(1210)
	public Object upJewel(int playerId,Int2Param jewel){
		IntParam result = new IntParam();
		result.param = equipService.upJewel(playerId, jewel.param1);
		return result;
	}
	
	//洗练
	@Command(1211)
	public Object clear(int playerId,Long2Param goods){
		return equipService.clear(playerId, goods.param1,(int)goods.param2);
	}
	
	//替换
	@Command(1212)
	public Object replace(int playerId,LongParam id){
		IntParam result = new IntParam();
		result.param = equipService.replace(playerId, id.param);
		return result;
	}

	//出售
	@Command(1213)
	public Object sell(int playerId,Int2Param param){
		IntParam result = new IntParam();
		result.param = goodsService.sell(playerId, param.param1,param.param2);
		return result;
	}

	//合成
	@Command(1214)
	public Object compound(int playerId,IntParam param){
		return  goodsService.compound(playerId, param.param);
	}

	//合成
	@Command(1215)
	public Object lockItem(int playerId,Int2Param param){
		return  goodsService.lockItem(playerId, param.param1,param.param2);
	}

	@Command(1216)
	public Object getOtherEquips(int playerId,IntParam param){
		return  goodsService.getOtherEquips(playerId, param.param);
	}
}
