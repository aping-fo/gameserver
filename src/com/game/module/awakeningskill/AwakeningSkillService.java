package com.game.module.awakeningskill;

import com.game.data.AwakenAttributeCfg;
import com.game.data.AwakeningSkillCfg;
import com.game.data.Response;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.util.ConfigData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class AwakeningSkillService {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PlayerCalculator playerCalculator;

    /**
     * 获取觉醒技能信息
     *
     * @param playerId 玩家id
     * @return 觉醒技能id和解锁id（用于查询觉醒技能等级）
     */
    public ListParam<Int2Param> GetAwakeningSkill(int playerId) {
        ListParam<Int2Param> param = new ListParam<>();
        param.params = new ArrayList<>();

        //获取玩家信息
        Player player = playerService.getPlayer(playerId);
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (player == null || playerData == null) {
            param.code = Response.ERR_PARAM;
            return param;
        }

        //获取配置表
        Collection<Object> configs = ConfigData.getConfigs(AwakeningSkillCfg.class);
        if (configs.size() == 0) {
            param.code = Response.ERR_PARAM;
            return param;
        }

        //获取觉醒技能集合
        Map<Integer, Integer> awakeningSkillMap = playerData.getAwakeningSkillMap();
        if (awakeningSkillMap == null) {
            param.code = Response.ERR_PARAM;
            return param;
        }

        //是否已获取技能集合
        if (awakeningSkillMap.size() == 0) {
            //获取本职业觉醒技能
            for (Object obj : configs) {
                AwakeningSkillCfg config = (AwakeningSkillCfg) obj;
                //是否本职业
                if (player.getVocation() == config.vocation) {
                    awakeningSkillMap.put(config.id, 0);
                }
            }
        }

        //返回技能id和等级
        for (int key : awakeningSkillMap.keySet()) {
            Int2Param int2Param = new Int2Param();
            int2Param.param1 = key;
            int2Param.param2 = awakeningSkillMap.get((key));
            param.params.add(int2Param);
        }

        return param;
    }

    /**
     * 升级觉醒技能
     *
     * @param playerId 玩家id
     * @param intParam 需要升阶的觉醒技能id
     * @return 错误码
     */
    public IntParam UpAwakeningSkill(int playerId, IntParam intParam) {
        IntParam param = new IntParam();

        //获取玩家信息
        Player player = playerService.getPlayer(playerId);
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (player == null || playerData == null) {
            param.param = Response.ERR_PARAM;
            return param;
        }

        //获取配置表
        int id = intParam.param;
        AwakeningSkillCfg config = ConfigData.getConfig(AwakeningSkillCfg.class, id);
        if (config == null) {
            param.param = Response.ERR_PARAM;
            return param;
        }

        //是否达到等级
        if (player.getLev() < config.locklv) {
            param.param = Response.NO_LEV;
            return param;
        }

        //获取觉醒技能集合
        Map<Integer, Integer> awakeningSkillMap = playerData.getAwakeningSkillMap();
        if (awakeningSkillMap == null) {
            param.param = Response.ERR_PARAM;
            return param;
        }

        //是否激活技能
        if (awakeningSkillMap.get(id) == null) {
            param.param = Response.ERR_PARAM;
            return param;
        }
        int lockID = awakeningSkillMap.get(id);
        boolean isActivate = false;
        if (lockID == 0) {
            lockID = config.lockID;
            isActivate = true;
        }

        //获取觉醒技能属性表
        AwakenAttributeCfg awakenAttributeCfg = ConfigData.getConfig(AwakenAttributeCfg.class, lockID);
        if (!isActivate) {
            if (awakenAttributeCfg == null) {
                param.param = Response.ERR_PARAM;
                return param;
            }
            //是否最大等级
            if (awakenAttributeCfg.nextID == 0) {
                param.param = Response.MAX_LEV;
                return param;
            }

            //激活材料为当前行消耗材料，升级材料为下一行的消耗材料
            awakenAttributeCfg = ConfigData.getConfig(AwakenAttributeCfg.class, awakenAttributeCfg.nextID);
        }
        if (awakenAttributeCfg == null) {
            param.param = Response.ERR_PARAM;
            return param;
        }

        //扣除材料
        int[][] material = awakenAttributeCfg.material;
        if(goodsService.decConsume(playerId, material, LogConsume.STRENGTH_COST)!=Response.SUCCESS){
            param.param = Response.ERR_PARAM;
            return param;
        }

        //觉醒技能升级
        int skillLevel = awakenAttributeCfg.id;
        //是否技能激活
        if (awakeningSkillMap.get(id) == 0) {
            skillLevel = lockID;
        }
        awakeningSkillMap.put(id, skillLevel);

        //更新属性
        playerCalculator.calculate(playerId);

        return param;
    }
}
