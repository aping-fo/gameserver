package com.game.sdk.service;

import com.game.sdk.dao.MarryRank;
import com.game.sdk.dao.MarryRankDAO;
import com.game.util.JsonUtils;
import com.game.util.StringUtil;
import org.apache.commons.lang.StringUtils;
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

    public void login(String openId, String nickName, String avatarUrl, String invitor){
        int hasRecord = marryRankDAO.checkRecord(openId);

        if(hasRecord == 0){
            marryRankDAO.insertMarry(openId, nickName, avatarUrl, 3, "1,1,1,1,1,0");
            if(invitor != null && !invitor.isEmpty()){
                //如果有邀请者，则增加邀请者道具
                String datas = marryRankDAO.queryMarryDatas(invitor);
                if(datas != null && !datas.isEmpty()){
                    String[] array = datas.split(",");
                    if(array.length >= 6){
                        int inviteCount = Integer.parseInt(array[5]) + 1;
                        array[5] = String.valueOf(inviteCount);
                        //道具4，邀请1个增加1个
                        array[3] = String.valueOf(Integer.parseInt(array[3]) + 1);
                        //道具5，邀请2个增加1个
                        if(inviteCount % 2 == 0) {
                            array[4] = String.valueOf(Integer.parseInt(array[4]) + 1);
                        }
                        String result = StringUtils.join(array, ",");

                        marryRankDAO.updateMarryDatas(invitor, result);
                    }
                }
            }
        }else{
            marryRankDAO.updateMarry(openId, nickName, avatarUrl);
        }
    }

    public void updateScore(String openId, int score) {
        marryRankDAO.updateMarryScore(openId, score);
    }

    public int queryScore(String openId){
        return marryRankDAO.queryMarryScore(openId);
    }

    public String queryMarry(int beginIndex, int endIndex) {
        List<MarryRank> list = marryRankDAO.queryMarryRank(beginIndex, endIndex);
        return JsonUtils.object2String(list);
    }

    public void updateDatas(String openId, String datas){
        String oldDatas = marryRankDAO.queryMarryDatas(openId);
        if(oldDatas != null && !oldDatas.isEmpty()){
            String[] oldArray = oldDatas.split(",");
            String[] array = datas.split(",");
            if(array.length >= 5 && oldArray.length >= 5){
                for(int i = 0; i < array.length; i++){
                    oldArray[i] = String.valueOf(Integer.parseInt(oldArray[i]) + Integer.parseInt(array[i]));
                }

                String result = StringUtils.join(oldArray, ",");
                marryRankDAO.updateMarryDatas(openId, result);
            }
        }
    }

    public String queryDatas(String openId){
        return marryRankDAO.queryMarryDatas(openId);
    }
}
