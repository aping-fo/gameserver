package com.fsp.disrup;

import java.util.concurrent.ThreadFactory;

/**
 * Created by lucky on 2017/7/11.
 */
public class NameThreadFactory implements ThreadFactory {

    private final String name;

    public NameThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name);
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }
}

