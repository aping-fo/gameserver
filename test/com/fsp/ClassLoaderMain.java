package com.fsp;

import javax.sound.midi.Soundbank;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by lucky on 2017/7/24.
 */
public class ClassLoaderMain {
    static {
        System.out.println(ClassLoaderMain.i);
        System.out.println(ClassLoaderMain.n);
    }
    static int i = 10;
    final static int n = 20;



    {

    }

    ClassLoaderMain() {

    }

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(3);
        CountDownLatch latch = new CountDownLatch(3);

        for(int i = 0;i< 3;i++) {
            new Thread(()->{
                try {
                    barrier.await();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

                System.out.println("===" + Thread.currentThread().getName());
                latch.countDown();

            }).start();
        }

        latch.await();
        System.out.println("all finish");
    }
}
