package com.game.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StringUtil {

	/**
	 * 组装成 key二级分割val一级分割
	 * 
	 * @param map
	 * @param oneSp
	 * @param twoSp
	 * @return
	 */
	public static String map2str(Map<?, ?> map, String oneSp, String twoSp) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		for (Entry<?, ?> entry : map.entrySet()) {
			str.append(entry.getKey()).append(twoSp).append(entry.getValue())
					.append(oneSp);
		}
		return str.substring(0, str.length() - oneSp.length());
	}

	/**
	 * 未作错误处理
	 * 
	 * @param str
	 *            必须是数字，用分隔符分开
	 */
	public static int[] str2arr(String str, String split) {
		try {
			String[] items = str.split(split);
			int[] result = int[] result = Arrays.stream(items).mapToInt(Integer::parseInt).toArray();
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将数字字符串转成list
	 * 
	 * @param str
	 * @param split
	 * @return
	 */
	public static List<Integer> str2list(String str, String split) {
		try {
			String[] items = str.split(split);
			List<Integer> result = List<Integer> result = Arrays.stream(items).map(Integer::parseInt).collect(Collectors.toList());
			return result;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	/**
	 * 将数字字符串转成list
	 * 
	 * @param str
	 * @param split
	 * @return
	 */
	public static List<String> str2list2(String str, String split) {
		try {
			String[] items = str.split(split);
			List<String> result = Arrays.stream(items).collect(Collectors.toList());
			return result;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	/**
	 * 将字符串转成map，格式 num[二级分割符]num[[一级分割符]]num[二级分割符]num
	 * 
	 * @param str
	 * @param oneSplit
	 * @param twoSplit
	 * @return
	 */
	public static Map<Integer, Integer> str2map(String str, String oneSplit,
			String twoSplit) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		if(str==null||str.isEmpty()){
			return result;
		}
		String[] arr = str.split(oneSplit);
		for (String item : arr) {
			String[] data = item.split(twoSplit);
			int id = Integer.valueOf(data[0]);
			int count = Integer.valueOf(data[1]);
			Integer curCount = result.get(id);
			if(curCount==null){
				curCount = 0;
			}
			curCount+=count;
			result.put(id,curCount);
		}
		return result;
	}

	/**
	 * 将字符串转成map，格式 num[二级分割符]num[[一级分割符]]num[二级分割符]num
	 * 
	 * @param str
	 * @param oneSplit
	 * @param twoSplit
	 * @return
	 */
	public static Map<String, String> str2StrMap(String str, String oneSplit,
			String twoSplit) {
		Map<String, String> result = new HashMap<String, String>();
		String[] arr = str.split(oneSplit);
		for (String item : arr) {
			String[] data = item.split(twoSplit);
			if (data.length < 2) {
				result.put(data[0], "");
			} else {
				result.put(data[0], data[1]);
			}
		}
		return result;
	}
	
	/**
	 * 将字符串转化成二维数组
	 * @param str
	 * @param oneSplit
	 * @param twoSplit
	 * @return 格式 num[二级分割符]num[[一级分割符]]num[二级分割符]num
	 */
	public static int[][] str2dArrInt(String str,String oneSplit,String twoSplit){
		String[] one = str.split(oneSplit);
		
		int[][]result = new int[one.length][];
		
		for(int i=0;i<one.length;i++){
			String item = one[i];
			String[] info = item.split(twoSplit);
			
			result[i]=new int[info.length];
			
			for(int j=0;j<info.length;j++){
				result[i][j] = Integer.valueOf(info[j]);
			}
			
		}
		return result;
	}
	
	/**
	 * 将字符串转化成二维数组
	 * @param str
	 * @param oneSplit
	 * @param twoSplit
	 * @return 格式 num[二级分割符]num[[一级分割符]]num[二级分割符]num
	 */
	public static String[][] str2dArrStr(String str,String oneSplit,String twoSplit){
		String[] one = str.split(oneSplit);
		
		String[][]result = new String[one.length][];
		
		for(int i=0;i<one.length;i++){
			String item = one[i];
			String[] info = item.split(twoSplit);
			
			result[i]=new String[info.length];
			
			for(int j=0;j<info.length;j++){
				result[i][j] = (info[j]);
			}
			
		}
		return result;
	}
	
	public static String toFixedLengInt(int num,int length){
		DecimalFormat df=(DecimalFormat)NumberFormat.getInstance();
		df.setMinimumIntegerDigits(length);
		return df.format(num); 
	}
	
	public static boolean isNull(String str){
		return str == null || str.trim().length() == 0;
	}
	
	public static void main(String[] args) {
		String[][] result = str2dArrStr("1,3", ";", ",");
		System.out.println(result);
	}
}
