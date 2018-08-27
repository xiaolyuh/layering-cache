package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.aspect.LayeringAspect;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 多级缓存自动配置类
 *
 * @author xiaolyuh
 */
@Configuration
@ConditionalOnBean(RedisTemplate.class)
@AutoConfigureAfter({RedisAutoConfiguration.class})
@EnableAspectJAutoProxy
public class LayeringCacheAutoConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        return new LayeringCacheManager(redisTemplate);
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }

}
