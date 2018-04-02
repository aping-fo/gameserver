package com.fsp;

import Hot.Run;
import com.game.module.worldboss.HurtRecord;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import sun.misc.Unsafe;

import javax.xml.transform.Source;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

/**
 * Created by lucky on 2017/7/5.
 */
public class Main {
    static int i = 0;

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        //Injector injector = Guice.createInjector(Stage.PRODUCTION,new GuiceModule());
        //System.out.println(System.getProperty("os.name"));
        /*Hero hero = new Hero();
        hero.setAge(1);
        hero.setId(1);
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        hero.setMap(map);
        String s = JsonUtils.object2String(hero);
        System.out.println(s);

        Hero hero1 = JsonUtils.string2Object("{\"age\":1,\"map\":{\"1\":1,\"2\":2}}", Hero.class);
        System.out.println(hero1.getAge());

        LongAdder longAdder = new LongAdder();
        longAdder.add(1000);
        longAdder.increment();
        System.out.println(longAdder.intValue());

        Main m = new Main();
        ThreadLocal<Integer> local = ThreadLocal.withInitial(m::local);
        System.out.println(local.get());

        System.out.println(Lists.newArrayList(1,1,1,1).parallelStream().mapToInt(i -> i).sum());


        CyclicBarrier barrier = new CyclicBarrier(3);

        Thread t = new Thread(() ->{
            while(i < 50)
                i += 1;
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("11111111");
        });

        Thread t2  = new Thread(() ->{
            while(i >= 50 && i < 100)
                i += 1;
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("22222222222");
        });

        t.start();
        t2.start();

        barrier.await();
        System.out.println(i);*/
//        LongAdder longAdder = new LongAdder();
//        LocalDate today = LocalDate.now();
//        System.out.println(today);
//
//        Instant instant = Instant.now();
//        System.out.println(instant.getEpochSecond() * 1000);
//        System.out.println(System.currentTimeMillis());
//        YearMonth yearMonth = YearMonth.now();
//        System.out.println(yearMonth.lengthOfMonth());
//        System.out.println(yearMonth.lengthOfYear());
//
//        MonthDay monthDay =MonthDay.now();
//        Unsafe unsafe = Unsafe.getUnsafe();

//        TreeSet<HurtRecord> treeMap = new TreeSet<>();
//        HurtRecord hr = new HurtRecord(10001,"1");
//        hr.setHurt(2);
//        treeMap.add(hr);
//
//        HurtRecord hr1 = new HurtRecord(10002,"2");
//        hr1.setHurt(1);
//
//        int h = 1;
//        while (true) {
//            HurtRecord hr2 = new HurtRecord(10002,"2");
//            hr2.setHurt(h - 1);
//            treeMap.remove(hr2);
//            System.out.println(treeMap.size());
//            hr2.setHurt(h ++);
//            treeMap.add(hr2);
//            System.out.println(treeMap.size());
//            Thread.sleep(1000);
//        }

        /*for(int i = 0;i < 3;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("12");
                    synchronized (map) {
                        System.out.println("1111111111111");
                        try {
                            Thread.sleep(1111);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("11111111111112");
                        System.out.println("11111111111113");
                    }

                }
            }).start();
        }*/

//        int i = 9;
//        System.out.println(i >> 1);
//        System.out.println(Math.sin(Math.PI / 4));

        /*Map<String,String> map  = new HashMap<>();
        map.put(null,"ssssssss");
        map.put(null,"ssssssss1");

        System.out.println(map.get(null));*/
       /* RangeMap<Integer, String> test = TreeRangeMap.create();
        test.put(Range.closed(0, 3), "xyb");
        test.put(Range.closed(1, 2), "Charlotte");*/
        //test.put(Range.closedOpen(2, 3), "love");
        // System.out.println(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        /*boolean a = false;
        a |= true;
        System.out.println(a);
        a |= false;
        System.out.println(a);*/

        /*LocalDateTime dateTime = LocalDateTime.now();
        System.out.println(dateTime.getDayOfYear());
        System.out.println(dateTime.getSecond());
        System.out.println(dateTime.getHour());
        System.out.println(dateTime.getDayOfWeek().getValue());
        System.out.println(Instant.now().toEpochMilli());
        System.out.println(System.currentTimeMillis());

        LocalDateTime dateTime1 = LocalDateTime.of(2017,11,23,11,11,11);
        LocalDateTime dateTime2 = LocalDateTime.now();
        LocalDateTime dateTime3 = dateTime2.withHour(0);
        System.out.println(dateTime3.getHour());
        System.out.println(dateTime1.isBefore(dateTime2));*/

        /*LocalDate localDate1 = LocalDate.now();
        LocalDate localDate = localDate1.plusYears(2);
        Period period = Period.between(localDate1, localDate);
        period.getUnits().forEach(System.out::println);
        System.out.println(localDate.until(localDate1, ChronoUnit.DAYS));*/

        /*Lists.newArrayList(1,1,1,1,1,1,1,1).stream().flatMap(o-> Stream.of(o))
        .forEach(System.out::println);*/

        int[] arr = new int[]{1};
        System.out.println(arr[0]);
    }

    static Map<Integer, Integer> map = new ConcurrentHashMap<>();

    static class TreeElement {
        private int id;
        private int h;

        public TreeElement(int id, int h) {
            this.id = id;
            this.h = h;
        }
    }

    public int local() {
        return 1;
    }
}
