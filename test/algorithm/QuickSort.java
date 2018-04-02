package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo
 * 快排
 * 思想：分治法
 */
public class QuickSort {
    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6};
        binary(0, arr.length - 1, arr);
        Arrays.stream(arr).forEach(System.out::println);
    }


    public static void binary(int low, int high, int[] arr) {
        int i = low;
        int j = high;
        int b = arr[low];

        while (i < j) {
            while (i < j && arr[j] >= b) j--;
            if (i < j) {
                arr[i++] = arr[j];
            }
            while (i < j && arr[i] < b) i++;
            if (i < j) {
                arr[j--] = arr[i];
            }
        }
        arr[i] = b;
        if (low < i - 1) binary(low, i - 1, arr);
        if (i + 1 < high) binary(i + 1, high, arr);
    }
}
