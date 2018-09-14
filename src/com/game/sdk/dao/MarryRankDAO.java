package com.game.sdk.dao;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

import java.util.List;

/**
 * Created by lucky on 2018/9/10.
 */
@DAO
public interface MarryRankDAO {
    @SQL("SELECT openId, nickName, avatarUrl,score FROM t_marry_rank ORDER BY score DESC LIMIT :beginIndex,:endIndex")
    public List<MarryRank> queryMarryRank(@SQLParam("beginIndex") int beginIndex, @SQLParam("endIndex") int endIndex);


    @SQL("REPLACE INTO t_marry_rank VALUES (:openId,:nickName,:avatarUrl,:score)")
    public void insertMarry(@SQLParam("openId") String openId, @SQLParam("nickName") String nickName, @SQLParam("avatarUrl") String avatarUrl, @SQLParam("score") int score);
}
