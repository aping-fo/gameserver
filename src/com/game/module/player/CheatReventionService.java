package com.game.module.player;

import com.game.data.SkillConfig;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.google.common.collect.Maps;
import com.server.util.ServerLogger;
import com.server.util.Util;
import javafx.util.Pair;

import java.util.Map;

public class CheatReventionService {

    // 玩家技能CD检测防作弊
    // <playerId, <skillid, time>
    static private Map<Integer, Map<Integer, Long>> playerSkillCds = Maps.newConcurrentMap();
    static private Map<Integer, Map<Integer, Integer>> playerSkillCdHurtCount = Maps.newConcurrentMap();

    static private Map<Integer, Integer> playerHurtCountRecord = Maps.newConcurrentMap();
    static private Map<Integer, Long> playerHurtTimeRecord = Maps.newConcurrentMap();

    public static boolean isValidSkillHurt(int playerId, int skillId, int vocation)
    {
        if (skillId > 0) {
            SkillConfig config = ConfigData.getConfig(SkillConfig.class, skillId);
            if (config == null) {
                return false;
            }
            if (!playerSkillCds.containsKey(playerId)) {
                playerSkillCds.put(playerId, Maps.newConcurrentMap());
            }

            if (playerSkillCds.get(playerId).containsKey(skillId)) {
                long lastSkillTime = playerSkillCds.get(playerId).get(skillId);
                long timeNow = TimeUtil.getTimeNow();
                long second = (TimeUtil.getTimeNow() - lastSkillTime) / 1000;
                if (second > config.cd) {
                    playerSkillCds.get(playerId).put(skillId, timeNow);
                    // 清除记录
                    if (playerSkillCdHurtCount.containsKey(playerId)) {
                        playerSkillCdHurtCount.get(playerId).remove(skillId);
                    }
                }
                else {
                    // 记录技能CD时间内的连击数
                    if (!playerSkillCdHurtCount.containsKey(playerId)) {
                        playerSkillCdHurtCount.put(playerId, Maps.newConcurrentMap());
                    }

                    if (playerSkillCdHurtCount.get(playerId).containsKey(skillId)) {
                        int count = playerSkillCdHurtCount.get(playerId).get(skillId) + 1;
                        if (config.isUltimateSkill || config.cd > 20) { // 是觉醒技能
                            if (count > 20) {
                                ServerLogger.info("isValidSkillHurt 觉醒技能超出限制次数: Id = " + playerId + " vocation="+vocation + " skillId=" + skillId + " count=" + count);
                                return false;
                            }
                        }
                        else {
                            if (count > 15) {
                                ServerLogger.info("isValidSkillHurt 普通技能超出限制次数: Id = " + playerId + " vocation="+vocation + " skillId=" + skillId + " count=" + count);
                                return  false;
                            }
                        }

                        playerSkillCdHurtCount.get(playerId).put(skillId, count);
                        return true;
                    }
                    else {
                        playerSkillCdHurtCount.get(playerId).put(skillId, 1);
                    }
                }
            } else {
                playerSkillCds.get(playerId).put(skillId, TimeUtil.getTimeNow());
            }
        }
        return  true;
    }

    public static void resetRecords(int playerId)
    {
        if (playerSkillCds.containsKey(playerId)) {
            playerSkillCds.get(playerId).clear();
        }

        if (playerSkillCdHurtCount.containsKey(playerId)) {
            playerSkillCdHurtCount.get(playerId).clear();
        }

        if (playerHurtCountRecord.containsKey(playerId)) {
            playerHurtCountRecord.containsKey(playerId);
        }
        if (playerHurtTimeRecord.containsKey(playerId)) {
            playerHurtTimeRecord.containsKey(playerId);
        }
    }

    public static void addPlayerHurtRecord(int playerId)
    {
        if (!playerHurtCountRecord.containsKey(playerId)) {
            playerHurtCountRecord.put(playerId, 1);
        }
        else {
            playerHurtCountRecord.put(playerId, playerHurtCountRecord.get(playerId) + 1);
        }

        if (!playerHurtTimeRecord.containsKey(playerId)) {
            playerHurtTimeRecord.put(playerId, TimeUtil.getTimeNow());
        }
    }

    public static boolean hasPlayerHurtRecord(int playerId)
    {
        if (playerHurtCountRecord.containsKey(playerId) && playerHurtTimeRecord.containsKey(playerId)) {
            if (playerHurtCountRecord.get(playerId) > 0) {
                long time = playerHurtTimeRecord.get(playerId);
                long now = TimeUtil.getTimeNow();
                if ((now - time) / 1000 > 1200) {
                    playerHurtCountRecord.remove(playerId);
                    playerHurtTimeRecord.remove(playerId);
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
