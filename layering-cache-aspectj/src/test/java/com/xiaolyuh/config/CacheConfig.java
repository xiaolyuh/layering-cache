package com.xiaolyuh.config;

import com.xiaolyuh.aspect.LayeringAspect;
import com.xiaolyuh.manager.CacheManager;
import com.xiaolyuh.manager.LayeringCacheManager;
import com.xiaolyuh.test.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({RedisConfig.class})
@EnableAspectJAutoProxy
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        return new LayeringCacheManager(redisTemplate);
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }
}
