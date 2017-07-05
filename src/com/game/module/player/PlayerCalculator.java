package com.game.module.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.data.ArtifactCfg;
import com.game.data.EquipJewelCfg;
import com.game.data.EquipStarCfg;
import com.game.data.EquipStrengthCfg;
import com.game.data.FashionCollectCfg;
import com.game.data.GoodsConfig;
import com.game.data.PlayerUpgradeCfg;
import com.game.module.goods.Goods;
import com.game.module.goods.GoodsService;
import com.game.module.goods.PlayerBag;
import com.game.params.goods.AttrItem;
import com.game.util.CommonUtil;
import com.game.util.ConfigData;

@Service
public class PlayerCalculator {
	
	@Autowired
	private PlayerService playerService;
	@Autowired
	private GoodsService goodsService;

	// 重新计算人物属性
	public void calculate(int playerId) {
		calculate(playerService.getPlayer(playerId));
	}

	// 重新计算人物的属性
	public void calculate(Player player) {
		synchronized (player) {
			initPlayer(player);
			updateAttr(player);
			// 通知前端
			playerService.refreshPlayerToClient(player.getPlayerId());
		}
	}

	// 初始化人物的各种属性
	public void initPlayer(Player player) {
		PlayerUpgradeCfg attr = ConfigData.getConfig(PlayerUpgradeCfg.class, player.getLev());
		player.setHp(attr.hp);
		player.setAttack(attr.attack);
		player.setDefense(attr.defense);
		player.setSymptom(attr.symptom);
		player.setFu(attr.symptom);
		player.setCrit(attr.crit);
	}

	// 计算属性加成
	public void updateAttr(Player player) {
		addEquip(player);
		addJewel(player);
		addArtifact(player);
		AddFashion(player);
		//百分比保持最后
		addPercent(player);
		// 更新战斗力
		float[] fightParams = ConfigData.globalParam().fightParams;
		float fight = player.getHp()*fightParams[0]+player.getAttack()*fightParams[1]+player.getDefense()*fightParams[2]+player.getFu()*fightParams[3]+player.getSymptom()*fightParams[4]+
				player.getCrit()*fightParams[5]; 
		player.setFight((int)fight);
		playerService.refreshPlayerToClient(player.getPlayerId());
	}

	// 增加装备属性
	private void addEquip(PlayerAddition player) {
		PlayerBag bag = goodsService.getPlayerBag(player.getPlayerId());
		PlayerData data = playerService.getPlayerData(player.getPlayerId());
		
		for(Goods g:bag.getAllGoods().values()){
			if(g.isInBag()){
				continue;
			}
			GoodsConfig cfg = ConfigData.getConfig(GoodsConfig.class, g.getGoodsId());
			if(!CommonUtil.contain(ConfigData.globalParam().equipTypes, cfg.type)){
				continue;
			}
			int hp = cfg.hp;
			int attack = cfg.attack;
			int defense = cfg.defense;
			int crit = cfg.crit;
			int fu = cfg.fu;
			int symptom = cfg.symptom;
			int starId = cfg.type*100000+cfg.level*100+g.getStar();
			EquipStarCfg star = ConfigData.getConfig(EquipStarCfg.class, starId);
			if(star!=null){
				hp+=star.hp;
				attack+=star.attack;
				defense +=star.defense;
				crit +=star.crit;
				fu +=star.fu;
				symptom +=star.symptom;
			}
			Integer strengthLev = data.getStrengths().get(cfg.type);
			if(strengthLev==null){
				strengthLev = 0;
			}
			EquipStrengthCfg strength = ConfigData.getConfig(EquipStrengthCfg.class, cfg.type*1000+strengthLev);
			if(strength!=null){
				hp+=(int)(hp*strength.add*0.01f);
				attack+=(int)(attack*strength.add*0.01f);
				defense+=(int)(defense*strength.add*0.01f);
				crit+=(int)(crit*strength.add*0.01f);
				fu+=(int)(fu*strength.add*0.01f);
				symptom+=(int)(symptom*strength.add*0.01f);
			}
			player.addAttack(attack);
			player.addCrit(crit);
			player.addDefense(defense);
			player.addFu(fu);
			player.addHp(hp);
			player.addSymptom(symptom);
		}
	}
	
	//宝石
	private  void addJewel(PlayerAddition player){
		PlayerData data = playerService.getPlayerData(player.getPlayerId());
		for(Entry<Integer, Jewel> entry:data.getJewels().entrySet()){
			Jewel jewel = entry.getValue();
			int type = entry.getKey();
			if(jewel.getLev()==0){
					continue;
				}
				int id = type*1000+jewel.getLev();
				EquipJewelCfg cfg = ConfigData.getConfig(EquipJewelCfg.class, id);
				if(cfg == null){
					continue;
				}
				player.addAttack(cfg.attack);
				player.addCrit(cfg.crit);
				player.addDefense(cfg.defense);
				player.addFu(cfg.fu);
				player.addHp(cfg.hp);
				player.addSymptom(cfg.symptom);
			}
	}
	
	//加时装战力，非百分比的部分
	private void AddFashion(PlayerAddition player){
		PlayerData data = playerService.getPlayerData(player.getPlayerId());
		int count = data.getFashions().size();
		FashionCollectCfg cfg = ConfigData.getConfig(FashionCollectCfg.class, count);
		if(cfg==null){
			return;
		}
		player.addAttack(cfg.attack);
		player.addCrit(cfg.crit);
		player.addDefense(cfg.defense);
		player.addFu(cfg.fu);
		player.addSymptom(cfg.symptom);
	}
	
	//处理神器
	private void addArtifact(PlayerAddition player){
		PlayerData data = playerService.getPlayerData(player.getPlayerId());
		if(data.getArtifacts().isEmpty()){
			return;
		}
		for(Entry<Integer,int[]> artifact:data.getArtifacts().entrySet()){
			int id = artifact.getKey();
			int[] components = artifact.getValue();
			int activeCount = 0;
			for(int c:components){
				if(c==1){
					activeCount++;
				}
			}
			if(activeCount==0){
				continue;
			}
			ArtifactCfg cfg = ConfigData.getConfig(ArtifactCfg.class, id);
			for(int i=0;i<activeCount;i++){
				addAttrValue(player, cfg.attrs[i][0], cfg.attrs[i][1]);
			}
		}
	}
	
	//百分比的（要统一放在这里处理，先累加所有的百分比，再计算原始值+原始值%)
	public void addPercent(PlayerAddition player){
		//装备的特殊属性
		HashMap<Integer, Integer> percentAttrs = new HashMap<Integer, Integer>();
		PlayerBag bag = goodsService.getPlayerBag(player.getPlayerId());
		for(Goods g:bag.getAllGoods().values()){
			if(g.isInBag()){
				continue;
			}
			for(AttrItem attr:g.getAddAttrList()){
				if(attr.type>6){
					continue;
				}
				addPercentAttr(percentAttrs, attr.type, attr.value);
			}
		}
		//时装的百分比
		PlayerData data = playerService.getPlayerData(player.getPlayerId());
		FashionCollectCfg collect = ConfigData.getConfig(FashionCollectCfg.class, data.getFashions().size());
		if(collect!=null){
			addPercentAttr(percentAttrs, Goods.ATK, collect.attackPercent);
			addPercentAttr(percentAttrs, Goods.DEF, collect.defensePercent);
			addPercentAttr(percentAttrs, Goods.CRIT, collect.critPercent);
			addPercentAttr(percentAttrs, Goods.SYMPTOM, collect.symptomPercent);
			addPercentAttr(percentAttrs, Goods.FU, collect.fuPercent);
		}
		
		for(Entry<Integer, Integer> attr:percentAttrs.entrySet()){
			addAttrValuePercent(player, attr.getKey(), attr.getValue());
		}
	}
	
	private void addPercentAttr(Map<Integer,Integer> data,int type,int value){
		Integer curPercent = data.get(type);
		if(curPercent==null){
			curPercent = 0;
		}
		curPercent += value;
		data.put(type, curPercent);
	}
	
	private void addAttrValuePercent(PlayerAddition player, int type, int valuePercent) {
		switch (type) {
		case Goods.HP:
			player.addHp((int) (player.getHp() * valuePercent * 0.01));
			break;
		case Goods.ATK:
			player.addAttack((int) (player.getAttack() * valuePercent * 0.01));
			break;
		case Goods.DEF:
			player.addDefense((int) (player.getDefense() * valuePercent * 0.01));
			break;
		case Goods.CRIT:
			player.addCrit((int) (player.getCrit() * valuePercent * 0.01));
			break;
		case Goods.FU:
			player.addFu((int) (player.getFu() * valuePercent * 0.01));
			break;
		case Goods.SYMPTOM:
			player.addSymptom((int) (player.getSymptom() * valuePercent * 0.01));
			break;
		}
	}
	
	private void addAttrValue(PlayerAddition player,int type,int value){
		switch (type) {
		case Goods.HP:
			player.addHp(value);
			break;
		case Goods.ATK:
			player.addAttack(value);
			break;
		case Goods.DEF:
			player.addDefense(value);
			break;
		case Goods.CRIT:
			player.addCrit(value);
			break;
		case Goods.FU:
			player.addFu(value);
			break;
		case Goods.SYMPTOM:
			player.addSymptom(value);
			break;
		}
	}

}
