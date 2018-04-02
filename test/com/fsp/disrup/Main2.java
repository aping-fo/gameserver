package com.fsp.disrup;

/**
 * Created by lucky on 2017/12/4.
 */
public class Main2 {
    private static Main2 singleTon = new Main2();
    public static int count2 = 2;

    int a = 0;
    {
        System.out.println(count2);
    }

    static {
        count2 ++;
        System.out.println(count2);
    }

    public Main2() {
        count2++;
        System.out.println(count2);
    }

    public static Main2 getInstance() {
        return singleTon;
    }

    public static void main(String[] args) {
        Main2 main2 = Main2.getInstance();
        System.out.println(Main2.count2);
    }
}
