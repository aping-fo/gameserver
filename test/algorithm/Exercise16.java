package algorithm;

/**
 * Created by aping.foo
 * 练习题16
 * 数组中出现次数超过一半的数字
 * 时间复杂度 O（n）
 *
 * 大部分的人，可能想到的是每个元素进行遍历，来统计出现次数，那么这样时间复杂度可能会是o(n2)
 * 下面咱们一起来找出一个好一点的方案
 */
public class Exercise16 {

    public static void main(String[] args) {
        int array[] = {1, 2, 3, 2, 2, 2, 5, 4, 6};
        fun(array);
    }

    /**
        方法是先遍历数组，保存数组中的数字和其出现次数
        如果下一个相同则次数加1，不同减1，如果次数变为0则保存数字为下一个数,最终情况是出现次数最多的元素
        最终保存下来，然后检查是否超过半数 ，也就是，如果某个元素超过一半，那么最终保存下来的就是这个元素
    */
    private static void fun(int[] arr) {
        int len = arr.length;

        int result = 0;
        int times = 0;
        for (int i = 0; i < len; i++) {
            if (times == 0) {
                result = arr[i];
                times = 1;
            } else if (result == arr[i]) {
                times += 1;
            } else {
                times -= 1;
            }
        }
        if (checkResult(arr, result, len)) {
            System.out.println("the number is " + result);
        }
    }

    private static boolean checkResult(int[] arr, int number, int len) {
        int time = 0, i;
        for (i = 0; i < len; i++) {
            if (arr[i] == number) {
                time++;
            }
        }
        return time * 2 > len;
    }
}
