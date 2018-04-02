package algorithm;

/**
 * Created by aping.foo.
 * 求最大连续和
 * 思路，动态规划求解
 */
public class MaxSubArr {
    public static void main(String[] args) {
        int arr[] = {0, -2, 3, 5, -6, 2};
        int len = arr.length;

        int start = arr[len - 1];
        int all = arr[len - 1];

        for (int i = len - 2; i >= 0; i--) {
            start = Math.max(arr[i], start + arr[i]);
            all = Math.max(start, all);
        }

        System.out.println(all);
    }
}
