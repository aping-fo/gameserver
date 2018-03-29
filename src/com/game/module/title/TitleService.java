package com.game.module.title;

import com.game.data.Response;
import com.game.data.TitleConfig;
import com.game.module.activity.ActivityConsts;
import com.game.module.ladder.LadderService;
import com.game.module.player.Player;
import com.game.module.player.PlayerCalculator;
import com.game.module.player.PlayerData;
import com.game.module.player.PlayerService;
import com.game.module.scene.SceneService;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.game.params.ListParam;
import com.game.params.TitleVO;
import com.game.params.goods.AttrItem;
import com.game.util.ConfigData;
import com.game.util.JsonUtils;
import com.game.util.TimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.server.SessionManager;
import com.server.util.GameData;
import com.server.util.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Created by lucky on 2017/12/26.
 * 称号系统
 */
@Service
public class TitleService {
    private static final int CMD_ADD = 9002; //新增称号
    private static final int CMD_BROADCAST_EQUIP = 9005; // 称号状态广播
    @Autowired
    private PlayerService playerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private LadderService ladderService;
    @Autowired
    private PlayerCalculator calculator;
    @Autowired
    private SceneService sceneService;

    public void doInit(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        for (Object obj : GameData.getConfigs(TitleConfig.class)) {
            TitleConfig cfg = (TitleConfig) obj;
            Map<Integer, Title> map = data.getTitleTypeMap().get(cfg.titleSubType);
            if (map == null) {
                map = Maps.newHashMap();
                data.getTitleTypeMap().put(cfg.titleSubType, map);
            }

            if (!map.containsKey(cfg.id)) {
                Title title = new Title();
                title.setId(cfg.id);
                title.setOpenFlag(false);
                title.setCondType(cfg.titleSubType);
                map.put(cfg.id, title);
            }
        }
    }

    public void updateWeekly(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        List<Integer> deleteList = Lists.newArrayList();
        for (int id : data.getTitles()) {
            TitleConfig cfg = GameData.getConfig(TitleConfig.class, id);
            Map<Integer, Title> map = data.getTitleTypeMap().get(cfg.titleSubType);
            if (map != null) {
                Title title = map.get(cfg.id);
                if (cfg.period != 0) {
                    if (System.currentTimeMillis() - title.getRecvTime() > cfg.period * TimeUtil.ONE_DAY) {
                        deleteList.add(id);
                    }
                }
            }
        }

        for (int id : deleteList) {
            data.getTitles().remove(id);
        }
    }

    public void onLogin(int playerId) {
        doInit(playerId);
        updateWeekly(playerId);
        int ladderLevel = ladderService.getRank(playerId);
        if (ladderLevel != 0) { //排位赛
            checkTitle(playerId, TitleConsts.LADDER, ladderLevel, ActivityConsts.UpdateType.T_VALUE, null);
        }
    }

    /**
     * 新增称号
     *
     * @param playerId
     * @param titles
     */
    private void pushAddTitle(int playerId, List<Integer> titles) {
        if (titles.isEmpty()) {
            return;
        }
        ListParam listParam = new ListParam();
        listParam.params = Lists.newArrayList();
        for (int title : titles) {
            IntParam intParam = new IntParam();
            intParam.param = title;
            listParam.params.add(intParam);
        }
        Player player = playerService.getPlayer(playerId);
        calculator.calculate(player);
        ServerLogger.info("add new titles " + JsonUtils.object2String(listParam));
        SessionManager.getInstance().sendMsg(CMD_ADD, listParam, playerId);
    }

    /**
     * 更新称号条件
     *
     * @param playerId
     * @param type
     * @param value
     * @param updateType
     */
    public void complete(int playerId, int type, int value, int updateType) {
        if (!SessionManager.getInstance().getAllSessions().containsKey(playerId)) {
            return;
        }
        List<Integer> titles = Lists.newArrayList();
        checkTitle(playerId, type, value, updateType, titles);
        pushAddTitle(playerId, titles);
    }

    private void checkTitle(int playerId, int type, int value, int updateType, List<Integer> titles) {
        PlayerData data = playerService.getPlayerData(playerId);
        Map<Integer, Title> map = data.getTitleTypeMap().get(type);
        if (map == null) {
            return;
        }
        for (Title title : map.values()) {
            if (title.getCondType() == type) {
                if (data.getTitles().contains(title.getId())) continue;
                if (ActivityConsts.UpdateType.T_ADD == updateType) {
                    title.setValue(title.getValue() + value);
                } else if (ActivityConsts.UpdateType.T_VALUE == updateType) {
                    title.setValue(value);
                }

                TitleConfig config = ConfigData.getConfig(TitleConfig.class, title.getId());
                if (title.getCondType() == TitleConsts.ARENA || title.getCondType() == TitleConsts.LADDER) {
                    if (title.getValue() >= config.cond[1] && title.getValue() <= config.cond[2]) {
                        if (titles != null) titles.add(title.getId());
                        data.getTitles().add(title.getId());
                        title.setRecvTime(TimeUtil.getTodayBeginTime());
                    }
                } else {
                    if (title.getValue() >= config.cond[1]) {
                        if (titles != null) titles.add(title.getId());
                        data.getTitles().add(title.getId());
                        title.setRecvTime(TimeUtil.getTodayBeginTime());
                    }
                }
            }
        }

        if (titles != null && !titles.isEmpty()) {
            taskService.doTask(playerId, Task.TYPE_TITLE, data.getTitles().size());
        }
    }

    /**
     * 装备称号
     *
     * @param playerId
     * @param titleId
     * @return
     */
    public Int2Param equipTitle(int playerId, int titleId) {
        PlayerData data = playerService.getPlayerData(playerId);
        Player player = playerService.getPlayer(playerId);
        Int2Param param = new Int2Param();
        if (player.getTitle() == titleId) {
            param.param1 = Response.TITLE_EQUIP;
            return param;
        }

        if (titleId != 0 && !data.getTitles().contains(titleId)) {
            param.param1 = Response.TITLE_NOT_GET;
            return param;
        }
        player.setTitle(titleId);
        calculator.calculate(player);
        param.param1 = Response.SUCCESS;
        param.param2 = titleId;

        Int2Param int2Param = new Int2Param();
        int2Param.param1 = playerId;
        int2Param.param2 = titleId;
        sceneService.brocastToSceneCurLine(player, CMD_BROADCAST_EQUIP, int2Param);
        return param;
    }

    /**
     * 装备称号
     *
     * @param playerId
     * @return
     */
    public ListParam<TitleVO> getTitles(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);
        ListParam<TitleVO> listParam = new ListParam();
        listParam.params = Lists.newArrayList();

        LocalDate localDate = LocalDate.now();
        for (int titleId : data.getTitles()) {
            TitleConfig config = ConfigData.getConfig(TitleConfig.class, titleId);
            TitleVO param = new TitleVO();
            param.id = titleId;
            if (config.period != 0) {
                param.time = 7 - localDate.getDayOfWeek().getValue();
            }
            param.openFlag = data.getTitleTypeMap().get(config.titleSubType).get(titleId).isOpenFlag();
            listParam.params.add(param);
        }
        return listParam;
    }

    public Int2Param openTitle(int playerId, int titleId) {
        PlayerData data = playerService.getPlayerData(playerId);
        Int2Param param = new Int2Param();
        TitleConfig config = ConfigData.getConfig(TitleConfig.class, titleId);
        Title title = data.getTitleTypeMap().get(config.titleSubType).get(titleId);
        if (title == null) {
            param.param1 = Response.TITLE_NOT_GET;
            return param;
        }
        title.setOpenFlag(true);
        param.param1 = Response.SUCCESS;
        param.param2 = titleId;
        return param;
    }

    public ListParam<AttrItem> getThreeTypesRed(int playerId) {
        PlayerData data = playerService.getPlayerData(playerId);

        ListParam<AttrItem> result = new ListParam<>();
        result.params = Lists.newArrayList();

        for (Map.Entry<Integer, Integer> s : data.getTitleRead().entrySet()) {
            AttrItem attrItem = new AttrItem();
            attrItem.type = s.getKey();
            attrItem.value = s.getValue();

            result.params.add(attrItem);
        }

        return result;
    }

    public IntParam getSingleTypesRed(int playerId, Int2Param param) {
        PlayerData data = playerService.getPlayerData(playerId);

        data.getTitleRead().put(param.param1, param.param2);
        IntParam result = new IntParam();
        result.param = Response.SUCCESS;
        return result;
    }
}
