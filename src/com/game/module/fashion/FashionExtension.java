package com.game.module.fashion;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.params.Int2Param;
import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;

/**  
 * 时装协议
 */
@Extension
public class FashionExtension {
	
	@Autowired
	private FashionService fashionService;

	public static final int GET_INFO = 4701;

	//获取信息
	@Command(4701)
	public Object getInfo(int playerId,Object param){
		return fashionService.getFashionInfo(playerId);
	}
	
	//激活时装
	@Command(4702)
	public Object active(int playerId,IntParam id){
		IntParam result = new IntParam();
		result.param = fashionService.active(playerId, id.param);
		return result;
	}
	
	//替换时装
	@Command(4703)
	public Object replace(int playerId,Int2Param id){
		return fashionService.replace(playerId, id.param1, id.param2);
	}

	//购买为永久时装
	@Command(4704)
	public Object updateToForever(int playerId,IntParam id){
		return fashionService.updateToForever(playerId, id.param);
	}
}
