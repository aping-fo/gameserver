package algorithm;

/**
 * Created by aping.foo
 * KMP算法，高效寻找目标串。
 * 给定的字符串中，是否包含某个子字符串
 */
public class Exercise8 {
    public static void main(String[] args) throws Exception {
        final String s = "abadabcd";
        kmp(s, "abad");
    }

    /**
     * 在target字符串中寻找source的匹配
     *
     * @param target
     * @param source
     */
    private static void kmp(String target, String source) {
        int sourceLength = source.length();
        int targetLength = target.length();
        int[] result = preProcess(source);
        int j = 0;
        for (int i = 0; i < targetLength; i++) {
            //找到匹配的字符时才执行
            while (j > 0 && source.charAt(j) != target.charAt(i)) {
                //设置为source中合适的位置
                j = result[j - 1];
            }
            //找到一个匹配的字符
            if (source.charAt(j) == target.charAt(i)) {
                j++;
            }
            //匹配到一个，输出结果
            if (j == sourceLength) {
                j = result[j - 1];
                System.out.println("find");
            }
        }
    }

    /**
     * 预处理,生成 部分匹配表 ，这个是KMP关键部分
     *
     * @param s
     * @return
     */
    private static int[] preProcess(final String s) {
        int size = s.length();
        int[] result = new int[size];
        result[0] = 0;
        int j = 0;
        //循环计算
        for (int i = 1; i < size; i++) {
            while (j > 0 && s.charAt(j) != s.charAt(i)) {
                j = result[j];
            }
            if (s.charAt(j) == s.charAt(i)) {
                j++;
            }
            //找到一个结果
            result[i] = j;
        }
        return result;
    }

}
