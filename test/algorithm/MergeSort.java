package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo.
 * 归并排序
 * 思想，分治法，将数组分割成N个有序部分，最终目的是分割成只有1个元素的部分，这样每个部分都是有序
 * 最后合并有序数组
 *
 * 空间复杂读 o(n)
 * 时间复杂读 o(nlgn)
 */
public class MergeSort {

    static void memeryArray(int a[], int first, int mid, int last, int temp[]) {
        int i = first, j = mid + 1; //第一有序部分
        int m = mid, n = last; //第二有序部分
        int k = 0;

        while (i <= m && j <= n) {
            if (a[i] <= a[j])
                temp[k++] = a[i++];
            else
                temp[k++] = a[j++];
        }

        //如果某个数组多余，直接加到后面
        while (i <= m)
            temp[k++] = a[i++];

        while (j <= n)
            temp[k++] = a[j++];

        for (i = 0; i < k; i++)
            a[first + i] = temp[i];
    }

    static void mergeSort(int a[], int first, int last, int temp[]) {
        if (first < last) {
            int mid = (first + last) / 2;
            mergeSort(a, first, mid, temp);    //左边有序
            mergeSort(a, mid + 1, last, temp); //右边有序
            memeryArray(a, first, mid, last, temp); //再将二个有序数列合并
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 10, 6,4};
        int[] newArr = new int[arr.length];
        mergeSort(arr, 0, arr.length - 1, newArr);
        Arrays.stream(newArr).forEach(System.out::println);
    }
}
