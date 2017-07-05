package com.test;


import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.util.BeanManager;
import com.game.util.JsonUtils;
import com.game.util.StopWatch;

public class PlayerDataTest {

	public static void main(String[] args) {
		BaseTest.init();
		try{
		JsonUtils.string2Object(null, null);
		}catch(Exception e){
			
		}
		
		PlayerService playerService = BeanManager.getBean(PlayerService.class);
		
		//playerService.initPlayerData(1001);
		StopWatch.start();
//		Player player = playerService.getPlayer(1001);
//		StopWatch.stop();
		PlayerData data = playerService.getPlayerData(1001);
		data.getDailyData().put(1, 1);
		playerService.updatePlayerData(1001);
		
	}

}
