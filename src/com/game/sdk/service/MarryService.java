package com.game.sdk.service;

import com.game.sdk.dao.MarryRank;
import com.game.sdk.dao.MarryRankDAO;
import com.game.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lucky on 2018/9/10.
 */
@Service
public class MarryService {
    @Autowired
    private MarryRankDAO marryRankDAO;

    public void saveOrUpdateScore(String openId, String nickName, String avatarUrl, int score) {
        marryRankDAO.insertMarry(openId, nickName, avatarUrl, score);
    }

    public String queryMarry(int beginIndex, int endIndex) {
        List<MarryRank> list = marryRankDAO.queryMarryRank(beginIndex, endIndex);
        return JsonUtils.object2String(list);
    }
}
