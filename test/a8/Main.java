package a8;

import scala.Char;

import java.util.AbstractMap;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by lucky on 2018/1/8.
 */
public class Main<T, R> {
    public static void main(String[] args) {
        int[] arr1 = {1, 2, 3, 4};
        int[] arr2 = {0, 1, 2, 3};
        int sum = IntStream.of(arr1).reduce(1, (a, b) -> a + b);
        System.out.println(sum);
        IntStream.of(arr2).mapToObj(i -> new AbstractMap.SimpleEntry(i, arr2[i])).forEach(System.out::println);

        Main<String, String> main = new Main<>();
        main.Func(param -> param + "23","1111");
        System.out.println(Char.char2int('?'));
        byte[] arr = new byte['?'];
        System.out.println(arr.length);
    }

    public void Func(Function<T, R> function, T t) {
        R r = function.apply(t);
        System.out.println(r);
    }
}
