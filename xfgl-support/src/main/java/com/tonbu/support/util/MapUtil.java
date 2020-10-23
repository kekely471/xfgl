package com.tonbu.support.util;

import java.lang.reflect.Field;
import java.util.Map;

public class MapUtil {
    public static Object convertMap(Class<?> type, Map<String, String> map) {
        Object ob = null;
        try {
            if (map != null && map.size() > 0) {
                ob = type.newInstance();
                Field fields[] = type.getDeclaredFields();
                Field.setAccessible(fields, true);
                for (int i = 0; i < fields.length; i++) {
                    if (map.containsKey(fields[i].getName())) {
                        fields[i].set(ob, map.get(fields[i].getName()));
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return ob;
    }
}
