package com.game.module.rank;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractRankCA<T extends AbstractRankCA<?>> implements IRankCA {

	@JsonIgnore
	private int owner;
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(IRankCA o) {
		if(owner == o.getOwner()){
			return 0;
		}
		int result = compareTo((T)o);
		if(result != 0){
			return result;
		}else{
			return 1;
		}
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	abstract public int compareTo(T t);
}
