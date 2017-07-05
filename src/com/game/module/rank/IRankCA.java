package com.game.module.rank;


public interface IRankCA extends Comparable<IRankCA> {

	int getOwner();
	
	void setOwner(int owner);
}
