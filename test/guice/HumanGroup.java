package guice;

import com.google.inject.Inject;

import java.util.Date;

/**
 * Created by lucky on 2017/12/15.
 */
public class HumanGroup {
    public HumanGroup() {
        System.out.println(new Date().getTime());// test singleton
    }

    @Inject
    private String groupName;

    /**
     * properties inject
     */
    @Inject
    private Human h1;

    public void h1talk() {
        h1.talk("h1");
    }

    private Human h2;

    /**
     * setter inject
     */
    @Inject
    public void setH2(Human h2) {
        this.h2 = h2;
    }

    public void h2talk() {
        h2.talk("h2");
    }

    /**
     * method inject(this method will execute automatic when instance creating)
     */
    @Inject
    public void h3talk(Human h3) {
        h3.talk("h3");
    }

    @Inject
    private Human w;

    public void wtalk() {
        w.talk("w");
    }

    public void noise() {
        this.h1talk();
        this.h2talk();
        this.h3talk(h1);
        System.out.println(groupName);
        this.wtalk();
    }
}
