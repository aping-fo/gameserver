package a8;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.server.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import scala.Char;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Created by lucky on 2018/1/8.
 */
public class Main<T, R> {
    private static Map<Integer,Integer> map = new HashMap<>();

    public static byte[] short2bytes(int length) {
        int temp = length;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8;// 向右移8位
        }
        return b;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }
    public static void main(String[] args) {
        /*Main<String, String> main = new Main<>();
        main.Func(param -> param + "23","1111");

        map.put(1,1);
        map.put(2,2);
        map.put(3,4);
        Main<Integer, Integer> main1 = new Main<>();
        main1.Func1(id -> map.get(id),1);*/

        /*short b = (short) (Short.MAX_VALUE + 1);
        byte[] lengthByte = short2bytes(b);
        System.out.println(byteToShort(lengthByte));
        byte[] lengthByte1 = short2bytes(byteToShort(lengthByte));
        System.out.println();

        byte[] lengthByte3 = Util.shortToBytes(b);
        short b1 = Util.bytesToShort(lengthByte3,0);
        System.out.println(Util.bytesToShort(lengthByte3,0));
        byte[] lengthByte4 = Util.shortToBytes(b1);
        System.out.println();*/

        /*int[] arr1 = {1, 2, 3, 4};
        int[] arr2 = {0, 1, 2, 3};
        int sum = IntStream.of(arr1).reduce(1, (a, b) -> a + b);
        System.out.println(sum);
        IntStream.of(arr2).mapToObj(i -> new AbstractMap.SimpleEntry(i, arr2[i])).forEach(System.out::println);


        System.out.println(Char.char2int('?'));
        byte[] arr = new byte['?'];
        System.out.println(arr.length);*/

        /*Comparator<String> cmp = (o1, o2) -> {
            if (o1.length() == o2.length()) {
                return o1.compareTo(o2);
            }
            return o1.length() - o2.length();
        };

        //cmp =cmp.thenComparing(String.CASE_INSENSITIVE_ORDER);
        List<String> list = Lists.newArrayList("adsf", "1111", "cda");

        Collections.sort(list, cmp);
        list.stream().forEach(System.out::println);*/

       /* Comparator<VehiclePart> cmp = comparator()
                .thenComparingInt(VehiclePart::getType).thenComparingDouble(VehiclePart::getRd)
                .thenComparing(comparator2());

        List<VehiclePart> list = Lists.newArrayList();
        VehiclePart v = new VehiclePart();
        v.setTier(1);
        v.setStars(3);
        v.setExperience(100);
        v.setType(2);
        v.setRd(0.3d);
        list.add(v);

        v = new VehiclePart();
        v.setTier(2);
        v.setStars(3);
        v.setExperience(99);
        v.setType(1);
        v.setRd(0.2d);
        list.add(v);

        v = new VehiclePart();
        v.setTier(3);
        v.setStars(4);
        v.setExperience(100);
        v.setType(2);
        v.setRd(0.3d);
        list.add(v);

        v = new VehiclePart();
        v.setTier(4);
        v.setStars(3);
        v.setExperience(100);
        v.setType(2);
        v.setRd(0.3d);
        list.add(v);

        v = new VehiclePart();
        v.setTier(4);
        v.setStars(2);
        v.setExperience(100);
        v.setType(2);
        v.setRd(0.3d);
        list.add(v);

        v = new VehiclePart();
        v.setTier(4);
        v.setStars(2);
        v.setExperience(102);
        v.setType(3);
        v.setRd(0.4d);
        list.add(v);

        List<VehiclePart> l = list.stream().sorted(cmp).collect(Collectors.toList());

        l.forEach(o -> System.out.println(o));*/

        Table<Integer,Integer,Integer> table =  HashBasedTable.create();
        table.put(1,11,111);
        System.out.println(table.row(1));
        System.out.println(table.get(1,11));
        for(Table.Cell cell : table.cellSet()) {
            System.out.println(cell.getRowKey());
            System.out.println(cell.getColumnKey());
            System.out.println(cell.getValue());
        }
    }


    public static Comparator<VehiclePart> comparator(){
        return Comparator.<VehiclePart>comparingInt(part -> -part.getTier());
    }

    public static Comparator<VehiclePart> comparator2(){
        return Comparator.<VehiclePart>comparingInt(part -> -part.getStars())
                .thenComparingLong(part -> -part.getExperience());
    }
    public void Func(Function<T, R> function, T t) {
        R r = function.apply(t);
        System.out.println(r);
    }

    public void Func1(Function<T, R> function, T t) {
        R r = function.apply(t);
        System.out.println(r);
    }
}
