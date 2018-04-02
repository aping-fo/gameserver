package bitcoin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Created by lucky on 2017/12/18.
 */
public class BlockMain {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new GuiceModule());
        BlockModule blockModule = injector.getInstance(BlockModule.class);
        blockModule.onStart();

        blockModule.addBlock("Send 1 BTC to Ivan");
        blockModule.addBlock("Send 2 more BTC to Ivan");

        blockModule.showAll();
    }
}
