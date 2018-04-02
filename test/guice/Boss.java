package guice;

import com.google.inject.Inject;

/**
 * Created by lucky on 2017/12/14.
 */
public class Boss {
    private final Clerk Clerk;

    @Inject
    public Boss(Clerk Clerk) {
        this.Clerk = Clerk;
    }

    public void test(){
        System.out.println("Boss...");
        Clerk.test();
    }
}
