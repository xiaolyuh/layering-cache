package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.aspect.LayeringAspect;
import com.github.xiaolyuh.cache.properties.LayeringCacheProperties;
import com.github.xiaolyuh.cache.properties.LayeringCacheRedisProperties;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.github.xiaolyuh.redis.clinet.ClusterRedisClient;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.clinet.RedisProperties;
import com.github.xiaolyuh.redis.clinet.SingleRedisClient;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import com.github.xiaolyuh.util.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 多级缓存自动配置类
 *
 * @author xiaolyuh
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({LayeringCacheProperties.class, LayeringCacheRedisProperties.class})
public class LayeringCacheAutoConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager layeringCacheManager(RedisClient layeringCacheRedisClient, LayeringCacheProperties layeringCacheProperties) {

        LayeringCacheManager layeringCacheManager = new LayeringCacheManager(layeringCacheRedisClient);
        // 默认开启统计功能
        layeringCacheManager.setStats(layeringCacheProperties.isStats());
        return layeringCacheManager;
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }


    @Bean
    public RedisClient layeringCacheRedisClient(LayeringCacheRedisProperties layeringCacheRedisProperties) {
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setDatabase(layeringCacheRedisProperties.getDatabase());
        redisProperties.setHost(layeringCacheRedisProperties.getHost());
        redisProperties.setCluster(layeringCacheRedisProperties.getCluster());
        redisProperties.setPassword(StringUtils.isBlank(layeringCacheRedisProperties.getPassword()) ? null : layeringCacheRedisProperties.getPassword());
        redisProperties.setPort(layeringCacheRedisProperties.getPort());

        KryoRedisSerializer<Object> kryoRedisSerializer = new KryoRedisSerializer<>(Object.class);
        StringRedisSerializer keyRedisSerializer = new StringRedisSerializer();

        RedisClient redisClient = new SingleRedisClient(redisProperties);
        if (StringUtils.isNotBlank(redisProperties.getCluster())) {
            redisClient = new ClusterRedisClient(redisProperties);
        }

        redisClient.setKeySerializer(keyRedisSerializer);
        redisClient.setValueSerializer(kryoRedisSerializer);
        return redisClient;
    }
}
