package com.fsp.disrup;

import com.google.common.collect.Lists;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lucky on 2017/7/11.
 */
public class Main {
    public static void main(String[] args) {
        Executor executor = Executors.newFixedThreadPool(20);
        LongEventFactory factory = new LongEventFactory();
        int bufferSize = 4096;
//        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory,bufferSize,executor);
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, new DefaultThreadFactory("aa"));
        disruptor.handleEventsWith(new LongEventHandler());
        disruptor.start();
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        long s = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            long seq = ringBuffer.next();
            LongEvent event = ringBuffer.get(seq);
            event.setValue(i);
            ringBuffer.publish(seq);
        }

        Map<Integer, Integer> map = new HashMap<>();
       /* map.forEach((k,v)->{

        });*/
        for (Map.Entry<Integer, Integer> s1 : map.entrySet()) {

        }

        List<Integer> list = Lists.newArrayList(1, 1, 1, 1);

        list.stream().forEach(System.out::print);


        Thread t = new Thread(() ->{

        });
    }
}
