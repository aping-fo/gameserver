package guice;

import com.google.inject.AbstractModule;

/**
 * Created by lucky on 2017/12/15.
 */
public class HumanModule extends AbstractModule {
    @Override
    protected void configure() {
//        bind(Human.class).to(Child.class);
        bind(Human.class).to(Woman.class);
    }
}