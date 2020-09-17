package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RedisClusterConfig.class})
public class CacheClusterConfig {

    @Bean
    public CacheManager layeringCacheManager(RedisClient layeringCacheRedisClient) {
        LayeringCacheManager layeringCacheManager = new LayeringCacheManager(layeringCacheRedisClient);
        // 开启统计功能
        layeringCacheManager.setStats(true);
        return layeringCacheManager;
    }

}
