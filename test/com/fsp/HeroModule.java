package com.fsp;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/5.
 */
public class HeroModule {

    @Inject
    private MailModule mailModule;


    public HeroModule() {
        System.out.println("start1..");
    }

    @Inject
    public void onStart() {
        System.out.println("start2..");
        mailModule.print();
    }
}
