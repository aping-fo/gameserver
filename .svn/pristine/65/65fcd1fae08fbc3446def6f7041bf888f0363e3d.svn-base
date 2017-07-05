package com.game.module.goods;

import java.util.Comparator;

public class LeftCountSortor implements Comparator<Goods> {

	private static LeftCountSortor instance = new LeftCountSortor();
	
	public static LeftCountSortor getInstance(){
		return instance;
	}

	@Override
	public int compare(Goods o1, Goods o2) {
		return o1.getStackNum()-o2.getStackNum();
	}

}
