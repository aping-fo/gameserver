package algorithm;

/**
 * Created by aping.foo
 * 2个字符串最长公共字串
 */
public class Exercise18 {

    public static void main(String[] args) {
        String s1 = "abac";
        String s2 = "baba";
        func(s1, s2);
        func1(s1, s2);
    }


    /**
     * 首先大家想到的方法可能是使用一个双层遍历的方式
     * 即外层长字符串，内层短字符串对比
     * 下面首先来采用这种常规方法
     *
     * @param s1
     * @param s2
     */
    public static void func(String s1, String s2) {
        String max = s1.length() >= s2.length() ? s1 : s2;
        String min = s1.length() >= s2.length() ? s2 : s1;

        int l = 0;
        String s = "";
        for (int i = 0; i < min.length(); i++) { //从第0个元素开始
            for (int j = i + 1; j <= min.length(); j++) {
                if (!max.contains(min.substring(i, j))) {
                    //如果不包含该字串，提前退出该层循环
                    break;
                }
                if (max.contains(min.substring(i, j)) && j - i > l) {
                    l = j - i; //长度
                    s = min.substring(i, j);
                }
            }
        }
        System.out.println(s);
    }

    /**
     * 采用动态规划方式来求解
     *  这里的思路是，把2个字符串 分割成字串，然后求最长公共字串
     * @param s1
     * @param s2
     */
    public static void func1(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        char char1;
        char char2;
        int len = 0;
        String lcs = "";
        String[][] arr = new String[len1][len2];
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                char1 = s1.charAt(i);
                char2 = s2.charAt(j);
                if (char1 != char2) {
                    arr[i][j] = "";
                } else {
                    if (i == 0 || j == 0) { //如果其中是第一个元素，那么肯定不会存在前面相等的元素，故直接放置
                        arr[i][j] = String.valueOf(char1);
                    } else {  //如果都不是第一个元素，并且相等，那么找前面一个元素合并，注意，此时他们前一个元素有可能有，有可能就是空""
                        arr[i][j] = arr[i - 1][j - 1] + char1;
                    }
                    if (arr[i][j].length() > len) {
                        len = arr[i][j].length();
                        lcs = arr[i][j];
                    } else if (arr[i][j].length() == len) {
                        lcs = lcs + "," + arr[i][j];
                    }
                }
            }
        }
        System.out.println(lcs);
    }
}
