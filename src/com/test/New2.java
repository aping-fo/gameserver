package com.test;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by lucky on 2017/7/8.
 */
public class New2 {
    public void test() {
        System.out.println();
    }

    public static void main(String[] args) {
        byte plainByte = 3;
        byte key = 0x56;
        byte encryptByte = (byte) ((plainByte ^ key));
        System.out.println(plainByte);
        System.out.println(encryptByte);
        System.out.println((encryptByte^key));

        Set<Integer> set = Sets.newHashSet();
        for(int i = 1;i < 1000000;i++){
            set.add(i ^ 0xef);
        }

        System.out.println(set.size());
    }
}
