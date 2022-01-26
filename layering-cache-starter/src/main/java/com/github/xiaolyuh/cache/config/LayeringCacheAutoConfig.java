package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.aspect.LayeringAspect;
import com.github.xiaolyuh.cache.properties.LayeringCacheProperties;
import com.github.xiaolyuh.cache.properties.LayeringCacheRedisProperties;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.manager.LayeringCacheManager;
import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.clinet.RedisProperties;
import com.github.xiaolyuh.redis.serializer.AbstractRedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import com.github.xiaolyuh.stats.extend.CacheStatsReportService;
import com.github.xiaolyuh.stats.extend.DefaultCacheStatsReportServiceImpl;
import com.github.xiaolyuh.util.GlobalConfig;
import com.github.xiaolyuh.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({LayeringCacheProperties.class, LayeringCacheRedisProperties.class})
public class LayeringCacheAutoConfig {

    @Value("${spring.application.name:}")
    private String applicationName;

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager layeringCacheManager(RedisClient layeringCacheRedisClient, CacheStatsReportService cacheStatsReportService, LayeringCacheProperties layeringCacheProperties) {

        LayeringCacheManager layeringCacheManager = new LayeringCacheManager(layeringCacheRedisClient);
        // 默认开启统计功能
        layeringCacheManager.setStats(layeringCacheProperties.isStats());
        // 上报缓存统计信息
        layeringCacheManager.setCacheStatsReportService(cacheStatsReportService);
        // 设置缓存命名空间
        GlobalConfig.setNamespace(StringUtils.isBlank(layeringCacheProperties.getNamespace()) ? applicationName : layeringCacheProperties.getNamespace());
        return layeringCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(CacheStatsReportService.class)
    public CacheStatsReportService cacheStatsReportService() {
        return new DefaultCacheStatsReportServiceImpl();
    }

    @Bean
    public LayeringAspect layeringAspect() {
        return new LayeringAspect();
    }

    @Bean
    public RedisProperties redisProperties(LayeringCacheRedisProperties layeringCacheRedisProperties) {
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setDatabase(layeringCacheRedisProperties.getDatabase());
        redisProperties.setHost(layeringCacheRedisProperties.getHost());
        redisProperties.setCluster(layeringCacheRedisProperties.getCluster());
        redisProperties.setPassword(StringUtils.isBlank(layeringCacheRedisProperties.getPassword()) ? null : layeringCacheRedisProperties.getPassword());
        redisProperties.setPort(layeringCacheRedisProperties.getPort());
        redisProperties.setTimeout(layeringCacheRedisProperties.getTimeout());
        redisProperties.setSerializer(layeringCacheRedisProperties.getSerializer());
        return redisProperties;
    }

    @Bean
    @ConditionalOnMissingBean(RedisClient.class)
    public RedisClient layeringCacheRedisClient(RedisProperties redisProperties) throws Exception {
        AbstractRedisSerializer valueRedisSerializer = (AbstractRedisSerializer) Class.forName(redisProperties.getSerializer()).newInstance();
        StringRedisSerializer keyRedisSerializer = new StringRedisSerializer();

        RedisClient redisClient = RedisClient.getInstance(redisProperties);
        redisClient.setKeySerializer(keyRedisSerializer);
        redisClient.setValueSerializer(valueRedisSerializer);
        return redisClient;
    }

}
