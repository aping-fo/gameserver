package com.fsp;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/5.
 */
public class MailModule {
    @Inject
    private HeroModule heroModuleProvider;


    public MailModule() {
        System.out.println("MailModule");
    }

    @Inject
    public void onStart() {
        System.out.println("MailModule onStart");
        heroModuleProvider.print();
    }

    public void  print() {
        System.out.println("MailModule print test");
    }
}
