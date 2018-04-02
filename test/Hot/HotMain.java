package Hot;

import com.google.common.collect.Lists;
import com.google.inject.*;
import com.road.ClassReloader;
import org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by lucky on 2017/7/11.
 */
public class HotMain {
    @Inject
    private PlayerModule playerModule;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new HotGuiceModule());


        for(Map.Entry<Key<?>, Binding<?>> m : injector.getAllBindings().entrySet()){
            System.out.println(m.getValue().getKey());
        }

        Runnable r = () -> {
            try {
                Thread.sleep(5000);
                String path = System.getProperty("user.dir");
                File file = new File(path);
                File[] listFile = file.listFiles((dir, name) -> name.indexOf(".class") >= 0 && name.indexOf(".classpath") < 0);

                for (File f : listFile) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        try (BufferedInputStream dis = new BufferedInputStream(fis)) {
                            dis.mark(dis.available());
                            byte[] clazzBytes = new byte[dis.available()];
                            dis.read(clazzBytes);
                            dis.reset();
                            ClassReader c = new ClassReader(dis);
                            String clazzName = c.getClassName().replaceAll("/", ".");
                            System.out.println(clazzName);
                            Class<?> clazz = Class.forName(clazzName);
                            ClassReloader.load(clazz, clazzBytes);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();

//        Thread t1 = new Thread(() -> {
//            while (true) {
//                playerModule.test();
//                try {
//                    Thread.sleep(1200);
//                } catch (InterruptedssssssssscException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        t1.start();


        injector.getInstance(HotMain.class).playerModule.test();
        Thread.sleep(2000);

    }

    private void test() {
        List<Integer> list = Lists.newArrayList(2, 1, 1, 1);
        playerModule.test();
    }

}
