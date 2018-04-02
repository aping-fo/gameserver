package algorithm;

/**
 * Created by aping.foo
 * N的阶乘末尾有多少个O
 *
 */
public class Exercise14 {

    public static void main(String[] args) {
        int n = fun2(20);
        System.out.println(n);
        n = fun1(110);
        System.out.println(n);
    }

    /**
     * 要计算Z，最直接的方法就是求出N的阶乘的所有因式(1,2,3,...,N)分解中5的指数。然后求和
     *时间复杂度 O(N)
     * @param n
     */
    private static int fun1(int n) {
        int num = 0;
        int i, j;

        for (i = 5; i <= n; i += 5) {
            j = i;
            while (j % 5 == 0) {
                num++;
                j /= 5;
            }
        }
        return num;
    }


    /**
     * 1、2、3、4、5、6、7、8、9、10、11、...N
     * 观察上面的数列可知，每5个数中会出现一个可以产生结果中0的数字。把这些数字抽取出来是
     * ...、5、...、10、...、15、...、20、...、25、...
     * 1、这些数字其实是都能满足5*k的数字，是5的倍数。统计一下他们的数量：n1=N/5。比如如果是101，则101之前应该是5,10,15,20,...,95,100共101/5=20个数字满足要求
     * 2、将1中的这些数字化成5*(1、2、3、4、5、...)的形式，内部的1、2、3、4、5、...又满足上面的分析：每5个数字有一个是5的倍数。抽取为：
     * ...、25、...、50、...、75、...、100、...、125、...
     *转化，25、50、75、100、125、...=5*(5、10、15、20、25、...)=5*5*(1、2、3、4、5、...)
     *
     * Z = N/5 + N /(5*5) + N/(5*5*5).....知道N/(5的K次方)等于0
     *  时间复杂度 O(logN)
     * @param n
     * @return
     */
    private static int fun2(int n) {
        int num = 0;

        while (n > 0) {
            num += n / 5;
            n = n / 5;
        }

        return num;
    }
}
