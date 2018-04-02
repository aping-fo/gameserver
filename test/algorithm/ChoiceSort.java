package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo
 * 选择排序
 */
public class ChoiceSort {

    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6, 5};

        int len = arr.length;

        for (int i = 0; i < len; i++) {
            int t = arr[i];
            int k = 0;
            for (int j = i + 1; j < len; j++) {
                if (arr[j] < t) {
                    t = arr[j];
                    k = j;
                }
            }
            if (k != 0) {
                arr[k] = arr[i];
                arr[i] = t;
            }
        }
        Arrays.stream(arr).forEach(System.out::println);
    }
}
