package guice;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Created by lucky on 2017/12/14.
 */
public class Clerk {
    private final Provider<Store> shopProvider;
    @Inject
    Clerk(Provider<Store> shopProvider) {
        this.shopProvider = shopProvider;
    }

    public void test(){
        System.out.println("Clerk...");
        shopProvider.get().test();
    }
}
