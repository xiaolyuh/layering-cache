package com.github.xiaolyuh.config;

import com.github.xiaolyuh.aspect.LayeringAspect;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.test.BatchTestService;
import com.github.xiaolyuh.test.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RedisClusterConfig.class})
@EnableAspectJAutoProxy
public class CacheClusterConfig {

    @Bean
    public CacheManager layeringCacheManager(RedisClient layeringCacheRedisClient) {
        LayeringCacheManager layeringCacheManager = new LayeringCacheManager(layeringCacheRedisClient);
        // 开启统计功能
        layeringCacheManager.setStats(true);
        return layeringCacheManager;
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

    @Bean
    public BatchTestService testBatchTestService() {
        return new BatchTestService();
    }
}
