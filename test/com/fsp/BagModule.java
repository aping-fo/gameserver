package com.fsp;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/5.
 */
public class BagModule {
    @Inject
    private MailModule mailModule;

    public BagModule() {
        System.out.println("BagModule..");
    }

    @Inject
    public void onStart() {
        System.out.println("BagModule onStart..");
        mailModule.print();
    }

    public void  print() {
        System.out.println("BagModule print test");
    }
}
