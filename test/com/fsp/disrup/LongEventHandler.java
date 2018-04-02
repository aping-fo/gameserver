package com.fsp.disrup;


import com.lmax.disruptor.EventHandler;

/**
 * Created by lucky on 2017/7/11.
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println("seq " + l + " event " + longEvent + " " + Thread.currentThread());
    }
}
