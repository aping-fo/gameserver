package com.game.module.bulletscreen;

import com.game.data.BulletScreenConfig;
import com.game.data.Response;
import com.game.data.SceneConfig;
import com.game.params.*;
import com.game.util.ConfigData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BulletScreenService {
    private ConcurrentHashMap<Integer, List<String>> bulletScreenContentMap = new ConcurrentHashMap<>();//弹幕场景内容集合
    private ConcurrentHashMap<Integer, Integer> bulletScreenIndexMap = new ConcurrentHashMap<>();//弹幕场景内容索引集合

    /**
     * 发送弹幕
     *
     * @param intStringParam 弹幕场景和内容
     * @return 错误码
     */
    public IntParam sendBulletScreen(IntStringParam intStringParam) {
        IntParam param = new IntParam();

        int sceneId = intStringParam.param1;//场景id

        //场景是否存在
        SceneConfig config = ConfigData.getConfig(SceneConfig.class, sceneId);
        if (config == null) {
            param.param = Response.SCENE_NOT_EXIST;
            return param;
        }

        //弹幕上限是否配置
        int maxBulletScreenNumber = config.maxBulletScreenNumber;
        if (maxBulletScreenNumber == 0) {
            param.param = Response.BULLETSCREEN_NOT_SET;
            return param;
        }

        //创建弹幕集合
        List<String> bulletScreenContentList = bulletScreenContentMap.get(sceneId);
        if (bulletScreenContentList == null) {
            bulletScreenContentList = new ArrayList<>();
            bulletScreenContentList = bulletScreenContentMap.putIfAbsent(sceneId, bulletScreenContentList);
            if (bulletScreenContentList == null) {
                bulletScreenContentList = bulletScreenContentMap.get(sceneId);
                bulletScreenIndexMap.putIfAbsent(sceneId, 0);
            }
        }

        //添加弹幕内容
        String content = intStringParam.param2;//弹幕内容
        if (bulletScreenContentList.size() < maxBulletScreenNumber) {
            bulletScreenContentList.add(content);
        } else {
            //根据指针添加弹幕
            int index = bulletScreenIndexMap.get(sceneId);
            bulletScreenContentList.set(index, content);
            index++;
            index %= maxBulletScreenNumber;
            bulletScreenIndexMap.put(sceneId, index);
        }

        param.param = Response.SUCCESS;
        return param;
    }

    /**
     * 获取弹幕
     *
     * @param int3Param 场景id、弹幕开始下标和弹幕结束下标
     * @return 错误码
     */
    public ListParam<StringParam> getBulletScreen(Int3Param int3Param) {
        ListParam<StringParam> param = new ListParam<>();
        List<StringParam> contentList = new ArrayList<>();//发送的弹幕列表

        int sceneId = int3Param.param1;//场景id
        int minRecord = int3Param.param2;//最小记录
        int maxRecord = int3Param.param3;//最大记录

        //玩家弹幕是否存在
        List<String> bulletScreenList = bulletScreenContentMap.get(sceneId);//玩家弹幕列表
        int record;
        if (bulletScreenList != null) {
            int bulletScreenIndex = bulletScreenIndexMap.get(sceneId);//弹幕索引
            //获取弹幕
            record = bulletScreenList.size();// 玩家弹幕数量
            //场景是否设置最大数量
            SceneConfig config = ConfigData.getConfig(SceneConfig.class, sceneId);
            if (config == null) {
                param.code = Response.SCENE_NOT_EXIST;
                return param;
            }

            //根据记录数进行不同的获取
            if (record >= maxRecord) {
                //全部获取玩家弹幕
                getBulletScreenByRecord(contentList, minRecord, maxRecord, bulletScreenList, record, bulletScreenIndex, config);
            } else if (record < maxRecord && record >= minRecord) {
                //先获取玩家弹幕
                int totalRecord = maxRecord - minRecord + 1;//共需获取的弹幕数量
                maxRecord = record;
                int playerRecord = maxRecord - minRecord + 1;//获取的玩家弹幕数量
                getBulletScreenByRecord(contentList, minRecord, maxRecord, bulletScreenList, record, bulletScreenIndex, config);

                //再获取本地弹幕
                minRecord = 1;
                maxRecord = totalRecord - playerRecord;//获取的本地弹幕数量
                if (getLocalBulletScreenByRecord(param, contentList, minRecord, maxRecord)) {
                    return param;
                }
            } else {
                //获取本地弹幕
                minRecord -= record;
                maxRecord -= record;
                if (getLocalBulletScreenByRecord(param, contentList, minRecord, maxRecord)) {
                    return param;
                }
            }
        } else {
            //获取本地弹幕
            if (getLocalBulletScreenByRecord(param, contentList, minRecord, maxRecord)) {
                return param;
            }
        }

        param.params = contentList;
        param.code=Response.SUCCESS;
        return param;
    }

    /**
     * 根据获取的数量获取玩家弹幕
     *
     * @param contentList       发送的弹幕列表
     * @param minRecord         最小记录
     * @param maxRecord         最大记录
     * @param bulletScreenList  玩家弹幕列表
     * @param record            玩家弹幕总记录
     * @param bulletScreenIndex 玩家弹幕索引
     * @param config            玩家弹幕配置表
     */
    private void getBulletScreenByRecord(List<StringParam> contentList, int minRecord, int maxRecord, List<String> bulletScreenList, int record, int bulletScreenIndex, SceneConfig config) {
        //是否已达到最大数量
        if (record < config.maxBulletScreenNumber) {
            minRecord -= 1;
            getPlayerBulletScreen(contentList, minRecord, maxRecord, bulletScreenList);
        } else {

            //指针后面部分是否足够获取
            int indexAfter = record - bulletScreenIndex;
            if (indexAfter >= maxRecord) {
                //加载指针后面部分
                minRecord += bulletScreenIndex - 1;
                maxRecord += bulletScreenIndex;
                getPlayerBulletScreen(contentList, minRecord, maxRecord, bulletScreenList);
            } else {
                //先加载指针后面部分
                int total = maxRecord - minRecord + 1;//总获取数量
                minRecord += bulletScreenIndex - 1;
                maxRecord = record;
                indexAfter = maxRecord - minRecord;//指针后面获取的数量

                //是否需要从指针后面获取
                if (minRecord <= maxRecord) {
                    getPlayerBulletScreen(contentList, minRecord, maxRecord, bulletScreenList);
                    minRecord = 0;
                } else {
                    minRecord -= maxRecord;
                }

                //再加载指针前面部分
                maxRecord = total - indexAfter;//指针前面获取的数量
                getPlayerBulletScreen(contentList, minRecord, maxRecord, bulletScreenList);
            }
        }
    }

    /**
     * 获取玩家弹幕
     *
     * @param contentList      发送的弹幕列表
     * @param minRecord        最小记录，从0开始
     * @param maxRecord        最大记录
     * @param bulletScreenList 玩家弹幕列表
     */
    private void getPlayerBulletScreen(List<StringParam> contentList, int minRecord, int maxRecord, List<String> bulletScreenList) {
        for (int i = minRecord; i < maxRecord; i++) {
            String content = bulletScreenList.get(i);
            StringParam param=new StringParam();
            param.param=content;
            contentList.add(param);
        }
    }

    /**
     * 根据获取的数量获取本地弹幕
     *
     * @param param       错误码和本地弹幕列表
     * @param contentList 弹幕列表
     * @param minRecord   最小记录，从1开始
     * @param maxRecord   最大记录
     * @return 是否获取到本地弹幕
     */
    private boolean getLocalBulletScreenByRecord(ListParam<StringParam> param, List<StringParam> contentList, int minRecord, int maxRecord) {
        int record;//获取本地弹幕
        Collection<Object> configs = ConfigData.getConfigs(BulletScreenConfig.class);
        record = configs.size();//本地弹幕数量

        //本地是否全部获取
        if (record < maxRecord && maxRecord >= minRecord) {
            maxRecord = record;
        } else if (record < minRecord) {
            param.code = Response.LOCALBULLETSCREEN_ALL_ACCESS;
            return true;
        }

        getLocalBulletScreen(contentList, minRecord, maxRecord);
        return false;
    }

    /**
     * 获取本地弹幕
     *
     * @param contentList 弹幕列表
     * @param minRecord   最小记录，从1开始
     * @param maxRecord   最大记录
     */
    private void getLocalBulletScreen(List<StringParam> contentList, int minRecord, int maxRecord) {
        //获取本地弹幕
        for (int i = minRecord; i <= maxRecord; i++) {
            BulletScreenConfig config = ConfigData.getConfig(BulletScreenConfig.class, i);
            String content = config.bulletScreenContent;
            StringParam param=new StringParam();
            param.param=content;
            contentList.add(param);
        }
    }
}
