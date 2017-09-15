package com.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.util.TimeUtil;

public class TimeTest {

    public static void main(String[] args) throws InterruptedException {
//		Date nowTime=new Date();
//		SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd");
//		TimeUtil.CUR_TIME_FORMAT = time.format(nowTime);

        TreeMap<Integer, Integer> treeMap = new TreeMap<>();


        final AtomicInteger a = new AtomicInteger(1);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    treeMap.put(a.getAndIncrement(), a.intValue());

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    List<Integer> list = new ArrayList(treeMap.values());
                    for (int i : treeMap.values()) {
                        System.out.println(i);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t2.start();

        Thread.sleep(11111111);
    }

}
