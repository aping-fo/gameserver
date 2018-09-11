package com.game.module.attach.training;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.game.util.JsonUtils;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.daily.DailyService;
import com.game.module.gang.GangService;
import com.game.module.player.Player;
import com.game.module.player.PlayerDao;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.util.ConfigData;


@Service
public class trainingLogic extends AttachLogic<TrainAttach> {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private GangService gangService;
    @Autowired
    private SerialDataService serialService;
    private int total;//总关卡数
    private Map<Integer, TrainOpponent> opponents;
    private Map<Integer, List<Integer>> sectionOpponents;
    private final List<Integer> NULL = new ArrayList<Integer>();

    @Override
    public byte getType() {
        return AttachType.TRAINING;
    }


    @Override
    public void handleInit() {
        super.handleInit();
        NULL.add(1);
    }


    public void resetOpponent() {
        SerialData serialData = serialService.getData();
        opponents = serialData.getOpponents();
        sectionOpponents = serialData.getSectionOpponents();
        total = ConfigData.globalParam().exprienceFightRatio.length;
        if (serialData.getTrainingReset() >= DailyService.FIVE_CLOCK) {
            return;
        }

        serialData.setTrainingReset(DailyService.FIVE_CLOCK);
        int[] fightSection = ConfigData.globalParam().experienceFightSection;
        int size = fightSection.length;
        opponents.clear();

        for (int i = 0; i < size; i++) {
            List<Integer> ex = sectionOpponents.getOrDefault(i, NULL);
            List<Integer> ids = playerDao.selectByFightingPower(ex, fightSection[i], total + 1);
            sectionOpponents.put(i, ids);
            for (int id : ids) {
                Player player = playerService.getPlayer(id);
                TrainOpponent opponent = new TrainOpponent();
                opponent.setPlayerId(id);
                opponent.setName(player.getName());
                opponent.setLevel(player.getLev());
                opponent.setExp(player.getExp());
                opponent.setVip(player.getVip());
                opponent.setVipExp(player.getChargeDiamond());
                if (player.getGangId() > 0) {
                    if (gangService.getGang(player.getGangId()) != null && gangService.getGang(player.getGangId()).getName() != null) {
                        opponent.setGang(gangService.getGang(player.getGangId()).getName());
                    }
                }
                opponent.setVocation(player.getVocation());
                opponent.setFashionId(player.getFashionId());
                opponent.setWeaponId(player.getWeaponId());
                PlayerData playerData = playerService.getPlayerData(id);
                if(playerData == null) {
                    ServerLogger.warn("player data is null ? playerId = " + id);
                    continue;
                }
                opponent.setFight(player.getFight());
                opponent.setTitle(player.getTitle());
                opponent.setCurCards(playerData.getCurrCardIds());
                opponent.setCurSkills(playerData.getCurSkills());
                opponents.put(id, opponent);
            }
        }
    }


    @Override
    public TrainAttach generalNewAttach(int playerId) {
        TrainAttach attach = new TrainAttach(playerId, getType());
        attach.setHp(100);//刚开始百分百满血
        setOpponent(attach);
        return attach;
    }

    public void dailyReset(int playerId) {
        TrainAttach attach = getAttach(playerId);
        attach.setHp(100);
        attach.setIndex(0);
        attach.getTreasureBox().clear();
        setOpponent(attach);
        attach.commitSync();
    }

    private void setOpponent(TrainAttach attach) {
        int myFight = playerService.getPlayer(attach.getPlayerId()).getFight();
        attach.setHp(100);//刚开始百分百满血
        int[] section = ConfigData.globalParam().experienceFightSection;
        int index = section.length - 1;
        for (int i = 0; i <= index; i++) {
            if (myFight < section[i]) {
                index = i;
                break;
            }
        }
        attach.setOpponents(sectionOpponents.get(index));
    }

    public List<Integer> getOpponents(int playerId) {
        Player player = playerService.getPlayer(playerId);
        List<Integer> targets = new ArrayList<>();
        int[] section = ConfigData.globalParam().experienceFightSection;
        int index = section.length - 1;
        for (int i = 0; i <= index; i++) {
            if (player.getFight() < section[i]) {
                targets.addAll(sectionOpponents.get(i));
                if (targets.size() >= 11) {
                    break;
                }
            }
        }
        int size = targets.size() > 12 ? 12 : targets.size();
        return targets.subList(0, size - 1);
    }

    public TrainOpponent getOpponent(int playerId) {
        return opponents.get(playerId);
    }

    public int getMaxLevel() {
        return total;
    }

    public void addHP(int playerId, int hp) {
        TrainAttach attach = getAttach(playerId);
        int newHP = attach.getHp() + hp;
        if (newHP > 100) {
            newHP = 100;
        }
        attach.setHp(newHP);
        attach.commitSync();
    }
}
