package com.tonbu.support.helper;

import com.tonbu.framework.util.ContainerUtils;
import com.tonbu.support.util.EhCacheUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class EhCacheHelper {

    private static CacheManager cacheManager = null;

    static {
         cacheManager= ContainerUtils.getBean(CacheManager.class);
    }

    /**
     * List添加数据
     * @param cacheName
     * @param value
     */
    @SuppressWarnings("unchecked")
    public static void add(String cacheName, String value) {
        Cache cache = cacheManager.getCache(cacheName);
        List<String> list = cache.get(cacheName, ArrayList.class);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(value);
        cache.put(cacheName, list);
    }

    /**
     * List获取数据
     *
     * @param cacheName
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static List<String> get(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        List<String> list = cache.get(cacheName, ArrayList.class);
        if (list == null) {
            return null;
        }
        return list;
    }



    /**
     * List判断是否包含数据
     * @param cacheName
     * @param value
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static boolean contains(String cacheName, String value) {
        Cache cache = cacheManager.getCache(cacheName);
        List<String> list = cache.get(cacheName, ArrayList.class);
        if (list == null) {
            return false;
        }
        return list.contains(value);
    }

    /**
     * List删除数据
     *
     * @param cacheName
     * @param value
     *            void
     */
    @SuppressWarnings("unchecked")
    public static void remove(String cacheName, String value) {
        Cache cache = cacheManager.getCache(cacheName);
        List<String> list = cache.get(cacheName, ArrayList.class);
        if (list == null) {
            return;
        }
        list.remove(value);
        cache.put(cacheName, list);
    }

    /**
     * Map添加数据
     *
     * @param cacheName
     * @param value
     */
    @SuppressWarnings("unchecked")
    public static void put(String cacheName, String key, String value) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> map = cache.get(cacheName, HashMap.class);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        cache.put(cacheName, map);
    }

    /**
     * Map获取数据
     *
     * @param cacheName
     * @param key
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static String get(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> map = cache.get(cacheName, HashMap.class);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Map获取数据
     *
     * @param cacheName
     * @param key
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static List<?> getList(String cacheName, String key,Callable<?> callable) {
        Cache cache = cacheManager.getCache(cacheName);
        return (List)cache.get(key,callable);
    }

    /**
     * Map获取数据
     *
     * @param cacheName
     * @param key
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static Object get(String cacheName, String key,Callable<?> callable) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache.get(key,callable);
    }

    /**
     * Map获取数据
     *
     * @param cacheName
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static List<String> getValues(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> map = cache.get(cacheName, HashMap.class);
        if (map == null) {
            return null;
        }
        List<String> values = new ArrayList<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            values.add(entry.getValue());
        }
        return values;
    }

    /**
     * Map判断是否包含数据
     * @param cacheName
     * @param key
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static boolean containsKey(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> map = cache.get(cacheName, HashMap.class);
        if (map == null) {
            return false;
        }
        return map.containsKey(key);
    }

    /**
     * Map删除数据
     *
     * @param cacheName
     * @param key
     *            void
     */
    @SuppressWarnings("unchecked")
    public static void removeKey(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> map = cache.get(cacheName, HashMap.class);
        if (map == null) {
            return;
        }
        map.remove(key);
        cache.put(cacheName, map);
    }

    public static void clear(String cacheName){
        Cache cache = cacheManager.getCache(cacheName);
        cache.clear();
    }


}
