package com.game.module.activity;

import com.game.data.ChargeConfig;
import com.game.data.ErrCode;
import com.game.data.Response;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.vip.VipService;
import com.game.params.Int2Param;
import com.game.util.ConfigData;
import com.server.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lucky on 2017/11/23.
 */
@Service
public class WelfareCardService {
    private static final int WEEKLY_DAYS = 7;
    private static final int MONTHLY_DAYS = 30;
    private static final int CMD_CARD_INFO = 8008;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailService mailService;

    public Int2Param getWelfareCardInfo(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        WelfareCard card = data.getWelfareCard();
        Int2Param param = new Int2Param();

        param.param1 = card.getRemainWeeklyDays();
        param.param2 = card.getRemainMonthlyDays();

        return param;
    }


    /**
     * 购买卡
     *
     * @param playerId
     * @param type
     */
    public void buyWelfareCard(int playerId, int type, int rechargeId) {
        if(type != VipService.TYPE_WEEKLY && type != VipService.TYPE_MONTH){
            return;
        }
        PlayerData data = playerService.getPlayerData(playerId);
        WelfareCard card = data.getWelfareCard();
        boolean sendMailFlag = false;
        if (type == VipService.TYPE_WEEKLY) {
            if (card.getWeeklyTime() == 0) {
                card.setWeeklyTime(System.currentTimeMillis());
                card.setWeeklyDays(WEEKLY_DAYS);
                sendMailFlag = true;
            } else {
                if (card.getRemainWeeklyDays() > 0) {
                    card.setWeeklyDays(card.getWeeklyDays() + WEEKLY_DAYS);
                } else {
                    card.setWeeklyTime(System.currentTimeMillis());
                    card.setWeeklyDays(WEEKLY_DAYS);
                    sendMailFlag = true;
                }
            }
        } else if (type == VipService.TYPE_MONTH) {
            if (card.getMonthlyTime() == 0) {
                card.setMonthlyTime(System.currentTimeMillis());
                card.setMonthlyDays(MONTHLY_DAYS);
                sendMailFlag = true;
            } else {
                if (card.getRemainMonthlyDays() > 0) {
                    card.setMonthlyDays(card.getMonthlyDays() + MONTHLY_DAYS);
                } else {
                    card.setMonthlyTime(System.currentTimeMillis());
                    card.setMonthlyDays(MONTHLY_DAYS);
                    sendMailFlag = true;
                }
            }
        }

        if (sendMailFlag) {
            String title;
            String content;
            if (type == VipService.TYPE_WEEKLY) {
                title = ConfigData.getConfig(ErrCode.class, Response.WEEKLY_CARD_TITLE).tips;
                content = ConfigData.getConfig(ErrCode.class, Response.WEEKLY_CARD_CONTENT).tips;
            } else {
                title = ConfigData.getConfig(ErrCode.class, Response.MONTHLYLY_CARD_TITLE).tips;
                content = ConfigData.getConfig(ErrCode.class, Response.MONTHLYLY_CARD_CONTENT).tips;
            }
            ChargeConfig cfg = ConfigData.getConfig(ChargeConfig.class, rechargeId);
            mailService.sendSysMailRewards(title, content, cfg.weekMonthCard, playerId, LogConsume.GUILD_COPY_REWARD);
        }

        Int2Param param = new Int2Param();

        param.param1 = card.getRemainWeeklyDays();
        param.param2 = card.getRemainMonthlyDays();
        SessionManager.getInstance().sendMsg(CMD_CARD_INFO, param, playerId);

    }

    private static final int monthlyId = 11;
    private static final int weeklyId = 12;

    /**
     * 登录发放奖励
     *
     * @param playerId
     */
    public void daily(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        WelfareCard card = data.getWelfareCard();
        if (card.getRemainWeeklyDays() > 0) {
            String title = ConfigData.getConfig(ErrCode.class, Response.WEEKLY_CARD_TITLE).tips;
            String content = ConfigData.getConfig(ErrCode.class, Response.WEEKLY_CARD_CONTENT).tips;
            ChargeConfig cfg = ConfigData.getConfig(ChargeConfig.class, weeklyId);
            mailService.sendSysMailRewards(title, content, cfg.weekMonthCard, playerId, LogConsume.GUILD_COPY_REWARD);
        }

        if (card.getRemainMonthlyDays() > 0) {
            String title = ConfigData.getConfig(ErrCode.class, Response.MONTHLYLY_CARD_TITLE).tips;
            String content = ConfigData.getConfig(ErrCode.class, Response.MONTHLYLY_CARD_CONTENT).tips;
            ChargeConfig cfg = ConfigData.getConfig(ChargeConfig.class, monthlyId);
            mailService.sendSysMailRewards(title, content, cfg.weekMonthCard, playerId, LogConsume.GUILD_COPY_REWARD);
        }
    }
}
