package algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aping.foo.
 * 最大不重复字串
 * 思路：用一个hashmap存放扫描过的字符和位置，记录当前不重复开始位置和最大长度已经历史不重复开始位置和最大长度
 * 2个对比
 * 时间复杂度 o(n)
 */
public class NoDuplication {
    public static void main(String[] args) {
        String s = "abcdeadfweqradf"; //目标串
        Map<Character, Integer> latestIndex = new HashMap<>();  // 存放字符最近一次出现的位置
        int curStart = 0;
        int curLen;

        int maxStart = 0;
        int maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);  // 添加字符c到一个已有的无重复字符子串结尾
            if (latestIndex.containsKey(c)) { // 字符c已经在这个无重复字符子串中
                curLen = i - curStart;
                if (curLen > maxLen) {
                    maxLen = curLen;
                    maxStart = curStart;
                }
                curStart = latestIndex.get(c) + 1;  // 更新当前无重复字符子串的起点
            }
            latestIndex.put(c, i);
        }

        System.out.println(s.substring(maxStart, maxStart + maxLen));
    }
}
