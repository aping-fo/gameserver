package com.tool.active;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.util.Context;
import com.game.util.RandomUtil;
import com.test.BaseTest;

public class GenMediaCard {

	private static final String INSERT = "INSERT INTO active_num(activeNum,type) values(?,?)";
	private static final int BEGIN = 358060000;

	private static final int[] types = { 14 };
	private static final int[] counts = { 10000 };

	public static void main(String[] args) {

		BaseTest.init();

		char[] ten = new char[10];

		List<Character> alphabet = new ArrayList<Character>(26);
		for (int i = 97; i < 97 + 26; i++) {
			if (i == 108 || i == 111) {
				continue;
			}
			alphabet.add((char) i);
		}
		for (int i = 0; i < 10; i++) {
			int random = RandomUtil.randInt(alphabet.size());
			if (RandomUtil.randInt(10) < 4) {
				ten[i] = alphabet.remove(random);
			}
		}
		List<Object[]> params = new ArrayList<Object[]>();
		int begin = BEGIN + 1;
		int end = 0;
		for (int i = 0; i < types.length; i++) {
			int type = types[i];
			int count = counts[i];
			System.out.println("开始生成类型:" + type);
			end = begin + count * 5;
			int nums[] = genUniNum(begin, end, count);

			for (int numI : nums) {
				String num = String.valueOf(numI);
				for (int j = 0; j < 10; j++) {
					if (ten[j] > 0) {
						num = num.replace(String.valueOf(j), String.valueOf(ten[j]));
					}
				}

				params.add(new Object[] { num, type });
				// System.out.println(num);

				if (params.size() % 1000 == 0) {
					Context.getLoggerService().getDb().batchUpdate(INSERT, params);
					params.clear();
				}
			}
			System.out.println("结束生成类型:" + type + ",共计:" + count);

			begin = end + 1;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!params.isEmpty()) {
			Context.getLoggerService().getDb().batchUpdate(INSERT, params);
		}
		System.out.println("最后的数字为:" + (end + 1));
		System.exit(0);

	}

	public static int[] genUniNum(int begin, int end, int count) {
		int[] result = new int[count];
		Map<Integer, Boolean> gen = new HashMap<Integer, Boolean>(10000);
		for (int i = 0; i < count; i++) {
			while (true) {
				int num = RandomUtil.randInt(begin, end);
				if (gen.containsKey(num)) {
					continue;
				}
				result[i] = num;
				gen.put(num, true);
				break;
			}
		}
		return result;
	}

}
