package com.fsp;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/5.
 */
public class MailModule {
    public MailModule() {
        System.out.println("start3");
    }

    @Inject
    public void onStart() {
        System.out.println("start4");
    }

    public void  print() {
        System.out.println("print");
    }
}
