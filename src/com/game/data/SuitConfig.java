package com.game.data;
import java.util.Map;
/**
* t套装.xlsx(自动生成，请勿编辑！)
*/
public class SuitConfig {
	public int id;//key
	public String name;//名称
	public int[] equips;//装备
	public Map<Integer,int[]> twoAdd;//2件套
	public String twoDesc;//描述2
	public Map<Integer,int[]> threeAdd;//3件套
	public String threeDesc;//描述3
	public Map<Integer,int[]> fourAdd;//4件套
	public String fourDesc;//描述4
	public Map<Integer,int[]> fiveAdd;//5件套
	public String fiveDesc;//描述5
	public Map<Integer,int[]> sixAdd;//6件套
	public String sixDesc;//描述6
	public int[][] buffAdd;//buff
}