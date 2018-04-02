package guice;

import com.fsp.BagModule;
import com.fsp.HeroModule;
import com.fsp.MailModule;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by lucky on 2017/7/5.
 */
public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(Boss.class).in(Singleton.class);
        bind(Store.class).in(Singleton.class);
        bind(Clerk.class).in(Singleton.class);
    }
}
