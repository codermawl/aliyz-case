package com.aliyz.practice.vm;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p></p>
 * Created by aliyz at 2020-07-17 14:27
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class JVMRunner {

    static String str = "AAAA";

    Map map = new HashMap<Integer, Integer>();
    Map cmap = new ConcurrentHashMap<Integer, Integer>();

    @Test
    public void stringOOM () {

        int i = 0;
        while (i++ < Integer.MAX_VALUE) {
            str += str;
        }
    }

    @Test
    public void strIntern () {

        String s1 = "java";

        System.out.println(s1.intern());

        String s2 = "java";
        System.out.println(s2.intern());

        String s3 = new StringBuilder("ja").append("va").toString();
        System.out.println(s3.intern());

        System.out.println(s1 == s2);
        System.out.println(s2 == s2.intern());
        System.out.println(s3 == s3.intern());

        System.out.println(s3.equals(s3.intern()));
        System.out.println(s2 == s3.intern());
    }

    @Test
    public void decimalToBinary () {
        final int HASH_BITS = 0x7fffffff;
        Integer k = new Integer(2836287);
        int h = k.hashCode();
        System.out.println("k.hashcode---:" + h);

        int idx = (h ^ (h >>> 16)) & HASH_BITS;
        System.out.println("idx---:" + idx);

        System.out.println(">>----[h]-----------------: " + decimalToBinary(h));
        System.out.println(">>----[(h >>> 16)]--------: " + decimalToBinary((h >>> 16)));
        System.out.println(">>----[h ^ (h >>> 16)]----: " + decimalToBinary((h ^ (h >>> 16))));
        System.out.println(">>----[idx]---------------: " + decimalToBinary(idx));

    }

    @Test
    public void flip() {
        int bitIndex = 3;
        int word = 62547127;
        System.out.println(">>----[word]-----------------: " + decimalToBinary(word));

        word ^= (1 << bitIndex);
        System.out.println(">>----[1 << bitIndex]--------: " + decimalToBinary(1 << bitIndex));
        System.out.println(">>----[flip]-----------------: " + decimalToBinary(word));
    }

    @Test
    public void threshold2f() {
        int n = 65;
        if (n < 1) {
            System.out.println(1);
        } else {
            int f = (n - 1) / 3;
            System.out.println( (n + f + 1 + 1) / 2 );
        }
    }

    private String decimalToBinary(int n){
        int[] bin = new int[32];
        int i = bin.length - 1;
        while(n!=0){
//              str = n%2+str;
            bin[i--] = n%2;
            n = n/2;
        }

        StringBuilder sb = new StringBuilder();
        i = 0;
        while (i < bin.length) {
            sb.append(bin[i++]);
            if (i == (16)) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
