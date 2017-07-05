package com.game.module.attach;

import java.lang.reflect.ParameterizedType;

import org.springframework.beans.factory.annotation.Autowired;


public abstract class AttachLogic<T extends Attach> {

	private final Class<T> attachClazz;
	
	@Autowired
	protected AttachService attachService;
	
	@SuppressWarnings("unchecked")
	public AttachLogic(){
		attachClazz = (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	Class<T> getAttachClass(){
		return attachClazz;
	}
	
	public void handleInit(){};
	
	public abstract byte getType();
	
	public abstract T generalNewAttach(int playerId);
	
	public T getAttach(int playerId){
		return attachService.getAttach(playerId, getType());
	}
	
}
