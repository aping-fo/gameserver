package com.game.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机工具类
 */
public class RandomUtil {
	private static Random rand = new Random();
	private static final int GAUSSIAN_PARAM = 4;// 正太分布的参数，越大就越精确


	/**
	 * 
	 * @param rates
	 *            和加起来为1
	 * @return 下标
	 */
	public static int getRandomIndex(final double rates[]) {
		double sum = 0.0;
		double random = ThreadLocalRandom.current().nextDouble();
		for (int i = 0; i < rates.length; i++) {
			sum += rates[i];
			if (random < sum) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * 返回随机概率的某个下标
	 * 
	 * @param rates
	 * @return
	 */
	public static int getRandomIndex(final int rates[]) {

	    /*
		int sum = 0;
		for (int rate : rates) {
			sum += rate;
		}

		int random = ThreadLocalRandom.current().nextInt(sum);
		sum = 0;
		for (int i = 0; i < rates.length; i++) {
			sum += rates[i];
			if (random < sum) {
				return i;
			}
		}
		return -1;// 不可能发生了
		*/
	    return getRandomIndex(rates, -1);
	}

    public static int getRandomIndex(final int rates[], int length) {

        if (length <= 0 || length > rates.length)
            length = rates.length;

	    int sum = 0;
        for (int i = 0; i < length; i++)
        {
            sum += rates[i];
        }

        int random = ThreadLocalRandom.current().nextInt(sum);
        sum = 0;
        for (int i = 0; i < length; i++) {
            sum += rates[i];
            if (random < sum) {
                return i;
            }
        }
        return -1;// 不可能发生了
    }
	
	/**
	 * 返回随机概率的某个下标
	 * 
	 * @param rates
	 * @return
	 */
	public static int getRandomIndex(final List<Integer> rates) {
		int sum = 0;
		for (int rate : rates) {
			sum += rate;
		}

		int random = ThreadLocalRandom.current().nextInt(sum);
		sum = 0;
		for (int i = 0; i < rates.size(); i++) {
			sum += rates.get(i);
			if (random < sum) {
				return i;
			}
		}
		return -1;// 不可能发生了
	}

	
	/**
	 * 生成符合正态分布(高斯分布)的随机数
	 * 
	 * @param min
	 *            随机数的最小值
	 * @param max
	 *            随机数的最大值
	 * @return 生成的随机数
	 */
	public static int nextGaussian(int min, int max) {
		return nextGaussian(min, max, GAUSSIAN_PARAM);
	}

	/**
	 * 生成符合正态分布(高斯分布)的随机数
	 * 
	 * @param min
	 * @param max
	 * @param precision
	 *            调整进度范围2-4之间会比较合适
	 * @return
	 */
	public static int nextGaussian(int min, int max, int precision) {
		if (precision <= 0) {// 参数错误
			precision = 2;
		}
		double randVal = ThreadLocalRandom.current().nextGaussian() + precision;

		if (randVal < 0) {
			randVal = 0;
		}
		int scope = precision * 2;// 范围
		if (randVal > scope) {
			randVal = scope;
		}
		double result = min + (max - min) * (randVal / scope);
		return Math.max((int) result, 1);
	}

	/**
	 * 返回0到high(不包括)的随机整数
	 * 
	 * @param high
	 * @return
	 */
	public static int randInt(int high) {
		return ThreadLocalRandom.current().nextInt(high);
	}
	
	/**
	 * 返回指定范围(包括high,low)的随机整数
	 */
	public static int randInt(int low, int high) {
		int d = high - low + 1;
		return ThreadLocalRandom.current().nextInt(d) + low;
	}

	/**
	 * x分之y是否随机到
	 */
	public static boolean randomHit(int x, int y) {
		return ThreadLocalRandom.current().nextInt(x) < y;
	}

	/**
	 * 在百分率范围内是否成功
	 */
	public static boolean randomHitPercent(int value) {
		return ThreadLocalRandom.current().nextInt(100) < value;
	}

	
	//计算圆桌概率
	public static int rollRate(int[]rates,int sum){
		for(int i=0;i<rates.length;i++){
			if(rates[i]>sum){
				rates[i] = sum;
			}
			sum -= rates[i];
		}
		return getRandomIndex(rates);
	}
	
	public static float randFloat(){
		return ThreadLocalRandom.current().nextFloat();
	}
}
