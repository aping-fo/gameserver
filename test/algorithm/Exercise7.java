package algorithm;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;

/**
 * Created by aping.foo
 * 已知某个文件内包含一些电话号码，一行一个的格式吧，简单点处理，提供个思路
 * ，每个号码为8位数字,统计不同号码的个数
 * 需要空间时间复杂度尽可能小
 * 其他类型题型，都可参考此方案解答
 */
public class Exercise7 {
    public static void main(String[] args) {
        /**
         * 定义一个bit结构，每一位代表一个号码,8位数最多 99999999
         * 用bit来表示的话，这么多位也就M
         *
         * 但是如果 int[] 99999999 * 32 =
         */
        int count = 0;
        BitSet bitSet = new BitSet();

        Path path = Paths.get("phone_number.txt");
        try (BufferedReader br = Files.newBufferedReader(path)) {
            int phoneNumber = Integer.parseInt(br.readLine());
            if (!bitSet.get(phoneNumber)) {
                count += 1;
                bitSet.set(phoneNumber);
            }
        } catch (Exception e) {

        }

        System.out.println("当前号码 ====" + count);
    }
}
