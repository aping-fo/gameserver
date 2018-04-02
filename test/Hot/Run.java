package Hot;

/**
 * Created by lucky on 2017/7/13.
 */
public class Run implements Runnable{
    @Override
    public void run() {
        while (true) {
            Test.getInstance().print();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
