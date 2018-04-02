package guice;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/12/14.
 */
public class Store {
    private final Boss boss;
    @Inject
    public Store(Boss boss) {
        this.boss = boss;
        //...
    }

    public void test(){
        System.out.println("store...");
    }
}
