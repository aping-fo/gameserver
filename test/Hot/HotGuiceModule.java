package Hot;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by lucky on 2017/7/5.
 */
public class HotGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(HeroModule1.class).in(Singleton.class);
        bind(PlayerModule.class).in(Singleton.class);
        bind(HotMain.class).in(Singleton.class);
    }
}
