package algorithm;

/**
 * Created by aping.foo
 * 逆转
 */
public class Exercise5 {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6};
        reverse1(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }
        System.out.println();
        reverse2(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }

        System.out.println();
        reverse3(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }
    }


    /**
     * 方法一，利用一个中间变量 进行交换
     *
     * @param arr
     */
    private static void reverse1(int[] arr) {
        int len = arr.length;
        for (int i = 0; i < len / 2; i++) {
            int t = arr[i];
            arr[i] = arr[len - 1 - i];
            arr[len - 1 - i] = t;
        }
    }

    /**
     * 方法二，利用 a = a + b;  b = a - b, a = a - b
     *
     * @param arr
     */
    private static void reverse2(int[] arr) {
        int len = arr.length;
        for (int i = 0; i < len / 2; i++) {
            arr[i] = arr[i] + arr[len - 1 - i];
            arr[len - 1 - i] = arr[i] - arr[len - 1 - i];
            arr[i] = arr[i] - arr[len - 1 - i];
        }
    }

    /**
     * 方法三，利用 a = a^b;  b = a^b, a = a ^ b
     *
     * @param arr
     */
    private static void reverse3(int[] arr) {
        int len = arr.length;
        for (int i = 0; i < len / 2; i++) {
            arr[i] = arr[i] ^ arr[len - 1 - i];
            arr[len - 1 - i] = arr[i] ^ arr[len - 1 - i];
            arr[i] = arr[i] ^ arr[len - 1 - i];
        }
    }
}
