package com.fsp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Created by lucky on 2017/7/5.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
       Injector injector = Guice.createInjector(Stage.PRODUCTION,new MainGuiceModule());
        Thread.sleep(1111111);
    }
}
