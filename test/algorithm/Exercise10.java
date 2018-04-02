package algorithm;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by aping.foo
 * 在游戏里，现在有很抢红包的功能，如何让红包分散的更均匀呢
 * 下面来分享一个在游戏里实现的一个红包算法
 *
 * 避免出现一个大包，其他都是1块
 * 因为我们之前采用随机方式，结果出现了这种情况，让玩家投诉了
 */
public class Exercise10 {

    public static void main(String[] args) {
        System.out.println(Math.floor(2.2));
        int size = 10;
        int money = 100;

        while (size > 0) {
            int m = randomMoney(size,money);
            money -= m;
            System.out.println(m);
            size --;
        }
    }

    /**
     * @param remainSize 剩余数量
     * @param remainMoney 剩余钱
     * @return
     */
    private static int randomMoney(int remainSize, int remainMoney) {
        if (remainSize == 1) {
            return remainMoney;
        }

        int min = 1; //保底1
        int max = remainMoney / remainSize * 2; //取平均数的2倍，这里是避免从总数里去随机，而出现一次随机出来的数过大，而这个倍数可调节
        int money = ThreadLocalRandom.current().nextInt(max); //随机
        money = money <= min ? min : money; //取2者最大的
        money = (int) Math.floor(money * 100) / 100; //这个地方其实是处理小数的，咱这里没小数，可以忽略，但是如果想更精准，上面那些可以用浮点数来代替
        return money;
    }
}
