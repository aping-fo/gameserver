package com.fsp.disrup;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by lucky on 2017/7/11.
 */
public class DisruptorExecutor {
    private final int id;
    private final Disruptor<Runnable> disruptor;

    public DisruptorExecutor(int id,String name) {
        this.id = id;
        disruptor = new Disruptor<Runnable>(() ->new Thread(),8192, new NameThreadFactory(name));

    }
}
