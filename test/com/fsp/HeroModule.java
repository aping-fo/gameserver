package com.fsp;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/5.
 */
public class HeroModule{
    @Inject
    private MailModule mailModule;
    @Inject
    private BagModule bagModule;

    public HeroModule() {
        System.out.println("HeroModule..");
    }
    @Inject
    public void onStart() {
        System.out.println("HeroModule onStart..");
        System.out.println(mailModule);
        bagModule.print();
    }

    public void  print() {
        System.out.println("HeroModule print test");
    }
}
