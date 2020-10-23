package com.tonbu.support.redis.service;

public interface RedisService {
    /**
     * set存数据
     * @param key
     * @param value
     * @return
     */
    boolean set(String key, String value);

    /**
     * get获取数据
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 设置有效天数
     * @param key
     * @param expire
     * @return
     */
    boolean expire(String key, long expire);

    /**
     * 查看过期时间
     *
     * @param key
     * @return
     */
    long getExpire(String key);

    /**
     * 移除数据
     * @param key
     * @return
     */
    boolean remove(String key);

    /**
     * set存数据
     * @param key //键
     * @param value //值
     * @param expire //过期时间
     * @return
     */
    boolean set(String key, String value, long expire);
}
