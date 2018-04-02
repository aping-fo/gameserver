package algorithm;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aping.foo
 * 求N个数如何组织成一个最大的数字，第一个算法里，暂不考虑越界问题
 * 后面会出一个考虑越界的做法
 */
public class Exercise6 {
    public static void main(String[] args) {
        int[] arr = {12, 122, 121, 45};
        BigDecimal num = maxNum(arr);
        System.out.println(num.toString());
    }

    /**
     * 没有考虑越界
     *
     * @param arr
     * @return
     */
    private static BigDecimal maxNum(int[] arr) {
        if (arr.length < 2) {
            return new BigDecimal(arr[0]);
        }
        List<String> list = Arrays.stream(arr).mapToObj(Integer::toString).collect(Collectors.toList());
        String s0 = list.get(0);
        String s1 = list.get(1);
        String str;
        if ((s0 + s1).compareTo(s1 + s0) >= 0) {
            str = s0 + s1;
        } else {
            str = s1 + s0;
        }

        if (list.size() < 3) {
            return new BigDecimal(Integer.parseInt(str));
        }

        for (int i = 2; i < list.size(); i++) {
            String s = list.get(i);
            if ((str + s).compareTo(s + str) >= 0) {
                str = str + s;
            } else {
                str = s + str;
            }
        }

        return new BigDecimal(str);
    }
}
