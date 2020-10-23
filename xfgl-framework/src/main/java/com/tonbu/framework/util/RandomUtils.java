package com.tonbu.framework.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class RandomUtils {

    protected static Logger logger=LogManager.getLogger(RandomUtils.class.getName());

    public RandomUtils() {
    }

    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        s = s.toUpperCase();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }

    public static String[] getUUID(int number) {
        if (number < 1) {
            return null;
        } else {
            String[] ss = new String[number];

            for(int i = 0; i < number; ++i) {
                ss[i] = getUUID();
            }

            return ss;
        }
    }

    public static void main(String[] args) {
        String[] ss = getUUID(10);

        for(int i = 0; i < ss.length; ++i) {
            System.out.println(ss[i]);
        }

    }


}
