package Hot;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2017/7/13.
 */
public class Test {
    private static Test test = new Test();
    public static Test getInstance() {
        return test;
    }

    public void print() {
        System.out.println("222222222222");
        List<Integer> list = Lists.newArrayList(2, 1, 1, 1);


        list.forEach(i ->{
            System.out.println();
        });


    }
}
