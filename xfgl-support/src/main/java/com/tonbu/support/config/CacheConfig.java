package com.tonbu.support.config;


import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis缓存配置类
 * @author jinlei
 *
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

//    //缓存管理器
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        //user信息缓存配置
//        RedisCacheConfiguration userCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(10)).disableCachingNullValues().prefixKeysWith("user");
//        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
//        redisCacheConfigurationMap.put("user", userCacheConfiguration);
//        //初始化一个RedisCacheWriter
//        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
//
//
////        设置CacheManager的值序列化方式为JdkSerializationRedisSerializer,但其实RedisCacheConfiguration默认就是使用StringRedisSerializer序列化key，JdkSerializationRedisSerializer序列化value,所以以下注释代码为默认实现
////        ClassLoader loader = this.getClass().getClassLoader();
////        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer(loader);
////        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer);
////        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
//
//        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
//
//        //设置默认超过期时间是30秒
//        defaultCacheConfig.entryTtl(Duration.ofSeconds(30));
//        //初始化RedisCacheManager
//        RedisCacheManager cacheManager = new RedisCacheManager(redisCacheWriter, defaultCacheConfig, redisCacheConfigurationMap);
//        return cacheManager;
//    }
}