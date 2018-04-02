package guice;

import com.google.common.collect.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.curator.shaded.com.google.common.collect.*;

import java.time.LocalDate;
import java.util.Collections;

/**
 * Created by lucky on 2017/12/14.
 */
public class GuiceMain {
    public static void main(String[] args) {
//        Injector injector = Guice.createInjector(Stage.PRODUCTION, new HumanModule());
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new HumanModule());

        LocalDate localDate = LocalDate.now();
        System.out.println(localDate.getDayOfWeek().getValue());
        injector.getInstance(Human.class).talk("2");

        Range.closed(1,3).contains(2);

        System.out.println(Range.closed(2.0D,4.55D).contains(4.01));


        RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.closed(1, 10), "foo");

        System.out.println(rangeMap.get(12));


        BiMap<Integer,Integer> biMap = HashBiMap.create();
        BiMap<Integer,Integer> biMap1 = Maps.synchronizedBiMap(biMap);
        biMap1.put(1,11);
        biMap1.put(2,22);
        biMap1.put(3,33);

        System.out.println(biMap1.size());
        System.out.println(biMap1.get(1));
        System.out.println(biMap1.inverse().get(11));

        Multimap<Integer,Integer> multimap = HashMultimap.create();
        multimap.put(1,100001);
        multimap.put(1,100002);
        multimap.put(1,100003);
        multimap.put(1,100004);

        System.out.println(multimap.get(1).contains(100001));
    }
}
