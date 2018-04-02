package com.fsp;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by lucky on 2017/7/5.
 */
public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(HeroModule.class).in(Singleton.class);
        bind(MailModule.class).in(Singleton.class);
        bind(BagModule.class).in(Singleton.class);
    }
}
