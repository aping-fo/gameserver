package com.game.module.fame;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.FameConfig;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.player.Upgrade;
import com.game.params.FameVo;
import com.game.params.ListParam;
import com.game.util.ConfigData;
import com.server.SessionManager;

/**
 * 声望系统
 */
@Service
public class FameService {

	@Autowired
	private PlayerService playerService;

	// 获取声望数据
	public ListParam<FameVo> getInfo(int playerId) {
		PlayerData data = playerService.getPlayerData(playerId);
		ListParam<FameVo> info = new ListParam<FameVo>();
		info.params = new ArrayList<FameVo>(data.getFames().size());
		for (Entry<Integer, Upgrade> fame : data.getFames().entrySet()) {
			FameVo vo = new FameVo();
			vo.camp = fame.getKey();
			vo.lev = fame.getValue().getLev();
			vo.exp = fame.getValue().getExp();
			info.params.add(vo);
		}
		return info;
	}

	// 刷新数据
	public void refresh(int playerId) {
		SessionManager.getInstance().sendMsg(FameExtension.GET_INFO, getInfo(playerId), playerId);
	}

	// 添加声望
	public void addFame(int playerId,  int camp,int fame) {
		if (fame <= 0) {
			return;
		}
		PlayerData data = playerService.getPlayerData(playerId);
		Upgrade fameData = data.getFames().get(camp);
		if (fameData == null) {
			fameData = new Upgrade();
			fameData.setLev(1);
			data.getFames().put(camp, fameData);
		}
		fameData.setExp(fameData.getExp()+fame);
		while (true) {
			FameConfig cfg = ConfigData.getConfig(FameConfig.class, camp * 1000 + fameData.getLev());
			if (cfg.exp > fameData.getExp()) {
				break;
			}
			int nextId = camp * 1000 + fameData.getLev() + 1;
			FameConfig nextCfg = ConfigData.getConfig(FameConfig.class, nextId);
			if (nextCfg == null) {
				break;
			}
			fameData.setLev(fameData.getLev() + 1);
			fameData.setExp(cfg.exp - fameData.getExp());
		}
		refresh(playerId);
	}
}
