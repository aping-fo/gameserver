package com.game.module.sign;

import com.game.data.Response;
import com.game.data.SignCfg;
import com.game.module.activity.ActivityConsts;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.Int2Param;
import com.game.util.ConfigData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lucky on 2017/7/3.
 * 签到
 */
@Service
public class SignService {
    private final static int SIGN_DONE = 1; //当天已经签到
    private final static int SIGN_TOTAL_DAY = 30; //签到最大天数
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private TitleService titleService;
    public Int2Param sign(int playerId) {
        Int2Param ret = new Int2Param();
        ret.param1 = Response.ERR_PARAM;

        PlayerData data = playerService.getPlayerData(playerId);
        if (data.getSignFlag() == SIGN_DONE) {
            ret.param1 = Response.SIGN_HAS_DONE;
            return ret;
        }

        if (data.getSign() >= SIGN_TOTAL_DAY) { //why?
            data.setSign(1);
        } else {
            data.setSign(data.getSign() + 1);
        }

        SignCfg conf = ConfigData.getConfig(SignCfg.class, data.getSign());
        if (conf == null) {
            return ret;
        }

        Player player = playerService.getPlayer(playerId);
        Map<Integer, Integer> rewards = new HashMap<>(conf.rewards);
        if (conf.doubleFlag == 1 && player.getVip() >= conf.vipLevel) {
            for (Map.Entry<Integer, Integer> e : rewards.entrySet()) {
                rewards.put(e.getKey(), e.getValue() * 2);
            }
        }
        //签到称号
        titleService.complete(playerId, TitleConsts.SIGN,data.getSign(), ActivityConsts.UpdateType.T_VALUE);
        data.setSignFlag(SIGN_DONE);
        // 奖励
        goodsService.addRewards(playerId, rewards, LogConsume.SIGN_REWARD, data.getSign());
        ret.param1 = Response.SUCCESS;
        ret.param2 = data.getSign();
        return ret;
    }

    public void dailyReset(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        data.setSignFlag(0);
        if (data.getSign() >= SIGN_TOTAL_DAY) {
            data.setSign(0);
        }
    }
}
