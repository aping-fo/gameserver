package com.fsp.disrup;

/**
 * Created by lucky on 2017/12/4.
 */
public class Main3 {
    public static int count2 = 2;
    int a = 0;

    {
        System.out.println(count2);
    }

    static {
        count2 ++;
        System.out.println(count2);
    }
    public static int count3 = 3;
    private Main3() {
        count2++;
        System.out.println(count2);
    }


    public static void main(String[] args) {
        Main3 main2 = new Main3();
        //System.out.println(Main3.count2);
    }
}
