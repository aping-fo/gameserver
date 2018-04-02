package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo
 * 冒泡
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6};

        int len = arr.length;
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (arr[i] > arr[j]) {
                    int t = arr[j];
                    arr[j] = arr[i];
                    arr[i] = t;
                }
            }
        }
        Arrays.stream(arr).forEach(System.out::println);
    }
}
