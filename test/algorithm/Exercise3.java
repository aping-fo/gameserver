package algorithm;

/**
 * Created by aping.foo
 * 位运算
 */
public class Exercise3 {
    public static void main(String[] args) {
        //位移N位置1
        int n = 39;
        System.out.println(Integer.toBinaryString(n));
        n = n | (1 << 3);
        System.out.println(Integer.toBinaryString(n));

        //N位置0
        int bit = 1 << 3;
        int mark = 0;
        System.out.println(Integer.toBinaryString((~mark)));
        mark = (~mark)^bit;
        n &= mark;
        System.out.println(Integer.toBinaryString(n));
    }
}
