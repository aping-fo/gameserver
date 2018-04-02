package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by aping.foo
 * 洗牌算法，棋牌游戏里重点
 */
public class Exercise11 {
    public static void main(String[] args) {
        //方案一，在游戏中，在一轮打完之后，牌不要重置，继续采用次算法进行打乱
        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8};  //第一个元素预留
        shuffle1(arr, arr.length / 2);
        Arrays.stream(arr).forEach(System.out::println);

        System.out.println("==========================");
        //我们还可以直接采用JDK提供的API来操作，此方案可以每次都采用初始状态数据进行打乱
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        Collections.shuffle(list);
        list.stream().forEach(System.out::println);
    }

    /***
     * 方案一 交换法
     * @param a
     * @param n
     */
    static void shuffle1(int[] a, int n) {
        int n2 = n * 2;
        int[] b = new int[n2 + 1];
        for (int i = 1; i <= n2; i++) {
            b[(i * 2) % (n2 + 1)] = a[i];
        }
        for (int i = 1; i <= n2; ++i) {
            a[i] = b[i];
        }
    }
}
