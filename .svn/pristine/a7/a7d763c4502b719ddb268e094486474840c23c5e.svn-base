package com.game.module.fame;

import org.springframework.beans.factory.annotation.Autowired;

import com.server.anotation.Command;
import com.server.anotation.Extension;

/**  
 * 声望
 */
@Extension
public class FameExtension {
	
	@Autowired
	private FameService service;

	public static final int GET_INFO = 3501;
	@Command(3501)
	public Object getInfo(int playerId,Object param){
		return service.getInfo(playerId);
	}
}
