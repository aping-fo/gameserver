package algorithm;

import java.util.Arrays;

/**
 * Created by aping.foo
 * 基数排序，限于整数排序，效率比较高
 * 时间复杂度：O(d(n+r)
 * 空间复杂度：O(n+r)
 * r为基数，0~9，d为位数
 */
public class Exercise13 {

    public static void main(String[] args) {
        int[] array = {
                50, 123, 1999,543, 187, 49, 30, 0, 2, 11, 100
        };

        radixSort(array, 0, array.length - 1, 4);
        Arrays.stream(array).forEach(System.out::println);
    }


    // 获取x这个数的d位数上的数字
    // 比如获取123的1位数，结果返回3
    private static int getDigit(int x, int d) {
        int a[] = {
                1, 10, 100, 1000, 10000, 100000, 1000000
        };
        return ((x / a[d]) % 10);
    }

    public static void radixSort(int[] list, int begin, int end, int digit) {
        final int radix = 10; // 基数
        int i = 0, j = 0;
        int[] count = new int[radix]; // 存放各个桶的数据统计个数
        int[] bucket = new int[end - begin + 1];

        // 按照从低位到高位的顺序执行排序过程
        for (int d = 1; d <= digit; d++) {

            // 置空各个桶的数据统计
            for (i = 0; i < radix; i++) {
                count[i] = 0;
            }

            // 统计各个桶将要装入的数据个数
            for (i = begin; i <= end; i++) {
                j = getDigit(list[i], d);
                count[j]++;
            }

            // count[i]表示第i个桶的右边界索引
            for (i = 1; i < radix; i++) {
                count[i] = count[i] + count[i - 1];
            }

            // 将数据依次装入桶中
            // 这里要从右向左扫描，保证排序稳定性
            for (i = end; i >= begin; i--) {
                j = getDigit(list[i], d); // 求出关键码的第k位的数字， 例如：576的第3位是5
                bucket[count[j] - 1] = list[i]; // 放入对应的桶中，count[j]-1是第j个桶的右边界索引
                count[j]--; // 对应桶的装入数据索引减一
            }

            // 将已分配好的桶中数据再倒出来，此时已是对应当前位数有序的表
            for (i = begin, j = 0; i <= end; i++, j++) {
                list[i] = bucket[j];
            }
        }
    }
}
