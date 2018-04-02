package algorithm;

import java.util.Arrays;

/**
 * Created by  aping.foo.
 * 二分查找
 * 思想：二分查找必须是有序,先排序，再折半查找
 */
public class BinarySearch {
    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6};
        QuickSort.binary(0, arr.length - 1, arr);
        Arrays.stream(arr).forEach(System.out::println);
        int index = binarySearch(arr, 6);
        if (index == -1) {
            System.out.println("cannot find");
        }
        System.out.println("index =" + index);
    }

    public static int binarySearch(int[] arr, int target) {
        int low = 0;
        int high = arr.length;

        while (low <= high) {
            int middle = (high + low) / 2;
            if (arr[middle] == target) {
                return middle;
            }

            if (arr[middle] < target) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }

        return -1;
    }
}
