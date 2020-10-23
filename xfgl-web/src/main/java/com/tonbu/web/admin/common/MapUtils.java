package com.tonbu.web.admin.common;

import java.util.*;

/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-06-30 17:59
 **/

public class MapUtils {
    // 将map值全部转换为大写
    public static Map<String, Object> transformUpperCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toUpperCase();
//			newKey = newKey.replace("_", "");
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }

    // 将map值全部转换为小写
    public static Map<String, Object> transformLowerCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toLowerCase();
//			newKey = newKey.replace("_", "");
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }
//    将list中map值全部转换为小写
    public static List<Map<String, Object>> listformLowerCase(List<Map<String, Object>> list) {

        List<Map<String,Object>> listbefor = new ArrayList<>();
        list.stream().forEach(item->{
            Map<String,Object> map1 = new HashMap<>();
            map1.putAll(MapUtils.transformLowerCase(item));
            listbefor.add(map1);
        });

        return listbefor;
    }
}
