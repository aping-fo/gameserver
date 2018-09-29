package com.game.module.giftbag;

import com.game.SysConfig;
import com.game.data.CdkeyConfig;
import com.game.data.Response;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialDataService;
import com.game.params.CDKRewardVo;
import com.game.params.Reward;
import com.game.params.StringParam;
import com.game.util.ConfigData;
import com.game.util.Context;
import com.game.util.HttpRequestUtil;
import com.game.util.StringUtil;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class GiftBagService {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    PlayerService playerService;

    /**
     * 获取礼包
     *
     * @param playerId            玩家Id
     * @param activationCodeParam 激活码集合
     * @return 错误码和礼包物品对象列表
     */
//    public CDKRewardVo getGiftBag(int playerId, StringParam activationCodeParam) {
//        String activationCode = activationCodeParam.param;
//        CDKRewardVo param = new CDKRewardVo();
//        param.rewards = new ArrayList<>();
//        CdkeyConfig config = ConfigData.giftBagMap.get(activationCode);
//
//        PlayerData player = playerService.getPlayerData(playerId);
//
//        if (player == null) {
//            param.errCode = Response.ACTIVATION_CODE_INVALID;
//            return param;
//        }
//
//        if (config == null) {
//            param.errCode = Response.ACTIVATION_CODE_INVALID;
//            return param;
//        }
//
//        //是否领取过该礼包
//        if(player.getGiftBagSet().contains(config.type)){
//            param.errCode = Response.HAS_RECEIVE_GIFTBAG;
//            return param;
//        }
//
//        if (serialDataService.getData().getCdkSet().contains(config.id)) {
//            param.errCode = Response.ACTIVATION_CODE_INVALID;
//            return param;
//        }
//
//        //返回礼包物品信息
//        int[][] rewards = config.item;
//        for (int[] reward1 : rewards) {
//            Reward reward = new Reward();
//            reward.id = reward1[0];
//            reward.count = reward1[1];
//            param.rewards.add(reward);
//        }
//
//        serialDataService.getData().getCdkSet().add(config.id);
//
//        //设置礼包失效
//        player.getGiftBagSet().add(config.type);
//
//        goodsService.addRewards(playerId, config.item, LogConsume.ACTIVATIONCODE_GIFTBAG);
//        param.errCode = Response.SUCCESS;
//        return param;
//    }

    /**
     * 获取礼包
     *
     * @param playerId            玩家Id
     * @param activationCodeParam 激活码集合
     * @return 错误码和礼包物品对象列表
     */
    public CDKRewardVo getGiftBag(int playerId, StringParam activationCodeParam) {
        String activationCode = activationCodeParam.param;
        CDKRewardVo param = new CDKRewardVo();

        Map<String, ActivationCode> giftBagMap = ConfigData.giftBagMap;
        if (giftBagMap == null) {
            ServerLogger.warn("激活码数据错误");
            param.errCode = Response.ACTIVATION_CODE_INVALID;
            return param;
        }

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            param.errCode = Response.ACTIVATION_CODE_INVALID;
            return param;
        }

        //是否领取过该礼包
        if (activationCode.length() < 8) {
            ServerLogger.warn("激活码=" + activationCodeParam.param);
            param.errCode = Response.ACTIVATION_CODE_INVALID;
            return param;
        }

        if (playerData.getGiftBagSet().contains(activationCode.substring(5, 8))) {
            param.errCode = Response.HAS_RECEIVE_GIFTBAG;
            return param;
        }

        ActivationCode activationCode1 = giftBagMap.get(activationCode);
        if (activationCode1 == null) {
            param.errCode = Response.ACTIVATION_CODE_INVALID;
            return param;
        }

        //返回礼包物品信息
        String rewards = activationCode1.getRewards();
        Map<Integer, Integer> reward = StringUtil.str2map(rewards, ";", ":");
        param.rewards = new ArrayList<>();
        for (Map.Entry<Integer, Integer> item : reward.entrySet()) {
            Reward rewardTmep = new Reward();
            rewardTmep.id = item.getKey();
            rewardTmep.count = item.getValue();
            param.rewards.add(rewardTmep);
        }

        goodsService.addRewards(playerId, reward, LogConsume.ACTIVATIONCODE_GIFTBAG);
        param.errCode = Response.SUCCESS;

        //更新后台激活码信息
        Context.getThreadService().execute(new Runnable() {
            @Override
            public void run() {
                Player player = playerService.getPlayer(playerId);
                if (player != null) {
                    StringBuffer stringBuffer = new StringBuffer("usePlayerId=" + playerId);
                    stringBuffer.append("&usePlayerName=" + player.getName());
                    stringBuffer.append("&usePlayerAccount=" + player.getAccName());
                    stringBuffer.append("&useServerId=" + SysConfig.serverId);
                    stringBuffer.append("&id=" + activationCode1.getId());
                    String s = HttpRequestUtil.sendPost(SysConfig.gmServerUrl + "/admin/updateActivationCode", stringBuffer.toString());
                }
            }
        });

        playerData.getGiftBagSet().add(activationCode.substring(5, 8));
        return param;
    }
}
