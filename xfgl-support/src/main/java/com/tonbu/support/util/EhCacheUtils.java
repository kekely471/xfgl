/**
 * Created with IntelliJ IDEA.
 */
package com.tonbu.support.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.InputStream;

/**
 * @PackageName: com.tonbu.support.utils
 * @author: jinlei
 * @date: 2019/3/6 3:56 PM
 * @description: 基于Ehcache的工具类
 */
public class EhCacheUtils {
    private static EhCacheUtils defaultCache = null;
    private static Cache cache = null;

    //实现单例模式
    public static EhCacheUtils getInstance() {
        if(defaultCache == null) {
            defaultCache = new EhCacheUtils();
        }
        return defaultCache;
    }

    //private Cache cache;

    public EhCacheUtils() {
        InputStream stream = this.getClass().getResourceAsStream("/ehcache.xml");
        CacheManager manager = CacheManager.create(stream);
        cache = manager.getCache("tokenCache");
    }

    /**
     * 设置缓存
     * @param key
     * @param o
     */
    public void setCache(String key, Object o) {

        Element element = new Element(key, o);
        cache.put(element);

    }

    /**
     * 从缓存中获得结果
     * @param key
     * @return
     */
    public Object getCache(String key) {
        Element aa = cache.get(key);
        Object r = null;
        if (aa != null) {
            r = aa.getObjectValue();
        }
        return r;

    }

    /**
     * 清除某个缓存
     * @param key
     */
    public boolean removeCache(String key) {
        return cache.remove(key);
    }

    /**
    * @description: 清空全部缓存
    * @author: jinlei
    * @date: 2019/3/6 4:01 PM
    * @param: []
    * @return: void
    */
    public void removeAllCache() {
        cache.removeAll();
    }

    /**
     * @return the cache
     */
    public Cache getCache() {
        return cache;
    }

}
