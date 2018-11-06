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
    @SQL("SELECT COUNT(1) FROM t_marry_rank WHERE openId = :openId")
    public int checkRecord(@SQLParam("openId") String openId);

    @SQL("SELECT openId, nickName, avatarUrl,score FROM t_marry_rank ORDER BY score DESC LIMIT :beginIndex,:endIndex")
    public List<MarryRank> queryMarryRank(@SQLParam("beginIndex") int beginIndex, @SQLParam("endIndex") int endIndex);

    @SQL("REPLACE INTO t_marry_rank(openId,nickName,avatarUrl,score, datas) VALUES (:openId,:nickName,:avatarUrl,:score, :datas)")
    public void insertMarry(@SQLParam("openId") String openId, @SQLParam("nickName") String nickName, @SQLParam("avatarUrl") String avatarUrl, @SQLParam("score") int score, @SQLParam("datas") String datas);

    @SQL("UPDATE t_marry_rank SET nickName = :nickName, avatarUrl = :avatarUrl WHERE openId = :openId")
    public void updateMarry(@SQLParam("openId") String openId, @SQLParam("nickName") String nickName, @SQLParam("avatarUrl") String avatarUrl);

    @SQL("UPDATE t_marry_rank SET score = :score WHERE openId = :openId AND score < :score")
    public void updateMarryScore(@SQLParam("openId") String openId, @SQLParam("score") int score);

    @SQL("SELECT score FROM t_marry_rank WHERE openId = :openId")
    public int queryMarryScore(@SQLParam("openId") String openId);

    @SQL("SELECT datas FROM t_marry_rank WHERE openId = :openId")
    public String queryMarryDatas(@SQLParam("openId") String openId);

    @SQL("UPDATE t_marry_rank SET datas = :datas WHERE openId = :openId")
    public void updateMarryDatas(@SQLParam("openId") String openId, @SQLParam("datas") String datas);
}
