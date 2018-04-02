package guice;

/**
 * Created by lucky on 2017/12/15.
 */
public class Woman implements Human{
    @Override
    public void talk(String name) {
        System.out.println("woman say hello " + name);
    }
}
