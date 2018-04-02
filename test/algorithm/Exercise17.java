package algorithm;

/**
 * Created by aping.foo
 * 输入一个整数，删除N个数，求最大值
 * 比如 325 ，删除1个，最大值是35
 *
 * 方法有很多
 * 比如暴力求法，首先组装出所有符合规则的数字，然后比较大小。
 * 消耗在组装数据上
 *
 *
 */
public class Exercise17 {

    public static void main(String[] args) {
        int[] arr = {1, 2, 6, 4, 5, 3, 8};
        func(arr, 4);
    }

    /**
     * 在这里，我把题意做了转化，将整数按顺序转化成数组，将删除N个数转化成保留M位数
     *  效率比暴力求解稍微快出K倍，各位大侠也可以来做做
     * 解法如下
     *
     *  我们首先找出首位最大的数字，
     *  然后找次大数字，以此类推
     * @param arr
     * @param count
     */
    private static void func(int[] arr, int count) {
        String s = "";
        int k = 0;
        while (s.length() < count) {
            System.out.println("--");
            int t = arr[k];
            int max = arr.length + s.length() - count + 1;
            for (int i = k; i < max; i++) {
                System.out.println("=");
                if (t <= arr[i]) {
                    t = arr[i];
                    k = i + 1;
                }
            }
            if (s.length() < count) s += t;
        }

        System.out.println(s);
    }
}
