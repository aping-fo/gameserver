package com.game.module.attach.endless;

import com.game.data.EndlessCfg;
import com.game.data.MonsterRefreshConfig;
import com.game.data.Response;
import com.game.module.activity.ActivityConsts;
import com.game.module.attach.AttachLogic;
import com.game.module.attach.AttachType;
import com.game.module.copy.CopyService;
import com.game.module.goods.GoodsEntry;
import com.game.module.goods.GoodsService;
import com.game.module.log.LogConsume;
import com.game.module.mail.MailService;
import com.game.module.player.PlayerService;
import com.game.module.rank.RankEntity;
import com.game.module.rank.RankService;
import com.game.module.rank.RankingList;
import com.game.module.rank.vo.EndlessRankEntity;
import com.game.module.task.Task;
import com.game.module.task.TaskService;
import com.game.module.title.TitleConsts;
import com.game.module.title.TitleService;
import com.game.params.EndlessInfo;
import com.game.params.ListParam;
import com.game.params.Reward;
import com.game.params.copy.CopyResult;
import com.game.util.ConfigData;
import com.game.util.TimeUtil;
import com.server.util.GameData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EndlessLogic extends AttachLogic<EndlessAttach> {

    @Autowired
    private CopyService copyService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RankService rankService;
    @Autowired
    private MailService mailService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TitleService titleService;
    private Map<String, Map<Integer, MonsterRefreshConfig>> monsterRefreshs = new ConcurrentHashMap<String, Map<Integer, MonsterRefreshConfig>>();

    private EndlessCfg cfg;

    @Override
    public byte getType() {
        return AttachType.ENDLESS;
    }

    @Override
    public void handleInit() {
        super.handleInit();
        cfg = GameData.getConfig(EndlessCfg.class, 10000);
    }

    public EndlessCfg getConfig() {
        return cfg;
    }

    @Override
    public EndlessAttach generalNewAttach(int playerId) {
        EndlessAttach attach = new EndlessAttach(playerId, getType());
        attach.setCurrLayer(1);
        attach.setChallenge(1);
        attach.setRefesh(cfg.resetTimeclearTime);
        return attach;
    }

    // 场景怪
    public Map<Integer, MonsterRefreshConfig> getSceneMonster(int playerId,
                                                              int copyId, int group) {

        EndlessAttach attach = attachService.getAttach(playerId, getType());
        int layer = attach.getCurrLayer();
        String key = String.format("%d_%d_%d", copyId, layer, group);
        Map<Integer, MonsterRefreshConfig> monsters = monsterRefreshs.get(key);
        if (monsters == null) {
            monsters = new ConcurrentHashMap<Integer, MonsterRefreshConfig>();
            monsterRefreshs.put(key, monsters);
            Map<Integer, MonsterRefreshConfig> _monsters = ConfigData
                    .getSceneMonster(copyId, group);
            int size = layer % 5 == 0 ? cfg.boss : cfg.monster;
            Random r = new Random(attach.getCurrLayer());
            if (_monsters.size() <= size) {
                monsters.putAll(_monsters);
            } else {
                List<MonsterRefreshConfig> ms = new ArrayList<MonsterRefreshConfig>(
                        _monsters.values());
                int total = _monsters.size();
                while (size-- > 0) {
                    MonsterRefreshConfig m = ms.remove(r.nextInt(total--));
                    monsters.put(m.id, m);

                }
            }

        }
        return monsters;
    }

    public int stopEndless(int playerId) {
        EndlessAttach attach = getAttach(playerId);
        attach.setChallenge(attach.getChallenge() - 1);
        attach.commitSync();
        return Response.SUCCESS;
    }

    public int resetEndless(int playerId) {
        EndlessAttach attach = getAttach(playerId);
        if (attach.getRefesh() <= 0) {
            return Response.NO_TODAY_TIMES;
        }
        if (attach.getCurrLayer() == 1) {
            return Response.ERR_PARAM;
        }
        attach.setCurrLayer(1);
        attach.setChallenge(1);
        attach.setRefesh(attach.getRefesh() - 1);
        attach.commitSync();
        return Response.SUCCESS;
    }

    public void dailyReset(int playerId) {
        EndlessAttach attach = getAttach(playerId);
        attach.setChallenge(1);
        attach.setRefesh(cfg.resetTimeclearTime);
        attach.commitSync();
    }

    public int clearEndless(int playerId) {
        EndlessAttach attach = getAttach(playerId);
        if (attach.getCurrLayer() > attach.getMaxLayer()) {
            return Response.ERR_PARAM;
        }
        if (attach.getChallenge() <= 0) {
            return Response.ERR_PARAM;
        }
        attach.setClearTime(System.currentTimeMillis());
        attach.commitSync();
        return Response.SUCCESS;
    }

    public ListParam<Reward> takeEndlessReward(int playerId, boolean pay) {
        ListParam<Reward> result = new ListParam<>();
        EndlessAttach attach = getAttach(playerId);
        int currLayer = attach.getCurrLayer();
        int max = attach.getMaxLayer();
        int layer;
        if (pay) {
            layer = (int) ((System.currentTimeMillis() - attach.getClearTime()) / (30 * TimeUtil.ONE_SECOND));
            int remainLayer = max - layer;
            List<GoodsEntry> goodsList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : cfg.clearCoin.entrySet()) {
                goodsList.add(new GoodsEntry(entry.getKey(), entry.getValue() * remainLayer));
            }
            if (goodsService.decConsume(playerId, goodsList, LogConsume.ENDLESS_CLEAR, currLayer, max) > 0) {
                result.code = Response.NO_CURRENCY;
                return result;
            }

            layer = max + 1;
        } else {
            layer = (int)((System.currentTimeMillis() - attach.getClearTime()) / (30 * TimeUtil.ONE_SECOND));
			if(layer < 1){
				attach.setClearTime(0L);
				attach.commitSync();
				return result;
			}
			layer = Math.min(max + 1, attach.getCurrLayer() + layer);
        }
        result.params = new ArrayList<>();
        for (int i = attach.getCurrLayer(); i < layer; i++) {
            // 掉落
            int copyId = ConfigData.endlessCopys[i % cfg.sectionLayer == 0 ? 1 : 0];
            //int star = playerService.getPlayerData(playerId).getCopys().get(copyId).getState();
            List<GoodsEntry> items = copyService.calculateCopyReward(playerId, copyId, 1);
            // 构造奖励
            int multiple = (int) ((i / cfg.sectionLayer + 1) * cfg.sectionMultiple);
            for (GoodsEntry g : items) {
                g.count *= multiple;
            }
            for (GoodsEntry g : items) {
                Reward reward = new Reward();
                reward.id = g.id;
                reward.count = g.count;
                result.params.add(reward);
            }
            goodsService.addRewards(playerId, items, LogConsume.COPY_REWARD, copyId, multiple);

        }
        attach.setClearTime(0L);
        attach.setCurrLayer(layer);
        //无尽漩涡称号
        titleService.complete(playerId, TitleConsts.WJXW_LAYER, layer, ActivityConsts.UpdateType.T_VALUE);
        attach.commitSync();
        return result;
    }

    public EndlessInfo getEndlessInfo(int playerId) {
        EndlessInfo info = new EndlessInfo();
        EndlessAttach attach = getAttach(playerId);
        info.challenge = attach.getChallenge();
        info.clearTime = attach.getClearTime();
        info.currLayer = attach.getCurrLayer();
        info.maxLayer = attach.getMaxLayer();
        info.refresh = attach.getRefesh();
        return info;
    }

    public void updateLayer(int playerId, CopyResult result) {
        EndlessAttach attach = getAttach(playerId);
        RankingList<EndlessRankEntity> ranking = rankService.getRankingList(RankService.TYPE_ENDLESS);
        ranking.updateEntity(playerId, new EndlessRankEntity(attach.getCurrLayer(), result.time));
        if (attach.getCurrLayer() > attach.getMaxLayer()) {
            attach.setMaxLayer(attach.getCurrLayer());
            taskService.doTask(playerId, Task.FINISH_ENDLESS, 1);
        }
        attach.setCurrLayer(attach.getCurrLayer() + 1);
        attach.commitSync();
    }

    //发放奖励，由ServerTimer定时调用
    public void sendReward() {
        RankingList<EndlessRankEntity> ranking = rankService.getRankingList(RankService.TYPE_ENDLESS);
        List<RankEntity> list = ranking.getOrderList();
        int rank = 1;
        for (RankEntity entity : list) {
            mailService.sendRewardMail(entity.getPlayerId(), MailService.ENDLESS_RANK, rank, LogConsume.ENDLESS_RANK_REWARD, rank);
            rank++;
        }
        //ranking.clear();
    }
}
