package com.game.module.robot;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.RobotNames;
import com.game.SysConfig;
import com.game.data.GlobalConfig;
import com.game.data.PlayerUpgradeCfg;
import com.game.event.InitHandler;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.util.ConfigData;
import com.game.util.RandomUtil;

@Service
public class RobotService implements InitHandler {
    public List<Integer> getRobots() {
        return new ArrayList<Integer>(robots);
    }

    @Autowired
    private PlayerService playerService;
    @Autowired
    private SerialDataService serialDataService;

    @Autowired
    private PlayerDao playerDao;
    private List<Integer> robots;

    private GlobalConfig globalParam;

    @Override
    public void handleInit() {
        globalParam = ConfigData.globalParam();
    }

    public void addRobot() throws Exception {
        robots = new ArrayList<>();
        if (!serialDataService.getData().isInitRobot()) {
            RobotNames.init();
            List<String> names = RobotNames.getRobotNames();
            int total = names.size();

            int minFight = ConfigData.globalParam().robotFight[0];
            int maxFight = ConfigData.globalParam().robotFight[1];
            int tempMaxFight=(maxFight-minFight)/total;//递减最大值

            for (int i = 0; i < total; i++) {
                robots.add(addRobot(maxFight,"sys", names.get(i % total), RandomUtil.randInt(1, 3), RandomUtil.randInt(20, 50)));

                //战力递减
                maxFight=maxFight-RandomUtil.randInt(1,tempMaxFight);
                System.out.println(maxFight);
                if(maxFight<minFight){
                    maxFight=minFight;
                }
            }
            serialDataService.getData().setInitRobot(true);
        } else {
            robots.addAll(playerDao.selectRobots());
        }
    }
    // 增加机器人

    private int addRobot(int fightRate,String accName, String name, int vocation, int lev) {
        // 基本属性
        int playerId = playerService.getNextPlayerId();
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setAccName(accName);
        player.setName(name);
        player.setSex(1);
        player.setVocation(vocation);
        player.setLev(lev);
        player.setRegTime(new Date());
        player.setServerId(SysConfig.serverId);

		/*PlayerUpgradeCfg attr = ConfigData.getConfig(PlayerUpgradeCfg.class, lev);
		player.setHp(attr.hp + RandomUtil.randInt(0, 110));
		player.setAttack(attr.attack + RandomUtil.randInt(0, 10));
		player.setDefense(attr.defense + RandomUtil.randInt(0, 10));
		player.setSymptom(attr.symptom + RandomUtil.randInt(0, 10));
		player.setFu(attr.symptom + RandomUtil.randInt(0, 10));
		player.setCrit(attr.crit + RandomUtil.randInt(0, 5));

		// 更新战斗力
		float[] fightParams = ConfigData.globalParam().fightParams;
		float fight = player.getHp()*fightParams[0]+player.getAttack()*fightParams[1]+player.getDefense()*fightParams[2]+player.getFu()*fightParams[3]+player.getSymptom()*fightParams[4]+
				player.getCrit()*fightParams[5]; */

        //int minFight = ConfigData.globalParam().robotFight[0];
        //int maxFight = ConfigData.globalParam().robotFight[1];
        //int fightRate = RandomUtil.randInt(minFight, maxFight);
        player.setHp(Math.round(fightRate * ConfigData.globalParam().RobotParas[0]));
        player.setAttack(Math.round(fightRate * ConfigData.globalParam().RobotParas[1]));
        player.setDefense(Math.round(fightRate * ConfigData.globalParam().RobotParas[2]));
        player.setSymptom(Math.round(fightRate * ConfigData.globalParam().RobotParas[3]));
        player.setFu(Math.round(fightRate * ConfigData.globalParam().RobotParas[4]));
        player.setCrit(Math.round(fightRate * ConfigData.globalParam().RobotParas[5]));
        player.setFight(fightRate);

        // 初始化用户数据
        PlayerData playerData = playerService.initPlayerData(playerId, false);
        // 初始化时装和武器
        int fashionId = globalParam.fashionId[player.getVocation() - 1];
        player.setFashionId(fashionId);
        playerData.getFashions().add(fashionId);

        int weaponId = globalParam.weaponId[player.getVocation() - 1];
        player.setWeaponId(weaponId);
        playerData.getFashions().add(weaponId);
        playerData.setRobotFlag(true);
        //初始化技能
        int[] skills = globalParam.playerDefaultSkill[player.getVocation() - 1];
        for (int skill : skills) {
            playerData.getSkills().add(skill);
            playerData.getCurSkills().add(skill);

        }
        // 保存数据
        playerDao.insert(player);
        playerDao.update(player);
        playerService.updatePlayerData(playerId);
        return playerId;
    }

    public boolean isRobot(int playerId) {
        return robots.contains(playerId);
    }
}
