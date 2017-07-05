package com.game.module.goods;

public class GoodsEntry {
	
	public final int id;
	public int count;
	
	public GoodsEntry(int id,int count){
		this.id = id;
		this.count = count;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj){
			return true;
		}
		if(!(obj instanceof GoodsEntry)){
			return false;
		}
		GoodsEntry entry=(GoodsEntry)obj;
		if(id==entry.id&&count==entry.count){
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		int result=17;
		result=result*37+count;
		result = 37 * result + (int) (id ^ (id >>> 32));
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("%d,%d", id,count);
	}

}
