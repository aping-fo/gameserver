package bitcoin;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import guice.Boss;

/**
 * Created by lucky on 2017/7/5.
 */
public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(BlockModule.class).in(Singleton.class);
        bind(ProofofworkModule.class).in(Singleton.class);
    }
}
