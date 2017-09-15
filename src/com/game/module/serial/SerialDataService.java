package com.game.module.serial;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.module.ladder.LadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.event.InitHandler;
import com.game.event.ServiceDispose;
import com.game.util.CompressUtil;
import com.game.util.JsonUtils;

@Service
public class SerialDataService implements InitHandler,ServiceDispose {

	@Autowired
	private SerialDataDao dao;
	@Autowired
	private LadderService ladderService;
	private SerialData data;
	private SerialData2 data2;//第二部分数据
	
	private AtomicInteger minGRank;
	
	@Override
	public void handleInit() {
		dao.initSerialData(1);
		byte[] dbData = dao.selectSerialData(1);
		if(dbData!=null){
			dbData = CompressUtil.decompressBytes(dbData);
			data = JsonUtils.string2Object(new String(dbData,Charset.forName("utf-8")), SerialData.class);
		}else{
			data = new SerialData();
		}
		
		//第二部分数据
		dao.initSerialData(2);
		byte[] dbData2 = dao.selectSerialData(2);
		if(dbData2!=null){
			dbData2 = CompressUtil.decompressBytes(dbData2);
			data2 = JsonUtils.string2Object(new String(dbData2,Charset.forName("utf-8")), SerialData2.class);
		}else{
			data2 = new SerialData2();
		}

		ladderService.ladderSort();
	}
	
	public SerialData getData(){
		return data;
	}

	@Override
	public void serviceDispse() {
		//第一部分
		String str = JsonUtils.object2String(data);
		byte[] dbData = str.getBytes(Charset.forName("utf-8"));
		dao.updateSerialData(CompressUtil.compressBytes(dbData),1);
		//第二部分
		String str2 = JsonUtils.object2String(data2);
		byte[] dbData2 = str2.getBytes(Charset.forName("utf-8"));
		dao.updateSerialData(CompressUtil.compressBytes(dbData2),2);
	}
	
	public SerialData2 getData2(){
		return data2;
	}
	
	//最小的神将排名 TODO 没初始化
	public int getMinGeneralRank(){
		return minGRank.get();
	}
}
