package com.game.module.facebook;

import com.game.data.ActivityCfg;
import com.game.data.ErrCode;
import com.game.data.GlobalConfig;
import com.game.data.Response;
import com.game.module.activity.ActivityConsts;
import com.game.module.activity.ActivityService;
import com.game.module.activity.ActivityTask;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.Player;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.serial.SerialData;
import com.game.module.serial.SerialDataService;
import com.game.params.*;
import com.game.util.ConfigData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Service
public class FacebookService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private SerialDataService serialDataService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private MailService mailService;

    public IntParam setFacebookShareFinish(int playerId)
    {
        //获取玩家信息
        PlayerData player = playerService.getPlayerData(playerId);
        IntParam ret = new IntParam();

        if (player == null) {
            ret.param = Response.ERR_PARAM;
            return ret;
        }

        if(player.getThirdChannel() == null || !player.getThirdChannel().equals("facebook")){
            ret.param = Response.FACEBOOK_NO_FACEBOOK;
            return ret;
        }

        //活动任务更新
        activityService.completeActivityTask(playerId, ActivityConsts.ActivityTaskCondType.T_FACEBOOK_SHARE, 1, ActivityConsts.UpdateType.T_VALUE, true);

        ret.param = Response.SUCCESS;

        return ret;
    }

    public IntParam setFacebookInvite(int playerId, StringParam idsParam) {
        IntParam ret = new IntParam();

        PlayerData player = playerService.getPlayerData(playerId);

        if(player == null) {
            ret.param = Response.ERR_PARAM;
            return ret;
        }

        if(player.getThirdChannel() == null || !player.getThirdChannel().equals("facebook")){
            ret.param = Response.FACEBOOK_NO_FACEBOOK;
            return ret;
        }

        Map<String, Set<Integer>> facebookInviteIds = serialDataService.getData().getFacebookInviteIds();
        String[] ids = idsParam.param.split("_");

        for(String facebookId : ids) {
            //检查是否邀请玩家已经有角色
            if(serialDataService.getData().getFacebookBindIds().containsKey(facebookId))
                continue;

            Set<Integer> playerIds = facebookInviteIds.get(facebookId);

            if(playerIds == null)
            {
                playerIds = new HashSet<>();
                facebookInviteIds.put(facebookId, playerIds);
            }

            playerIds.add(playerId);
        }

        ret.param = Response.SUCCESS;

        return ret;
    }

    public IntParam facebookInputInvitor(int playerId, IntParam id) {
        IntParam ret = new IntParam();
        PlayerData player = playerService.getPlayerData(playerId);
        PlayerData invitorPlayer = playerService.getPlayerData(id.param);

        if(invitorPlayer == null)
        {
            ret.param = Response.FACEBOOK_NO_PLAYER;
            return ret;
        }

        if(player.getThirdChannel() == null || !invitorPlayer.getThirdChannel().equals("facebook"))
        {
            ret.param = Response.FACEBOOK_INVITOR_NO_FACEBOOK;
            return ret;
        }

        if(player.getThirdChannel() == null || !player.getThirdChannel().equals("facebook"))
        {
            ret.param = Response.FACEBOOK_NO_FACEBOOK;
            return ret;
        }

        Map<String, Set<Integer>> facebookInviteIds = serialDataService.getData().getFacebookInviteIds();
        Set<Integer> playerIds = facebookInviteIds.getOrDefault(player.getThirdUserId(), null);

        if(playerIds == null || !playerIds.remove(invitorPlayer.getPlayerId())) {
            //不是邀请人
            ret.param = Response.FACEBOOK_NOT_INVITOR;
            return ret;
        }

        //成功,邀请者完成任务
        activityService.completeActivityTask(invitorPlayer.getPlayerId(), ActivityConsts.ActivityTaskCondType.T_FACEBOOK_INVITE, 1, ActivityConsts.UpdateType.T_ADD, true);

        //双方发放奖励;
        String title = ConfigData.getConfig(ErrCode.class, Response.FACEBOOK_EMAIL_INVITE_TITLE).tips;
        mailService.sendSysMailRewards(title, "", ConfigData.globalParam().facebookInviteSuccessReward, playerId, LogConsume.FACEBOOK_INVITE);
        mailService.sendSysMailRewards(title, "", ConfigData.globalParam().facebookInviteSuccessReward, invitorPlayer.getPlayerId(), LogConsume.FACEBOOK_INVITE);

        //绑定邀请成功ids
        Map<Integer, Set<Integer>> facebookInviteSuccessIds = serialDataService.getData().getFacebookInviteSuccessIds();
        Set<Integer> successIds = facebookInviteSuccessIds.get(invitorPlayer.getPlayerId());

        if(successIds == null)
        {
            successIds = new HashSet();
            facebookInviteSuccessIds.put(invitorPlayer.getPlayerId(), successIds);
        }
        successIds.add(playerId);

        ret.param = Response.SUCCESS;
        return ret;
    }

    public ListParam<FacebookInvitedPlayerVO> facebookGetInvitedPlayer(int playerId)
    {
        ListParam<FacebookInvitedPlayerVO> ret = new ListParam();
        ret.params = new ArrayList();

        Map<Integer, Set<Integer>> facebookInviteSuccessIds = serialDataService.getData().getFacebookInviteSuccessIds();
        Set<Integer> successIds = facebookInviteSuccessIds.get(playerId);

        if(successIds != null){
            for(Integer pId : successIds) {
                Player player = playerService.getPlayer(pId);
                if(player != null){
                    FacebookInvitedPlayerVO playerVo = new FacebookInvitedPlayerVO();

                    playerVo.id = pId;
                    playerVo.lev = player.getLev();
                    playerVo.fighting = player.getFight();
                    playerVo.name = player.getName();
                    playerVo.vocation = player.getVocation();

                    ret.params.add(playerVo);
                }
            }
        }

        return ret;
    }
}
