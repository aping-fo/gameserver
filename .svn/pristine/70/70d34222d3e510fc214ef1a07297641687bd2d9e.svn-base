package com.game.module.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.game.event.InitHandler;
import com.game.params.chat.ChatVo;

@Service
public class ChatService implements InitHandler {

	private Map<Integer, List<ChatVo>> offlineChats = new ConcurrentHashMap<Integer, List<ChatVo>>();
	
	@Override
	public void handleInit() {
		// TODO Auto-generated method stub

	}

	public void addOffChat(int playerId, ChatVo vo){
		List<ChatVo> list = offlineChats.get(playerId);
		if(list == null){
			list = new ArrayList<ChatVo>();
			offlineChats.put(playerId, list);
		}
		list.add(vo);
	}
	
	public List<ChatVo> getOffChat(int playerId){
		return offlineChats.get(playerId);
	}
 
}
