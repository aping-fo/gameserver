package Hot;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/7/24.
 */
public class PlayerModule extends AbstractGuiceModule{
    @Inject
    private HeroModule1 heroModule;

    public void test() {
        while (true) {
            heroModule.test();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
