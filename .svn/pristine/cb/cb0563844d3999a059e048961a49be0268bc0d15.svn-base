package com.game.module.shop;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.server.anotation.Command;
import com.server.anotation.Extension;

/**  
 * 商城
 */
@Extension
public class ShopExtension {

	@Autowired
	private ShopService service;
	@Autowired
	private PlayerService playerService;
	
	//获取信息
	@Command(1701)
	public Object getInfo(int playerId,IntParam type){
		return service.getInfo(playerId, type.param);
	}
	
	//购买
	@Command(1702)
	public Object buy(int playerId,Int2Param shop){
		IntParam result = new IntParam();
		result.param = service.buy(playerId, shop.param1, shop.param2);
		return result;
	}
	
	//刷新
	@Command(1703)
	public Object refresh(int playerId,IntParam type){
		IntParam result = new IntParam();
		result.param = service.refresh(playerId, type.param);
		return result;
	}
	
	@Command(1704)
	public Object mysteryTime(int playerId, Object obj){
		IntParam result = new IntParam();
		PlayerData data = playerService.getPlayerData(playerId);
		long time = data.getMysteryShopTime() + ConfigData.globalParam().mysteryShopTime * TimeUtil.ONE_MIN - System.currentTimeMillis();
		if(time < 0){
			time = 0;
		}
		result.param = (int)time;
		return result;
	}
}
