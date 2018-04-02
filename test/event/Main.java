package event;

import com.game.module.log.domain.Server;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lucky on 2017/12/11.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {


        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);


        System.out.println(list.subList(0,5));
        /*Server s  = new Server();
        s.setId(1);
        s.setChannelName("ssssssss");
        s.setChannelId(2);
        s.setName("22222");
        s.setType(2);
        s.setSaveOrUpdate(false);
        System.out.println(s.getSql());
        System.out.println(s.getParams());*/
        /*AtomicInteger atomic = new AtomicInteger(10);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        atomicBoolean.getAndSet(false);

        Semaphore s = new Semaphore(10);
        s.acquire();*/
        /*Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        t.interrupt();*/
        /*System.out.println(Thread.currentThread().getName());
        final EventBus eventBus = new EventBus();
        eventBus.register(new Object() {

            @Subscribe
            public void lister(Integer integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from int%n", integer);
            }

            @Subscribe
            public void lister(Number integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from Number%n", integer);
            }

            @Subscribe
            public void lister(Long integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from long%n", integer);
            }

            @Subscribe
            public void lister(DeadEvent event) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s%n", event.toString());
            }
        });*/

        /*eventBus.post(1);
        eventBus.post(1L);
        eventBus.post(new DeadEvent(1, 1));*/

        /*for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                eventBus.post(1);
            }).start();
        }

        Thread.sleep(10000);*/
        /*AsyncEventBus asyncEventBus = new AsyncEventBus(Executors.newSingleThreadExecutor());
        asyncEventBus.register(new Object() {

            @Subscribe
            public void lister(Integer integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from int%n", integer);
            }

            @Subscribe
            public void lister(Number integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from Number%n", integer);
            }

            @Subscribe
            public void lister(Long integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.printf("%s from long%n", integer);
            }
        });
        asyncEventBus.post(1);
        asyncEventBus.post(1L);*/
    }
}
