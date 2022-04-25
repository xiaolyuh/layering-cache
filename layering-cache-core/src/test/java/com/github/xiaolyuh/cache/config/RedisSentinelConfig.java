package com.github.xiaolyuh.cache.config;

import com.github.xiaolyuh.redis.clinet.RedisClient;
import com.github.xiaolyuh.redis.clinet.RedisProperties;
import com.github.xiaolyuh.redis.clinet.SentinelRedisClient;
import com.github.xiaolyuh.redis.serializer.FastJsonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JacksonRedisSerializer;
import com.github.xiaolyuh.redis.serializer.JdkRedisSerializer;
import com.github.xiaolyuh.redis.serializer.KryoRedisSerializer;
import com.github.xiaolyuh.redis.serializer.ProtostuffRedisSerializer;
import com.github.xiaolyuh.redis.serializer.StringRedisSerializer;
import com.github.xiaolyuh.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class RedisSentinelConfig {

    @Value("${layering-cache.redis.nodes:127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381}")
    private String nodes;

    @Value("${spring.redis.password:}")
    private String password;

    @Bean
    public RedisClient layeringCacheRedisClient() {
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setSentinelNodes(nodes);
        redisProperties.setPassword(StringUtils.isBlank(password) ? null : password);

        KryoRedisSerializer kryoRedisSerializer = new KryoRedisSerializer();
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();
        JacksonRedisSerializer jacksonRedisSerializer = new JacksonRedisSerializer();
        JdkRedisSerializer jdkRedisSerializer = new JdkRedisSerializer();
        ProtostuffRedisSerializer protostuffRedisSerializer = new ProtostuffRedisSerializer();

        StringRedisSerializer keyRedisSerializer = new StringRedisSerializer();
        RedisClient redisClient = new SentinelRedisClient(redisProperties);

        redisClient.setKeySerializer(keyRedisSerializer);
        redisClient.setValueSerializer(kryoRedisSerializer);
        return redisClient;
    }
}
