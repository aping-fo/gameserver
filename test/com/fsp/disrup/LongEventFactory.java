package com.fsp.disrup;

import com.lmax.disruptor.EventFactory;

/**
 * Created by lucky on 2017/7/11.
 */
public class LongEventFactory implements EventFactory<LongEvent>{
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
