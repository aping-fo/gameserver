package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo
 * 插入排序
 * 思想：将指定数插入一个有序的数组中
 */
public class InsertSort {
    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6, 5};
        int len = arr.length;
        for (int i = 1; i < len; i++) {
            int j = i;
            int target = arr[i];
            while (j > 0 && target < arr[j - 1]) {
                arr[j] = arr[j - 1];
                j--;
            }
            arr[j] = target;
        }
        Arrays.stream(arr).forEach(System.out::println);
    }
}
